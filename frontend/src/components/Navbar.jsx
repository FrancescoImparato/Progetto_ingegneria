import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/Client';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [unread, setUnread] = useState([]);
  const [openNotif, setOpenNotif] = useState(false);

  useEffect(() => {
    if (!user) {
      setUnread([]);
      return;
    }
    const fetchUnread = () => {
      api.get('/notifications/unread')
        .then((res) => setUnread(res.data))
        .catch(() => {});
    };
    fetchUnread();
    const interval = setInterval(fetchUnread, 5000);
    return () => clearInterval(interval);
  }, [user]);

  const markAllRead = async () => {
    await api.post('/notifications/read-all');
    setUnread([]);
    setOpenNotif(false);
  };

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="navbar">
      <div className="nav-left">
        <Link to="/" className="brand">SideQuest</Link>
        <Link to="/">Esplora</Link>
        {user && <Link to="/create-mission">Crea Missione</Link>}
        {user && <Link to="/dashboard">Area Personale</Link>}
        {user?.role === 'ADMIN' && <Link to="/admin">Moderazione</Link>}
      </div>

      <div className="nav-right">
        {user ? (
          <>
            <div className="notif-wrapper">
              <button className="btn-secondary" onClick={() => setOpenNotif(!openNotif)}>
                Notifiche ({unread.length})
              </button>
              {openNotif && (
                <div className="notif-dropdown">
                  <div className="notif-header">
                    <strong>Non lette</strong>
                    {unread.length > 0 && (
                      <button className="link-btn" onClick={markAllRead}>Segna tutte lette</button>
                    )}
                  </div>
                  {unread.length === 0 ? (
                    <p className="muted">Nessuna nuova notifica</p>
                  ) : (
                    unread.map((n) => (
                      <div key={n.id} className="notif-item">
                        <small>{n.type}</small>
                        <p>{n.message}</p>
                      </div>
                    ))
                  )}
                  <Link to="/dashboard" onClick={() => setOpenNotif(false)}>
                    Vedi storico completo →
                  </Link>
                </div>
              )}
            </div>
            <Link to="/profile">{user.nickname}</Link>
            <button className="btn-danger" onClick={handleLogout}>Esci</button>
          </>
        ) : (
          <>
            <Link to="/login">Accedi</Link>
            <Link to="/register" className="btn-primary">Registrati</Link>
          </>
        )}
      </div>
    </nav>
  );
}