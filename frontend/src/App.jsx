import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import ExplorePage from './pages/ExplorePage';
import { LoginPage, RegisterPage, ProfilePage } from './pages/AuthPages';

function Placeholder({ title }) {
  return <div className="page"><h2>{title}</h2><p>In arrivo nello Step 2...</p></div>;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <main className="container">
          <Routes>
            <Route path="/" element={<ExplorePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/missions/:id" element={<Placeholder title="Dettaglio Missione" />} />
            <Route path="/create-mission" element={<Placeholder title="Crea Missione" />} />
            <Route path="/dashboard" element={<Placeholder title="Area Personale" />} />
            <Route path="/admin" element={<Placeholder title="Pannello Moderazione" />} />
          </Routes>
        </main>
      </BrowserRouter>
    </AuthProvider>
  );
}