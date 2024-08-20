import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { connectToMatch, disconnectFromMatch } from '../services/websocket';
import api from '../services/api';
import './WatchLive.css';

const WatchLive = () => {
  const { matchKey } = useParams();
  const navigate = useNavigate();
  const [liveState, setLiveState] = useState(null);
  const [match, setMatch] = useState(null);
  const [connected, setConnected] = useState(false);
  const clientRef = useRef(null);

  useEffect(() => {
    // Load initial state via REST
    api.get(`/matches/${matchKey}/scorecard`).then(res => {
      setLiveState(res.data.liveState);
    }).catch(() => navigate('/enter-key'));

    api.get(`/matches/${matchKey}`).then(res => setMatch(res.data)).catch(console.error);

    // Connect WebSocket for live updates
    clientRef.current = connectToMatch(matchKey, (data) => {
      setLiveState(data);
      setConnected(true);
    });

    return () => {
      disconnectFromMatch();
    };
  }, [matchKey, navigate]);

  if (!liveState) return <div className="page-container"><div className="spinner" style={{ marginTop: '5rem' }}></div></div>;

  return (
    <div className="watch-live-page">
      <div className="wl-header">
        <div className="wl-title">
          🔴 LIVE — {match?.team1Name} vs {match?.team2Name}
        </div>
        <div className="wl-connection">
          <span className={`connection-dot ${connected ? 'connected' : ''}`}></span>
          {connected ? 'Live' : 'Connecting...'}
        </div>
      </div>

      <div className="score-topbar glass-card">
        <div className="score-main">
          <span className="score-team">{liveState.battingTeam}</span>
          <span className="score-runs">{liveState.totalRuns}/{liveState.totalWickets}</span>
          <span className="score-overs">({liveState.oversDisplay} Ov)</span>
        </div>
        <div className="score-stats">
          <div className="stat-item">
            <span className="stat-label">RR</span>
            <span className="stat-val">{liveState.currentRunRate?.toFixed(2)}</span>
          </div>
          {liveState.inningsNumber === 2 && liveState.target && (
            <>
              <div className="stat-item">
                <span className="stat-label">Target</span>
                <span className="stat-val target-val">{liveState.target}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Need</span>
                <span className="stat-val">{liveState.target - liveState.totalRuns} ({liveState.requiredRunRate?.toFixed(2)} RR)</span>
              </div>
            </>
          )}
          {liveState.inningsNumber === 1 && (
            <div className="stat-item">
              <span className="stat-label">Projected</span>
              <span className="stat-val">{Math.round(liveState.projectedScore)}</span>
            </div>
          )}
        </div>
      </div>

      {liveState.isFreehit && (
        <div className="freehit-banner">⚡ FREE HIT!</div>
      )}

      <div className="players-panel">
        <div className="batsmen-panel glass-card">
          <div className="panel-title">Batting</div>
          {[liveState.striker, liveState.nonStriker].filter(Boolean).map((b, i) => (
            <div key={i} className={`player-card ${b.isStriker ? 'striker' : ''}`}>
              <div className="player-card-name">
                {b.isStriker && <span className="strike-marker">*</span>} {b.name}
              </div>
              <div className="player-card-stats">
                <span className="stat-runs">{b.runs}</span>
                <span className="stat-sep">({b.balls})</span>
                <span className="stat-small">4s: {b.fours} 6s: {b.sixes} SR: {b.strikeRate?.toFixed(1)}</span>
              </div>
            </div>
          ))}
        </div>
        <div className="bowler-panel glass-card">
          <div className="panel-title">Bowling</div>
          {liveState.currentBowler && (
            <div className="player-card">
              <div className="player-card-name">{liveState.currentBowler.name}</div>
              <div className="player-card-stats">
                <span className="stat-runs">{liveState.currentBowler.wickets}/{liveState.currentBowler.runsConceded}</span>
                <span className="stat-sep">({liveState.currentBowler.oversDisplay})</span>
                <span className="stat-small">Eco: {liveState.currentBowler.economy?.toFixed(2)}</span>
              </div>
            </div>
          )}
        </div>
      </div>

      <div className="recent-balls glass-card">
        <span className="rb-label">This Over:</span>
        {(liveState.recentBalls || []).map((b, i) => (
          <span key={i} className={`ball-bubble ${b === 'W' ? 'ball-w' : b === '4' ? 'ball-4' : b === '6' ? 'ball-6' : b === 'Wd' || b === 'Nb' ? 'ball-extra' : ''}`}>{b}</span>
        ))}
      </div>

      {liveState.lastBallDescription && (
        <div className="last-ball glass-card">
          <span className="lb-label">Last ball:</span> {liveState.lastBallDescription}
        </div>
      )}

      <div className="wl-actions">
        <button className="btn btn-outline" onClick={() => navigate(`/match/${matchKey}/scorecard`)}>📋 Full Scorecard</button>
      </div>
    </div>
  );
};

export default WatchLive;
