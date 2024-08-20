import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import './EnterMatchKey.css';

const EnterMatchKey = () => {
  const [matchKey, setMatchKey] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!matchKey.trim()) return;
    setLoading(true);
    setError('');

    try {
      const res = await api.get(`/matches/${matchKey.trim()}`);
      if (res.data.status === 'COMPLETED') {
        navigate(`/past/${matchKey.trim()}`);
      } else {
        navigate(`/match/${matchKey.trim()}/watch`);
      }
    } catch (err) {
      setError('Match not found or invalid key');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container" style={{ display: 'flex', justifyContent: 'center', marginTop: '4rem' }}>
      <div className="enter-key-card glass-card">
        <div className="key-icon">🔑</div>
        <h2 className="dialog-title" style={{ textAlign: 'center' }}>Enter Match Key</h2>
        <p className="key-hint" style={{ textAlign: 'center', marginBottom: '2rem' }}>
          Enter a match key to watch live or view a scorecard.
        </p>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <input
              type="text"
              className="form-input"
              placeholder="e.g. IND-AUS-8H2K"
              value={matchKey}
              onChange={e => setMatchKey(e.target.value.toUpperCase())}
              style={{ textAlign: 'center', fontSize: '1.2rem', letterSpacing: '2px', padding: '1rem' }}
              autoFocus
            />
          </div>
          {error && <div className="error-msg" style={{ textAlign: 'center' }}>{error}</div>}
          <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '1rem', marginTop: '1rem' }} disabled={loading}>
            {loading ? <span className="spinner-sm"></span> : 'Go to Match →'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default EnterMatchKey;
