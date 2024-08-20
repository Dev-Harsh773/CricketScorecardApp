import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import './MatchSetup.css';

const MatchSetup = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    team1Name: '', team2Name: '', totalOvers: 20,
    venue: '', matchDate: '', tossWinner: '', tossDecision: 'BAT'
  });
  const [matchKey, setMatchKey] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [copied, setCopied] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!form.team1Name || !form.team2Name) { setError('Both team names are required'); return; }
    if (!form.tossWinner) { setError('Please select toss winner'); return; }
    setLoading(true);
    try {
      const res = await api.post('/matches', form);
      setMatchKey(res.data.matchKey);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create match');
    } finally {
      setLoading(false);
    }
  };

  const copyKey = () => {
    navigator.clipboard.writeText(matchKey);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  if (matchKey) {
    return (
      <div className="page-container">
        <div className="match-key-reveal glass-card">
          <div className="key-icon">🎉</div>
          <h2>Match Created!</h2>
          <p className="key-label">Your unique match key is:</p>
          <div className="key-display">
            <span className="key-value">{matchKey}</span>
            <button className="btn btn-outline copy-btn" onClick={copyKey}>
              {copied ? '✅ Copied!' : '📋 Copy'}
            </button>
          </div>
          <p className="key-hint">Share this key with viewers so they can watch the live score.</p>
          <div className="key-actions">
            <button
              className="btn btn-primary"
              onClick={() => navigate(`/match/${matchKey}/players`)}
            >
              Add Players →
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <h1 className="page-title">🏏 Create New Match</h1>

      <div className="setup-form glass-card">
        <form onSubmit={handleSubmit}>
          <div className="form-grid-2">
            <div className="form-group">
              <label className="form-label">Team 1 Name *</label>
              <input id="team1" name="team1Name" className="form-input" placeholder="e.g. India" value={form.team1Name} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label className="form-label">Team 2 Name *</label>
              <input id="team2" name="team2Name" className="form-input" placeholder="e.g. Australia" value={form.team2Name} onChange={handleChange} required />
            </div>
          </div>

          <div className="form-grid-2">
            <div className="form-group">
              <label className="form-label">Total Overs *</label>
              <input id="overs" name="totalOvers" type="number" min="1" max="50" className="form-input" value={form.totalOvers} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label className="form-label">Venue (optional)</label>
              <input id="venue" name="venue" className="form-input" placeholder="e.g. Wankhede Stadium" value={form.venue} onChange={handleChange} />
            </div>
          </div>

          <div className="form-grid-2">
            <div className="form-group">
              <label className="form-label">Match Date (optional)</label>
              <input id="matchDate" name="matchDate" type="date" className="form-input" value={form.matchDate} onChange={handleChange} />
            </div>
          </div>

          <div className="toss-section">
            <h3 className="toss-title">🪙 Toss</h3>
            <div className="form-grid-2">
              <div className="form-group">
                <label className="form-label">Toss Winner *</label>
                <select id="tossWinner" name="tossWinner" className="form-select" value={form.tossWinner} onChange={handleChange} required>
                  <option value="">Select team</option>
                  {form.team1Name && <option value={form.team1Name}>{form.team1Name}</option>}
                  {form.team2Name && <option value={form.team2Name}>{form.team2Name}</option>}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Elected to</label>
                <div className="toss-toggle">
                  <button type="button" className={`toggle-btn ${form.tossDecision === 'BAT' ? 'active' : ''}`} onClick={() => setForm({ ...form, tossDecision: 'BAT' })}>🏏 Bat</button>
                  <button type="button" className={`toggle-btn ${form.tossDecision === 'BOWL' ? 'active' : ''}`} onClick={() => setForm({ ...form, tossDecision: 'BOWL' })}>🎯 Bowl</button>
                </div>
              </div>
            </div>
          </div>

          {error && <div className="error-msg">{error}</div>}

          <button id="create-match" type="submit" className="btn btn-primary submit-btn" disabled={loading}>
            {loading ? <span className="spinner-sm"></span> : 'Create Match & Get Key →'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default MatchSetup;
