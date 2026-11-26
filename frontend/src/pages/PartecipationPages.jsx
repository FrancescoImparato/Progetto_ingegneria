import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../api/Client';

export default function ParticipationPage() {
  const { id } = useParams();
  const [p, setP] = useState(null);
  const [secondsLeft, setSecondsLeft] = useState(0);
  const [proofInputs, setProofInputs] = useState({});
  const [appealReason, setAppealReason] = useState('');
  const [error, setError] = useState('');
  const [msg, setMsg] = useState('');

  const load = async () => {
    try {
      const res = await api.get(`/participations/${id}`);
      setP(res.data);
      setSecondsLeft(res.data.remainingSeconds || 0);
    } catch (err) {
      setError(err.response?.data?.error || 'Impossibile caricare la partecipazione');
    }
  };

  useEffect(() => { load(); }, [id]);

  // Countdown visivo sincronizzato col valore autorevole ricevuto dal server
  useEffect(() => {
    if (!p || p.missionType !== 'SECRET_TIMED' || p.status !== 'IN_PROGRESS') return;
    const timer = setInterval(() => {
      setSecondsLeft((prev) => {
        if (prev <= 1) {
          load(); // chiede al server l'aggiornamento ufficiale a EXPIRED
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, [p?.status, p?.missionType]);

  const handleCompleteObjective = async (objectiveId) => {
    setError('');
    setMsg('');
    const input = proofInputs[objectiveId] || {};
    try {
      const res = await api.post(`/participations/${id}/objectives/${objectiveId}/complete`, {
        proofText: input.proofText || '',
        proofImageUrl: input.proofImageUrl || '',
      });
      setP(res.data);
      setMsg('Progresso salvato!');
    } catch (err) {
      setError(err.response?.data?.error || 'Errore completamento obiettivo');
      load();
    }
  };

  const handleAbandon = async () => {
    setError('');
    try {
      const res = await api.post(`/participations/${id}/abandon`);
      setP(res.data);
    } catch (err) {
      setError(err.response?.data?.error || 'Errore durante abbandono');
    }
  };

  const handleAppeal = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await api.post('/appeals', {
        targetType: 'PARTICIPATION_OUTCOME',
        targetId: Number(id),
        reason: appealReason,
      });
      setMsg('Richiesta di riesame inviata! Controllane lo stato in Area Personale.');
      setAppealReason('');
    } catch (err) {
      setError(err.response?.data?.error || 'Errore invio riesame');
    }
  };

  if (!p) return <div className="page">{error ? <div className="alert-error">{error}</div> : 'Caricamento...'}</div>;

  const mins = Math.floor(secondsLeft / 60);
  const secs = secondsLeft % 60;

  return (
    <div className="page">
      <div className="card">
        <div className="row-between">
          <h1>{p.missionTitle}</h1>
          <span className="badge">{p.status}</span>
        </div>

        {p.missionType === 'SECRET_TIMED' && p.status === 'IN_PROGRESS' && (
          <div className="timer-box">
            ⏳ Tempo residuo (server): <strong>{mins}:{secs.toString().padStart(2, '0')}</strong>
          </div>
        )}

        <p className="muted">Punti ottenuti: <strong>{p.scoreAwarded}</strong> / {p.missionMaxPoints}</p>

        {error && <div className="alert-error">{error}</div>}
        {msg && <div className="alert-ok">{msg}</div>}

        <h3>Obiettivi</h3>
        {p.objectives.map((obj) => (
          <div key={obj.objectiveId} className="subcard">
            <div className="row-between">
              <strong>{obj.orderIndex}. {obj.objectiveTitle}</strong>
              <span className="badge">{obj.verificationStatus}</span>
            </div>
            {obj.objectiveDescription && <p className="muted">{obj.objectiveDescription}</p>}

            {obj.completed ? (
              <div>
                <p className="muted">
                  Prova inviata: {obj.proofText || '(nessun testo)'} {obj.proofImageUrl ? `| Img: ${obj.proofImageUrl}` : ''}
                </p>
                {obj.verificationNote && <p><strong>Nota verifica:</strong> {obj.verificationNote}</p>}
              </div>
            ) : p.status === 'IN_PROGRESS' ? (
              <div className="form-stack">
                <input
                  placeholder={obj.requiresProof ? 'Descrivi la tua prova (obbligatoria se senza URL)...' : 'Nota opzionale...'}
                  value={proofInputs[obj.objectiveId]?.proofText || ''}
                  onChange={(e) => setProofInputs({
                    ...proofInputs,
                    [obj.objectiveId]: { ...proofInputs[obj.objectiveId], proofText: e.target.value },
                  })}
                />
                <input
                  placeholder="URL immagine prova (opzionale, es. https://...)"
                  value={proofInputs[obj.objectiveId]?.proofImageUrl || ''}
                  onChange={(e) => setProofInputs({
                    ...proofInputs,
                    [obj.objectiveId]: { ...proofInputs[obj.objectiveId], proofImageUrl: e.target.value },
                  })}
                />
                <button className="btn-primary" onClick={() => handleCompleteObjective(obj.objectiveId)}>
                  Completa Obiettivo
                </button>
              </div>
            ) : null}
          </div>
        ))}

        {p.status === 'IN_PROGRESS' && (
          <button className="btn-danger" onClick={handleAbandon}>Abbandona Missione</button>
        )}

        {(p.status === 'FAILED' || p.status === 'EXPIRED') && (
          <form className="subcard form-stack" onSubmit={handleAppeal}>
            <h4>Esito contestato? Richiedi Riesame (R15)</h4>
            <textarea
              placeholder="Spiega perché richiedi la revisione dell'esito..."
              value={appealReason}
              onChange={(e) => setAppealReason(e.target.value)}
              required
              minLength={5}
            />
            <button type="submit" className="btn-secondary">Invia Richiesta di Riesame</button>
          </form>
        )}
      </div>
    </div>
  );
}