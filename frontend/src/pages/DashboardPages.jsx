import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/Client';

export default function DashboardPage() {
  const [participations, setParticipations] = useState([]);
  const [createdMissions, setCreatedMissions] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [reports, setReports] = useState([]);
  const [appeals, setAppeals] = useState([]);
  const [selectedMissionParts, setSelectedMissionParts] = useState({ missionId: null, list: [] });
  const [modReportId, setModReportId] = useState('');
  const [modAppealReason, setModAppealReason] = useState('');
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  const loadAll = () => {
    api.get('/participations/mine').then((r) => setParticipations(r.data)).catch(() => {});
    api.get('/missions/mine').then((r) => setCreatedMissions(r.data)).catch(() => {});
    api.get('/notifications').then((r) => setNotifications(r.data)).catch(() => {});
    api.get('/reports/mine').then((r) => setReports(r.data)).catch(() => {});
    api.get('/appeals/mine').then((r) => setAppeals(r.data)).catch(() => {});
  };

  useEffect(() => { loadAll(); }, []);

  const handlePublish = async (missionId) => {
    await api.post(`/missions/${missionId}/publish`);
    loadAll();
  };

  const loadMissionParticipations = async (missionId) => {
    const res = await api.get(`/missions/${missionId}/participations`);
    setSelectedMissionParts({ missionId, list: res.data });
  };

  const handleVerify = async (participationId, objectiveId, approved) => {
    const note = prompt(approved ? 'Nota approvazione (opzionale):' : 'Motivo rifiuto:') || '';
    await api.post(`/participations/${participationId}/objectives/${objectiveId}/verify`, { approved, note });
    loadMissionParticipations(selectedMissionParts.missionId);
    loadAll();
  };

  const handleModerationAppeal = async (e) => {
    e.preventDefault();
    setError('');
    setMsg('');
    try {
      await api.post('/appeals', {
        targetType: 'MODERATION_REPORT',
        targetId: Number(modReportId),
        reason: modAppealReason,
      });
      setMsg('Richiesta di riesame sulla moderazione inviata!');
      setModReportId('');
      setModAppealReason('');
      loadAll();
    } catch (err) {
      setError(err.response?.data?.error || 'Errore richiesta riesame');
    }
  };

  return (
    <div className="page form-stack">
      <h1>Area Personale</h1>
      {msg && <div className="alert-ok">{msg}</div>}
      {error && <div className="alert-error">{error}</div>}

      <section className="card">
        <h2>Le mie Partecipazioni ({participations.length})</h2>
        {participations.length === 0 ? <p className="muted">Non hai ancora accettato missioni.</p> : (
          participations.map((p) => (
            <div key={p.id} className="subcard row-between">
              <div>
                <strong>{p.missionTitle}</strong> • <span className="badge">{p.status}</span>
                <div className="muted">Punti: {p.scoreAwarded} / {p.missionMaxPoints}</div>
              </div>
              <Link to={`/participations/${p.id}`} className="btn-primary">Apri</Link>
            </div>
          ))
        )}
      </section>

      <section className="card">
        <div className="row-between">
          <h2>Missioni Create da me ({createdMissions.length})</h2>
          <Link to="/create-mission" className="btn-primary">+ Nuova</Link>
        </div>
        {createdMissions.map((m) => (
          <div key={m.id} className="subcard">
            <div className="row-between">
              <div>
                <strong>{m.title}</strong> • <span className="badge">{m.status}</span>
              </div>
              <div className="row-actions">
                {m.status === 'DRAFT' && (
                  <button className="btn-primary" onClick={() => handlePublish(m.id)}>Pubblica</button>
                )}
                <Link to={`/edit-mission/${m.id}`} className="btn-secondary">Modifica</Link>
                <button className="btn-secondary" onClick={() => loadMissionParticipations(m.id)}>
                  Prove / Partecipanti
                </button>
              </div>
            </div>

            {selectedMissionParts.missionId === m.id && (
              <div style={{ marginTop: '10px' }}>
                <h4>Partecipanti ({selectedMissionParts.list.length})</h4>
                {selectedMissionParts.list.map((part) => (
                  <div key={part.id} className="notif-item">
                    <strong>{part.participantNickname}</strong> — Stato: <strong>{part.status}</strong>
                    {part.objectives.filter((o) => o.requiresProof && o.completed).map((o) => (
                      <div key={o.objectiveId} style={{ marginTop: '6px' }}>
                        <div>Obiettivo: <em>{o.objectiveTitle}</em> ({o.verificationStatus})</div>
                        <div className="muted">Prova: {o.proofText} {o.proofImageUrl}</div>
                        {o.verificationStatus === 'PENDING' && (
                          <div className="row-actions" style={{ marginTop: '4px' }}>
                            <button className="btn-primary" onClick={() => handleVerify(part.id, o.objectiveId, true)}>Approva</button>
                            <button className="btn-danger" onClick={() => handleVerify(part.id, o.objectiveId, false)}>Rifiuta</button>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                ))}
              </div>
            )}
          </div>
        ))}
      </section>

      <section className="card">
        <h2>Storico Notifiche ({notifications.length})</h2>
        {notifications.map((n) => (
          <div key={n.id} className="notif-item">
            <small>{n.type} {n.referenceId ? `• Rif #${n.referenceId}` : ''} • {new Date(n.createdAt).toLocaleString()}</small>
            <p>{n.message}</p>
          </div>
        ))}
      </section>

      <section className="card">
        <h2>Le mie Segnalazioni ({reports.length})</h2>
        {reports.map((r) => (
          <div key={r.id} className="subcard">
            <strong>#{r.id} su "{r.missionTitle}"</strong> • <span className="badge">{r.status}</span>
            <p className="muted">Motivo: {r.reason} — {r.details}</p>
            {r.decisionNote && <p><strong>Esito moderazione:</strong> {r.decisionNote}</p>}
          </div>
        ))}
      </section>

      <section className="card">
        <h2>I miei Riesami ({appeals.length})</h2>
        {appeals.map((a) => (
          <div key={a.id} className="subcard">
            <strong>Riesame #{a.id} ({a.targetType} #{a.targetId})</strong> • <span className="badge">{a.status}</span>
            <p className="muted">{a.reason}</p>
            {a.decisionNote && <p><strong>Risposta Admin:</strong> {a.decisionNote}</p>}
          </div>
        ))}

        <form className="subcard form-stack" onSubmit={handleModerationAppeal}>
          <h4>Contesta una decisione di moderazione su una tua missione</h4>
          <input
            type="number"
            placeholder="ID Segnalazione (indicato nella notifica, es. Rif #1)"
            value={modReportId}
            onChange={(e) => setModReportId(e.target.value)}
            required
          />
          <textarea
            placeholder="Motivazione del riesame..."
            value={modAppealReason}
            onChange={(e) => setModAppealReason(e.target.value)}
            required
            minLength={5}
          />
          <button type="submit" className="btn-secondary">Invia Richiesta Riesame</button>
        </form>
      </section>
    </div>
  );
}