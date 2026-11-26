import { useEffect, useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import api from '../api/Client';
import { useAuth } from '../context/AuthContext';
import MissionMap from '../components/MissionMap';

export default function MissionDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [mission, setMission] = useState(null);
  const [error, setError] = useState('');
  const [msg, setMsg] = useState('');
  const [showReport, setShowReport] = useState(false);
  const [reason, setReason] = useState('INAPPROPRIATE_CONTENT');
  const [details, setDetails] = useState('');

  useEffect(() => {
    api.get(`/missions/${id}`)
      .then((res) => setMission(res.data))
      .catch((err) => setError(err.response?.data?.error || 'Missione non trovata'));
  }, [id]);

  const handleAccept = async () => {
    setError('');
    try {
      const res = await api.post(`/missions/${id}/accept`);
      navigate(`/participations/${res.data.id}`);
    } catch (err) {
      setError(err.response?.data?.error || 'Impossibile accettare la missione');
    }
  };

  const handleReport = async (e) => {
    e.preventDefault();
    setError('');
    setMsg('');
    try {
      await api.post('/reports', { missionId: Number(id), reason, details });
      setMsg('Segnalazione inviata con successo. Puoi seguirne lo stato in Area Personale.');
      setShowReport(false);
      setDetails('');
    } catch (err) {
      setError(err.response?.data?.error || 'Errore invio segnalazione');
    }
  };

  if (!mission) return <div className="page">{error ? <div className="alert-error">{error}</div> : 'Caricamento...'}</div>;

  const isAuthor = user && user.id === mission.authorId;

  return (
    <div className="page">
      <div className="card">
        <div className="card-badges">
          <span className="badge">{mission.category}</span>
          <span className={`badge ${mission.type === 'SECRET_TIMED' ? 'badge-secret' : ''}`}>
            {mission.type === 'SECRET_TIMED' ? `Segreta (${mission.durationMinutes} min)` : 'Ordinaria'}
          </span>
          <span className="badge">{mission.points} pt</span>
        </div>

        <h1>{mission.title}</h1>
        <p>{mission.description}</p>
        <p className="muted">
          Autore: <strong>{mission.authorNickname}</strong> • Luogo: {mission.locationName || 'Non specificato'}
        </p>
        {mission.participationConditions && (
          <p><strong>Condizioni:</strong> {mission.participationConditions}</p>
        )}

        {error && <div className="alert-error">{error}</div>}
        {msg && <div className="alert-ok">{msg}</div>}

        <h3>Obiettivi ({mission.objectivesCount})</h3>
        {mission.type === 'SECRET_TIMED' && mission.objectives.length === 0 ? (
          <div className="subcard">
            🔒 <strong>Missione Segreta a Tempo:</strong> gli obiettivi verranno svelati solo dopo l'accettazione e da quel momento partirà il timer di {mission.durationMinutes} minuti!
          </div>
        ) : (
          <ul>
            {mission.objectives.map((o) => (
              <li key={o.id}>
                <strong>{o.orderIndex}. {o.title}</strong> {o.requiresProof ? '(Richiede prova)' : ''}
                {o.description && <div className="muted">{o.description}</div>}
              </li>
            ))}
          </ul>
        )}

        <div className="row-actions">
          {user && !isAuthor && mission.status === 'PUBLISHED' && (
            <button className="btn-primary" onClick={handleAccept}>Accetta Missione</button>
          )}
          {!user && <Link to="/login" className="btn-primary">Accedi per partecipare</Link>}
          {isAuthor && (
            <Link to={`/edit-mission/${mission.id}`} className="btn-secondary">Modifica Missione</Link>
          )}
          {user && !isAuthor && (
            <button className="btn-danger" onClick={() => setShowReport(!showReport)}>Segnala</button>
          )}
        </div>

        {showReport && (
          <form className="subcard form-stack" onSubmit={handleReport}>
            <h4>Segnala missione inappropriata (R13)</h4>
            <select value={reason} onChange={(e) => setReason(e.target.value)}>
              <option value="INAPPROPRIATE_CONTENT">Contenuto inappropriato</option>
              <option value="SPAM">Spam</option>
              <option value="UNSAFE_LOCATION">Luogo non sicuro</option>
              <option value="MISLEADING">Descrizione fuorviante</option>
              <option value="OTHER">Altro</option>
            </select>
            <textarea
              placeholder="Descrivi brevemente il motivo..."
              value={details}
              onChange={(e) => setDetails(e.target.value)}
              required
              minLength={5}
            />
            <button type="submit" className="btn-danger">Invia Segnalazione</button>
          </form>
        )}
      </div>

      {mission.latitude != null && mission.longitude != null && (
        <div style={{ marginTop: '20px' }}>
          <MissionMap missions={[mission]} />
        </div>
      )}
    </div>
  );
}