import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import LoginPage from './pages/LoginPage';
import Dashboard from './pages/Dashboard';
import MatchSetup from './pages/MatchSetup';
import PlayerSetup from './pages/PlayerSetup';
import LiveScoring from './pages/LiveScoring';
import WatchLive from './pages/WatchLive';
import EnterMatchKey from './pages/EnterMatchKey';
import Scorecard from './pages/Scorecard';
import PastMatches from './pages/PastMatches';
import PastScorecard from './pages/PastScorecard';
import Analytics from './pages/Analytics';
import './index.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route
            path="/*"
            element={
              <ProtectedRoute>
                <Navbar />
                <Routes>
                  <Route path="/dashboard" element={<Dashboard />} />
                  <Route path="/match/new" element={<MatchSetup />} />
                  <Route path="/match/:matchKey/players" element={<PlayerSetup />} />
                  <Route path="/match/:matchKey/score" element={<LiveScoring />} />
                  <Route path="/match/:matchKey/watch" element={<WatchLive />} />
                  <Route path="/match/:matchKey/scorecard" element={<Scorecard />} />
                  <Route path="/enter-key" element={<EnterMatchKey />} />
                  <Route path="/past-matches" element={<PastMatches />} />
                  <Route path="/past/:matchKey" element={<PastScorecard />} />
                  <Route path="/analytics" element={<Analytics />} />
                </Routes>
              </ProtectedRoute>
            }
          />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
