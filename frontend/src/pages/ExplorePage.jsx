// src/pages/ExplorePage.jsx
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/Client';
import MissionMap from '../components/MissionMap';

export default function ExplorePage() {
  const [missions, setMissions] = useState([]);
  const [q, setQ] = useState('');
  const [category, setCategory] = useState('');
  const [type, setType] = useState('');
  const [loading, setLoading] = useState(true);

  const loadMissions = async () => {
    setLoading(true);
    try {
      const params = {};
      if (q.trim()) params.q = q.trim();
      if (category) params.category = category;
      if (type) params.type = type;
      const res = await api.get('/missions', { params });
      setMissions(res.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMissions();
  }, []);

  const handleFilterSubmit = (e) => {
    e.preventDefault();
    loadMissions();
  };

  return (
    <div className="page">
      <h1>Esplora le Missioni</h1>

      <form className="filters-bar" onSubmit={handleFilterSubmit}>
        <input
          type="text"
          placeholder="Cerca per parola chiave o luogo..."
          value={q}
          onChange={(e) => setQ(e.target.value)}
        />
        <select value={category} onChange={(e) => setCategory(e.target.value)}>
          <option value="">Tutte le categorie</option>
          <option value="EXPLORATION">Esplorazione</option>
          <option value="ENIGMA">Enigma</option>
          <option value="CULTURE">Cultura</option>
          <option value="PHOTOGRAPHY">Fotografia</option>
          <option value="CREATIVE">Creativa</option>
        </select>
        <select value={type} onChange={(e) => setType(e.target.value)}>
          <option value="">Tutte le modalità</option>
          <option value="ORDINARY">Ordinaria</option>
          <option value="SECRET_TIMED">Segreta a tempo</option>
        </select>
        <button type="submit" className="btn-primary">Filtra</button>
      </form>

      <MissionMap missions={missions} />

      {loading ? (
        <p>Caricamento missioni...</p>
      ) : missions.length === 0 ? (
        <p>Nessuna missione trovata con i filtri selezionati.</p>
      ) : (
        <div className="cards-grid">
          {missions.map((m) => (
            <div key={m.id} className="card">
              <div className="card-badges">
                <span className="badge">{m.category}</span>
                <span className={`badge ${m.type === 'SECRET_TIMED' ? 'badge-secret' : ''}`}>
                  {m.type === 'SECRET_TIMED' ? `Segreta (${m.durationMinutes} min)` : 'Ordinaria'}
                </span>
              </div>
              <h3>{m.title}</h3>
              <p>{m.description}</p>
              <p className="muted">
                Luogo: {m.locationName || 'Ovunque'} • {m.objectivesCount} obiettivi • {m.points} pt
              </p>
              <Link to={`/missions/${m.id}`} className="btn-primary">Vedi missione</Link>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}