// src/pages/AuthPages.jsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/Client';
import { useAuth } from '../context/AuthContext';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await login(email, password);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.error || 'Errore di accesso');
    }
  };

  return (
    <div className="auth-box">
      <h2>Accedi a SideQuest</h2>
      {error && <div className="alert-error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <label>Email</label>
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <label>Password</label>
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        <button type="submit" className="btn-primary">Accedi</button>
      </form>
      <p className="muted" style={{ marginTop: '12px' }}>
        Demo: <code>mario@sidequest.it</code> / <code>Mario123!</code> oppure <code>admin@sidequest.it</code> / <code>Admin123!</code>
      </p>
    </div>
  );
}

export function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [nickname, setNickname] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await register(email, password, nickname);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.error || 'Errore nella registrazione');
    }
  };

  return (
    <div className="auth-box">
      <h2>Registrati</h2>
      {error && <div className="alert-error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <label>Email</label>
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <label>Nickname</label>
        <input type="text" value={nickname} onChange={(e) => setNickname(e.target.value)} required minLength={3} />
        <label>Password</label>
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={6} />
        <button type="submit" className="btn-primary">Crea Account</button>
      </form>
    </div>
  );
}

export function ProfilePage() {
  const { user, setUser } = useAuth();
  const [nickname, setNickname] = useState(user?.nickname || '');
  const [bio, setBio] = useState(user?.bio || '');
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  if (!user) return <p className="page">Devi effettuare il login.</p>;

  const handleSave = async (e) => {
    e.preventDefault();
    setMsg('');
    setError('');
    try {
      const res = await api.put('/users/me', { nickname, bio });
      setUser(res.data);
      setMsg('Profilo aggiornato!');
    } catch (err) {
      setError(err.response?.data?.error || 'Errore salvataggio');
    }
  };

  return (
    <div className="auth-box">
      <h2>Il mio Profilo ({user.role})</h2>
      <p className="muted">Email: {user.email}</p>
      {msg && <div className="alert-ok">{msg}</div>}
      {error && <div className="alert-error">{error}</div>}
      <form onSubmit={handleSave}>
        <label>Nickname</label>
        <input type="text" value={nickname} onChange={(e) => setNickname(e.target.value)} required />
        <label>Bio</label>
        <textarea rows={3} value={bio} onChange={(e) => setBio(e.target.value)} />
        <button type="submit" className="btn-primary">Salva Modifiche</button>
      </form>
    </div>
  );
}