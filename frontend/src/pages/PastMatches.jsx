import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const PastMatches = () => {
  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    api.get('/matches/my')
      .then(res => setMatches(res.data.filter(m => m.status === 'COMPLETED')))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="page-container"><div className="spinner" style={{ marginTop: '5rem' }}></div></div>;

  return (
    <div className="page-container">
      <h1 className="page-title">📋 Past Matches</h1>

      {matches.length === 0 ? (
        <div className="empty-state glass-card">
          <div className="empty-icon">🤷</div>
          <div className="empty-text">No completed matches found.</div>
        </div>
      ) : (
        <div className="match-list">
          {matches.map(m => (
            <div key={m.id} className="match-row glass-card">
              <div className="match-row-info">
                <div className="match-teams">{m.team1Name} <span>vs</span> {m.team2Name}</div>
                <div className="match-meta">Key: <code>{m.matchKey}</code></div>
                <div className="match-result" style={{ marginTop: '0.5rem', fontWeight: 600 }}>{m.result}</div>
              </div>
              <div className="match-row-actions">
                <button className="btn btn-primary" onClick={() => navigate(`/past/${m.matchKey}`)}>
                  View Scorecard
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default PastMatches;
