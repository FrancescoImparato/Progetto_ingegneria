import { useEffect, useState } from 'react';
import api from '../api/Client';

export default function AdminPage() {
  const [reports, setReports] = useState([]);
  const [appeals, setAppeals] = useState([]);
  const [logs, setLogs] = useState([]);
  const [notes, setNotes] = useState({});
  const [error, setError] = useState('');

  const load = () => {
    api.get('/admin/reports').then((r) => setReports(r.data)).catch((e) => setError(e.response?.data?.error || 'Accesso negato'));
    api.get('/admin/appeals').then((r) => setAppeals(r.data)).catch(() => {});
    api.get('/admin/audit-logs').then((r) => setLogs(r.data)).catch(() => {});
  };

  useEffect(() => { load(); }, []);

  const decideReport = async (id, action) => {
    setError('');
    const note = notes[`r-${id}`] || 'Decisione presa dal moderatore';
    try {
      await api.post(`/admin/reports/${id}/decide`, { action, decisionNote: note });
      load();
    } catch (err) {
      setError(err.response?.data?.error || 'Errore moderazione');
    }
  };

  const decideAppeal = async (id, accepted) => {
    setError('');
    const note = notes[`a-${id}`] || (accepted ? 'Riesame accolto' : 'Riesame respinto');
    try {
      await api.post(`/admin/appeals/${id}/decide`, { accepted, decisionNote: note });
      load();
    } catch (err) {
      setError(err.response?.data?.error || 'Errore riesame');
    }
  };

  return (
    <div className="page form-stack">
      <h1>Pannello Moderazione & Riesami (R14–R15)</h1>
      {error && <div className="alert-error">{error}</div>}

      <section className="card">
        <h2>Segnalazioni ({reports.length})</h2>
        {reports.map((r) => (
          <div key={r.id} className="subcard form-stack">
            <div className="row-between">
              <strong>Segnalazione #{r.id} su "{r.missionTitle}" ({r.missionStatus})</strong>
              <span className="badge">{r.status}</span>
            </div>
            <p className="muted">Da: {r.reporterNickname} • Motivo: {r.reason} — {r.details}</p>
            {r.status === 'OPEN' ? (
              <>
                <input
                  placeholder="Nota motivata (min 5 caratteri)..."
                  value={notes[`r-${r.id}`] || ''}
                  onChange={(e) => setNotes({ ...notes, [`r-${r.id}`]: e.target.value })}
                />
                <div className="row-actions">
                  <button className="btn-secondary" onClick={() => decideReport(r.id, 'DISMISS')}>Archivia</button>
                  <button className="btn-primary" onClick={() => decideReport(r.id, 'SUSPEND_MISSION')}>Sospendi Missione</button>
                  <button className="btn-danger" onClick={() => decideReport(r.id, 'REMOVE_MISSION')}>Rimuovi Missione</button>
                </div>
              </>
            ) : (
              <p><strong>Azione:</strong> {r.actionTaken} — {r.decisionNote}</p>
            )}
          </div>
        ))}
      </section>

      <section className="card">
        <h2>Richieste di Riesame ({appeals.length})</h2>
        {appeals.map((a) => (
          <div key={a.id} className="subcard form-stack">
            <div className="row-between">
              <strong>Riesame #{a.id} ({a.targetType} #{a.targetId}) — {a.requesterNickname}</strong>
              <span className="badge">{a.status}</span>
            </div>
            <p className="muted">{a.reason}</p>
            {a.status === 'PENDING' ? (
              <>
                <input
                  placeholder="Nota di riesame (min 5 caratteri)..."
                  value={notes[`a-${a.id}`] || ''}
                  onChange={(e) => setNotes({ ...notes, [`a-${a.id}`]: e.target.value })}
                />
                <div className="row-actions">
                  <button className="btn-primary" onClick={() => decideAppeal(a.id, true)}>Accogli Riesame</button>
                  <button className="btn-danger" onClick={() => decideAppeal(a.id, false)}>Respingi</button>
                </div>
              </>
            ) : (
              <p><strong>Esito:</strong> {a.decisionNote}</p>
            )}
          </div>
        ))}
      </section>

      <section className="card">
        <h2>Audit Log Decisioni ({logs.length})</h2>
        {logs.map((l) => (
          <div key={l.id} className="notif-item">
            <strong>{l.action}</strong> su {l.targetType} #{l.targetId} — da {l.actorNickname} ({new Date(l.createdAt).toLocaleString()})
            <div className="muted">{l.note}</div>
          </div>
        ))}
      </section>
    </div>
  );
}