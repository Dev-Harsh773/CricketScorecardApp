import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import './PlayerSetup.css';

const ROLES = ['BATSMAN', 'BOWLER', 'ALL_ROUNDER', 'WICKET_KEEPER'];
const BATTING_STYLES = ['RIGHT_HAND', 'LEFT_HAND'];
const BOWLING_STYLES = ['RIGHT_ARM_FAST', 'LEFT_ARM_FAST', 'RIGHT_ARM_OFF_SPIN', 'RIGHT_ARM_LEG_SPIN', 'LEFT_ARM_OFF_SPIN', 'LEFT_ARM_LEG_SPIN', 'RIGHT_ARM_MEDIUM', 'LEFT_ARM_MEDIUM', 'NOT_A_BOWLER'];

const emptyPlayer = { playerName: '', playerRole: 'BATSMAN', battingStyle: 'RIGHT_HAND', bowlingStyle: 'RIGHT_ARM_MEDIUM' };

const PlayerForm = ({ value, onChange, onAdd, teamName, count }) => (
  <div className="player-form">
    <input className="form-input" placeholder="Player name" value={value.playerName}
      onChange={e => onChange({ ...value, playerName: e.target.value })}
      onKeyDown={e => e.key === 'Enter' && onAdd()} />
    <select className="form-select" value={value.playerRole} onChange={e => onChange({ ...value, playerRole: e.target.value })}>
      {ROLES.map(r => <option key={r} value={r}>{r.replace('_', ' ')}</option>)}
    </select>
    <select className="form-select" value={value.battingStyle} onChange={e => onChange({ ...value, battingStyle: e.target.value })}>
      {BATTING_STYLES.map(s => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
    </select>
    <select className="form-select" value={value.bowlingStyle} onChange={e => onChange({ ...value, bowlingStyle: e.target.value })}>
      {BOWLING_STYLES.map(s => <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>)}
    </select>
    <button className="btn btn-success" onClick={onAdd} disabled={count >= 11}>Add</button>
  </div>
);

const TeamPanel = ({ team, players, value, onChange, onAdd, onRemove }) => (
  <div className="team-panel glass-card">
    <div className="team-header">
      <span className="team-name">{team}</span>
      <span className={`player-count ${players.length === 11 ? 'full' : ''}`}>{players.length}/11</span>
    </div>
    <PlayerForm value={value} onChange={onChange} onAdd={onAdd} teamName={team} count={players.length} />
    <div className="player-list">
      {players.map((p, i) => (
        <div key={i} className="player-item">
          <span className="player-num">{i + 1}</span>
          <div className="player-info">
            <span className="p-name">{p.playerName}</span>
            <span className="p-role">{p.playerRole} • {p.battingStyle.replace('_', ' ')}</span>
          </div>
          <button className="btn-remove" onClick={() => onRemove(i)}>✕</button>
        </div>
      ))}
    </div>
  </div>
);

const PlayerSetup = () => {
  const { matchKey } = useParams();
  const navigate = useNavigate();
  const [match, setMatch] = useState(null);
  const [team1Players, setTeam1Players] = useState([]);
  const [team2Players, setTeam2Players] = useState([]);
  const [newPlayer1, setNewPlayer1] = useState({ ...emptyPlayer });
  const [newPlayer2, setNewPlayer2] = useState({ ...emptyPlayer });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showStartDialog, setShowStartDialog] = useState(false);
  const [players, setPlayers] = useState([]);
  const [opener, setOpener] = useState({ strikerId: '', nonStrikerId: '', bowlerId: '', bowlingSide: 'OVER_THE_WICKET' });

  useEffect(() => {
    Promise.all([
      api.get(`/matches/${matchKey}`),
      api.get(`/matches/${matchKey}/players`)
    ]).then(([matchRes, playersRes]) => {
      setMatch(matchRes.data);
      if (playersRes.data && playersRes.data.length > 0) {
        setPlayers(playersRes.data);
        setShowStartDialog(true);
      }
    }).catch(() => navigate('/dashboard'));
  }, [matchKey, navigate]);

  const addPlayer = (team) => {
    const player = team === 1 ? newPlayer1 : newPlayer2;
    const list = team === 1 ? team1Players : team2Players;
    if (!player.playerName.trim()) return;
    if (list.length >= 11) { setError(`${team === 1 ? match.team1Name : match.team2Name} already has 11 players`); return; }
    if (list.some(p => p.playerName.toLowerCase() === player.playerName.toLowerCase())) { setError('Duplicate player name'); return; }
    setError('');
    if (team === 1) {
      setTeam1Players([...list, { ...player }]);
      setNewPlayer1({ ...emptyPlayer });
    } else {
      setTeam2Players([...list, { ...player }]);
      setNewPlayer2({ ...emptyPlayer });
    }
  };

  const removePlayer = (team, idx) => {
    if (team === 1) setTeam1Players(team1Players.filter((_, i) => i !== idx));
    else setTeam2Players(team2Players.filter((_, i) => i !== idx));
  };

  const submitPlayers = async () => {
    if (team1Players.length !== 11 || team2Players.length !== 11) {
      setError('Each team must have exactly 11 players'); return;
    }
    setLoading(true);
    try {
      const allPlayers = [
        ...team1Players.map(p => ({ ...p, teamName: match.team1Name })),
        ...team2Players.map(p => ({ ...p, teamName: match.team2Name }))
      ];
      await api.post(`/matches/${matchKey}/players`, allPlayers);
      
      const res = await api.get(`/matches/${matchKey}/players`);
      setPlayers(res.data);
      setShowStartDialog(true);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save players');
    } finally {
      setLoading(false);
    }
  };

  const startInnings = async () => {
    if (!opener.strikerId || !opener.nonStrikerId || !opener.bowlerId) { setError('Select all openers'); return; }
    if (opener.strikerId === opener.nonStrikerId) { setError('Striker and non-striker must be different'); return; }
    setLoading(true);
    try {
      await api.post(`/matches/${matchKey}/start`, {
        strikerId: parseInt(opener.strikerId),
        nonStrikerId: parseInt(opener.nonStrikerId),
        bowlerId: parseInt(opener.bowlerId),
        bowlingSide: opener.bowlingSide
      });
      navigate(`/match/${matchKey}/score`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to start match');
    } finally {
      setLoading(false);
    }
  };

  if (!match) return <div className="page-container"><div className="spinner"></div></div>;

  const isSecondInnings = match.status === 'INNINGS_BREAK' || match.status === 'SECOND_INNINGS';
  const currentBattingTeam = isSecondInnings ? match.bowlingFirstTeam : match.battingFirstTeam;
  const currentBowlingTeam = isSecondInnings ? match.battingFirstTeam : match.bowlingFirstTeam;
  const battingPlayers = players.filter(p => p.teamName === currentBattingTeam);
  const bowlingPlayers = players.filter(p => p.teamName === currentBowlingTeam);

  return (
    <div className="page-container">
      <h1 className="page-title">👥 Add Players</h1>
      <p className="setup-hint">{match.team1Name} vs {match.team2Name} • Match Key: <code>{matchKey}</code></p>
      {error && <div className="error-msg" style={{ marginBottom: '1rem' }}>{error}</div>}

      {players.length === 0 ? (
        <>
          <div className="teams-grid">
            <TeamPanel team={match.team1Name} players={team1Players}
              value={newPlayer1} onChange={setNewPlayer1}
              onAdd={() => addPlayer(1)} onRemove={(i) => removePlayer(1, i)} />
            <TeamPanel team={match.team2Name} players={team2Players}
              value={newPlayer2} onChange={setNewPlayer2}
              onAdd={() => addPlayer(2)} onRemove={(i) => removePlayer(2, i)} />
          </div>

          <div className="submit-row">
            <button className="btn btn-primary" style={{ padding: '0.9rem 2.5rem', fontSize: '1rem' }}
              onClick={submitPlayers} disabled={loading || team1Players.length !== 11 || team2Players.length !== 11}>
              {loading ? <span className="spinner-sm"></span> : 'Save Players & Select Openers →'}
            </button>
          </div>
        </>
      ) : (
        <div className="glass-card" style={{ padding: '3rem', textAlign: 'center', marginTop: '2rem' }}>
          <h2>Players already added!</h2>
          <p style={{ color: 'rgba(255,255,255,0.6)', marginTop: '0.5rem' }}>Select the openers for the {isSecondInnings ? 'second' : 'first'} innings.</p>
        </div>
      )}

      {/* Start dialog */}
      {showStartDialog && (
        <div className="dialog-overlay">
          <div className="dialog-box">
            <h2 className="dialog-title">🏏 Select Openers & Bowler</h2>
            <p style={{ color: 'rgba(255,255,255,0.5)', marginBottom: '1.5rem', fontSize: '0.9rem' }}>
              {currentBattingTeam} bats {isSecondInnings ? 'second' : 'first'}
            </p>
            <div className="form-group">
              <label className="form-label">Striker (Batsman on strike)</label>
              <select id="striker" className="form-select" value={opener.strikerId} onChange={e => setOpener({ ...opener, strikerId: e.target.value })}>
                <option value="">Select striker</option>
                {battingPlayers.map(p => <option key={p.id} value={p.id}>{p.playerName}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Non-Striker</label>
              <select id="nonStriker" className="form-select" value={opener.nonStrikerId} onChange={e => setOpener({ ...opener, nonStrikerId: e.target.value })}>
                <option value="">Select non-striker</option>
                {battingPlayers.map(p => <option key={p.id} value={p.id}>{p.playerName}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Opening Bowler ({currentBowlingTeam})</label>
              <select id="openBowler" className="form-select" value={opener.bowlerId} onChange={e => setOpener({ ...opener, bowlerId: e.target.value })}>
                <option value="">Select bowler</option>
                {bowlingPlayers.map(p => <option key={p.id} value={p.id}>{p.playerName}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Bowling Side</label>
              <select id="bowlingSide" className="form-select" value={opener.bowlingSide} onChange={e => setOpener({ ...opener, bowlingSide: e.target.value })}>
                <option value="OVER_THE_WICKET">Over the Wicket</option>
                <option value="ROUND_THE_WICKET">Round the Wicket</option>
              </select>
            </div>
            {error && <div className="error-msg">{error}</div>}
            <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem' }}>
              <button id="start-innings" className="btn btn-primary" style={{ flex: 1 }} onClick={startInnings} disabled={loading}>
                {loading ? <span className="spinner-sm"></span> : '🚀 Start Match!'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PlayerSetup;
