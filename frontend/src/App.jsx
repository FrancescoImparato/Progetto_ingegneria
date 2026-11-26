import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import ExplorePage from './pages/ExplorePage';
import { LoginPage, RegisterPage, ProfilePage } from './pages/AuthPages';
import CreateMissionPage from './pages/CreateMissionPage';
import MissionDetailPage from './pages/MissionDetailPage';
import ParticipationPage from './pages/ParticipationPage';
import DashboardPage from './pages/DashboardPage';
import AdminPage from './pages/AdminPage';

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
            <Route path="/missions/:id" element={<MissionDetailPage />} />
            <Route path="/create-mission" element={<CreateMissionPage />} />
            <Route path="/edit-mission/:id" element={<CreateMissionPage />} />
            <Route path="/participations/:id" element={<ParticipationPage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/admin" element={<AdminPage />} />
          </Routes>
        </main>
      </BrowserRouter>
    </AuthProvider>
  );
}