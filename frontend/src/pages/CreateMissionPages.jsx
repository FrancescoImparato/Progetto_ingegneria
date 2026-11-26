import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet';
import api from '../api/Client';

function LocationPicker({ position, onChange }) {
  useMapEvents({
    click(e) {
      onChange(e.latlng.lat, e.latlng.lng);
    },
  });
  return position ? <Marker position={position} /> : null;
}

export default function CreateMissionPage() {
  const { id } = useParams(); // se presente, siamo in modalità modifica
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('EXPLORATION');
  const [type, setType] = useState('ORDINARY');
  const [durationMinutes, setDurationMinutes] = useState(20);
  const [points, setPoints] = useState(100);
  const [participationConditions, setParticipationConditions] = useState('');
  const [locationName, setLocationName] = useState('');
  const [latitude, setLatitude] = useState(40.8359);
  const [longitude, setLongitude] = useState(14.2488);
  const [publishImmediately, setPublishImmediately] = useState(true);
  const [objectives, setObjectives] = useState([
    { title: '', description: '', requiresProof: false },
  ]);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!id) return;
    api.get(`/missions/${id}`).then((res) => {
      const m = res.data;
      setTitle(m.title);
      setDescription(m.description);
      setCategory(m.category);
      setType(m.type);
      setDurationMinutes(m.durationMinutes || 20);
      setPoints(m.points);
      setParticipationConditions(m.participationConditions || '');
      setLocationName(m.locationName || '');
      if (m.latitude != null) setLatitude(m.latitude);
      if (m.longitude != null) setLongitude(m.longitude);
      setPublishImmediately(m.status === 'PUBLISHED');
      if (m.objectives?.length > 0) {
        setObjectives(m.objectives.map((o) => ({
          title: o.title,
          description: o.description || '',
          requiresProof: o.requiresProof,
        })));
      }
    }).catch((err) => setError(err.response?.data?.error || 'Impossibile caricare la missione'));
  }, [id]);

  const updateObj = (idx, field, value) => {
    const copy = [...objectives];
    copy[idx] = { ...copy[idx], [field]: value };
    setObjectives(copy);
  };

  const addObjective = () => {
    setObjectives([...objectives, { title: '', description: '', requiresProof: false }]);
  };

  const removeObjective = (idx) => {
    if (objectives.length === 1) return;
    setObjectives(objectives.filter((_, i) => i !== idx));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const payload = {
      title,
      description,
      category,
      type,
      durationMinutes: type === 'SECRET_TIMED' ? Number(durationMinutes) : null,
      points: Number(points),
      participationConditions,
      latitude: Number(latitude),
      longitude: Number(longitude),
      locationName,
      publishImmediately,
      objectives,
    };

    try {
      if (id) {
        await api.put(`/missions/${id}`, payload);
      } else {
        await api.post('/missions', payload);
      }
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.error || 'Errore durante il salvataggio');
    }
  };

  return (
    <div className="page">
      <h1>{id ? 'Modifica Missione' : 'Crea Nuova Missione'}</h1>
      {error && <div className="alert-error">{error}</div>}

      <form className="card form-stack" onSubmit={handleSubmit}>
        <label>Titolo</label>
        <input value={title} onChange={(e) => setTitle(e.target.value)} required minLength={3} />

        <label>Presentazione / Descrizione</label>
        <textarea rows={3} value={description} onChange={(e) => setDescription(e.target.value)} required minLength={10} />

        <div className="row-2">
          <div>
            <label>Categoria</label>
            <select value={category} onChange={(e) => setCategory(e.target.value)}>
              <option value="EXPLORATION">Esplorazione</option>
              <option value="ENIGMA">Enigma</option>
              <option value="CULTURE">Cultura</option>
              <option value="PHOTOGRAPHY">Fotografia</option>
              <option value="CREATIVE">Creativa</option>
            </select>
          </div>
          <div>
            <label>Modalità</label>
            <select value={type} onChange={(e) => setType(e.target.value)}>
              <option value="ORDINARY">Ordinaria</option>
              <option value="SECRET_TIMED">Segreta a tempo</option>
            </select>
          </div>
        </div>

        <div className="row-2">
          {type === 'SECRET_TIMED' && (
            <div>
              <label>Durata massima (minuti)</label>
              <input type="number" min={1} max={1440} value={durationMinutes} onChange={(e) => setDurationMinutes(e.target.value)} required />
            </div>
          )}
          <div>
            <label>Punti ricompensa</label>
            <input type="number" min={10} max={10000} value={points} onChange={(e) => setPoints(e.target.value)} required />
          </div>
        </div>

        <label>Condizioni di partecipazione (opzionale)</label>
        <input value={participationConditions} onChange={(e) => setParticipationConditions(e.target.value)} />

        <label>Nome del luogo</label>
        <input placeholder="Es. Piazza Bellini, Napoli" value={locationName} onChange={(e) => setLocationName(e.target.value)} />

        <label>Clicca sulla mappa per scegliere le coordinate ({latitude.toFixed(4)}, {longitude.toFixed(4)})</label>
        <MapContainer center={[latitude, longitude]} zoom={13} style={{ height: '250px', width: '100%', borderRadius: '8px' }}>
          <TileLayer
            attribution='&copy; OpenStreetMap contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <LocationPicker
            position={[latitude, longitude]}
            onChange={(lat, lng) => { setLatitude(lat); setLongitude(lng); }}
          />
        </MapContainer>

        <h3>Obiettivi della missione</h3>
        {objectives.map((obj, idx) => (
          <div key={idx} className="subcard">
            <div className="row-between">
              <strong>Obiettivo #{idx + 1}</strong>
              {objectives.length > 1 && (
                <button type="button" className="btn-danger" onClick={() => removeObjective(idx)}>Rimuovi</button>
              )}
            </div>
            <input
              placeholder="Titolo obiettivo"
              value={obj.title}
              onChange={(e) => updateObj(idx, 'title', e.target.value)}
              required
            />
            <input
              placeholder="Descrizione / istruzioni"
              value={obj.description}
              onChange={(e) => updateObj(idx, 'description', e.target.value)}
            />
            <label className="checkbox-row">
              <input
                type="checkbox"
                checked={obj.requiresProof}
                onChange={(e) => updateObj(idx, 'requiresProof', e.target.checked)}
              />
              Richiede verifica di una prova dall'autore
            </label>
          </div>
        ))}
        <button type="button" className="btn-secondary" onClick={addObjective}>+ Aggiungi obiettivo</button>

        <label className="checkbox-row">
          <input
            type="checkbox"
            checked={publishImmediately}
            onChange={(e) => setPublishImmediately(e.target.checked)}
          />
          Pubblica subito sulla piattaforma
        </label>

        <button type="submit" className="btn-primary">{id ? 'Salva modifiche' : 'Crea Missione'}</button>
      </form>
    </div>
  );
}