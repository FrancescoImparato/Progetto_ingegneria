import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import { Link } from 'react-router-dom';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

export default function MissionMap({ missions }) {
  const geoMissions = missions.filter((m) => m.latitude != null && m.longitude != null);
  const center = geoMissions.length > 0
    ? [geoMissions[0].latitude, geoMissions[0].longitude]
    : [40.8359, 14.2488]; // Default: Napoli

  return (
    <div className="map-box">
      <MapContainer center={center} zoom={13} style={{ height: '380px', width: '100%', borderRadius: '8px' }}>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        {geoMissions.map((m) => (
          <Marker key={m.id} position={[m.latitude, m.longitude]}>
            <Popup>
              <strong>{m.title}</strong><br />
              <span>{m.category} • {m.type === 'SECRET_TIMED' ? 'Segreta a tempo' : 'Ordinaria'}</span><br />
              <span>{m.points} punti</span><br />
              <Link to={`/missions/${m.id}`}>Apri dettagli →</Link>
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
}