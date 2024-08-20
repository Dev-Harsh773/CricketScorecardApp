import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import './Dashboard.css';

const Dashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [myMatches, setMyMatches] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/matches/my').then(res => {
      setMyMatches(res.data);
    }).catch(console.error).finally(() => setLoading(false));
  }, []);

  const inProgress = myMatches.filter(m => m.status === 'IN_PROGRESS' || m.status === 'SECOND_INNINGS' || m.status === 'INNINGS_BREAK');

  const getStatusBadge = (status) => {
    if (status === 'COMPLETED') return <span className="score-badge badge-green">Completed</span>;
    if (status === 'IN_PROGRESS' || status === 'SECOND_INNINGS') return <span className="score-badge badge-orange">Live 🔴</span>;
    return <span className="score-badge badge-blue">Setup</span>;
  };

  return (
    <div className="page-container">
      <div className="dashboard-welcome">
        <div className="welcome-text">
          <h1 className="page-title">Welcome back, {user?.username} 👋</h1>
          <p className="welcome-sub">Manage your matches and track live scores</p>
        </div>
      </div>

      {/* Main action cards */}
      <div className="dashboard-cards">
        <div className="dash-card dash-card-new" onClick={() => navigate('/match/new')}>
          <div className="dash-card-icon">🏏</div>
          <div className="dash-card-title">Create New Match</div>
          <div className="dash-card-desc">Set up teams, players and start scoring</div>
        </div>
        <div className="dash-card dash-card-key" onClick={() => navigate('/enter-key')}>
          <div className="dash-card-icon">🔑</div>
          <div className="dash-card-title">Enter Match Key</div>
          <div className="dash-card-desc">Watch a live match or view a completed scorecard</div>
        </div>
        <div className="dash-card dash-card-past" onClick={() => navigate('/past-matches')}>
          <div className="dash-card-icon">📋</div>
          <div className="dash-card-title">Past Matches</div>
          <div className="dash-card-desc">View completed match scorecards and download PDFs</div>
        </div>
        <div className="dash-card dash-card-analytics" onClick={() => navigate('/analytics')}>
          <div className="dash-card-icon">📊</div>
          <div className="dash-card-title">Analytics</div>
          <div className="dash-card-desc">Charts, run rates, top scorers and bowlers</div>
        </div>
      </div>

      {/* In-progress matches */}
      {inProgress.length > 0 && (
        <div className="section-block">
          <h2 className="section-title">🔴 Matches In Progress</h2>
          <div className="match-list">
            {inProgress.map(m => (
              <div key={m.id} className="match-row glass-card">
                <div className="match-row-info">
                  <div className="match-teams">{m.team1Name} <span>vs</span> {m.team2Name}</div>
                  <div className="match-meta">{m.totalOvers} Overs{m.venue ? ` • ${m.venue}` : ''}</div>
                </div>
                {getStatusBadge(m.status)}
                <button className="btn btn-primary" onClick={() => navigate(`/match/${m.matchKey}/score`)}>
                  Continue Scoring →
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Recent matches */}
      {!loading && myMatches.length > 0 && (
        <div className="section-block">
          <h2 className="section-title">📂 My Matches</h2>
          <div className="match-list">
            {myMatches.map(m => (
              <div key={m.id} className="match-row glass-card">
                <div className="match-row-info">
                  <div className="match-teams">{m.team1Name} <span>vs</span> {m.team2Name}</div>
                  <div className="match-meta">
                    Key: <code>{m.matchKey}</code>
                    {m.matchDate ? ` • ${m.matchDate}` : ''}
                  </div>
                  {m.result && <div className="match-result">{m.result}</div>}
                </div>
                {getStatusBadge(m.status)}
                <div className="match-row-actions">
                  {m.status === 'COMPLETED' && (
                    <button className="btn btn-outline" onClick={() => navigate(`/past/${m.matchKey}`)}>
                      View Scorecard
                    </button>
                  )}
                  {(m.status === 'IN_PROGRESS' || m.status === 'SECOND_INNINGS') && (
                    <button className="btn btn-primary" onClick={() => navigate(`/match/${m.matchKey}/score`)}>
                      Score →
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {!loading && myMatches.length === 0 && (
        <div className="empty-state glass-card">
          <div className="empty-icon">🏏</div>
          <div className="empty-text">No matches yet. Create your first match!</div>
          <button className="btn btn-primary" onClick={() => navigate('/match/new')}>Create Match</button>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
