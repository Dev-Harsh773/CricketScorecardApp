import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import './LiveScoring.css';

const LiveScoring = () => {
  const { matchKey } = useParams();
  const navigate = useNavigate();
  const [state, setState] = useState(null);
  const [match, setMatch] = useState(null);
  const [players, setPlayers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Dialog states
  const [showWicketDialog, setShowWicketDialog] = useState(false);
  const [showBowlerDialog, setShowBowlerDialog] = useState(false);
  const [showBatsmanDialog, setShowBatsmanDialog] = useState(false);
  const [showByeDialog, setShowByeDialog] = useState(false);
  const [byeType, setByeType] = useState('BYE');
  const [byeRuns, setByeRuns] = useState(0);

  // Wicket form
  const [wicket, setWicket] = useState({ wicketType: 'BOWLED', batsmanOutId: '', fielderName: '', runsBeforeDismissal: 0 });
  const [nextBowlerId, setNextBowlerId] = useState('');
  const [bowlingSide, setBowlingSide] = useState('OVER_THE_WICKET');
  const [nextBatsmanId, setNextBatsmanId] = useState('');

  const loadState = useCallback(async () => {
    try {
      const [matchRes, scorecardRes, playersRes] = await Promise.all([
        api.get(`/matches/${matchKey}`),
        api.get(`/matches/${matchKey}/scorecard`),
        api.get(`/matches/${matchKey}/players`),
      ]);
      setMatch(matchRes.data);
      setState(scorecardRes.data.liveState);
      setPlayers(playersRes.data);
    } catch (e) {
      setError('Failed to load match data');
    } finally {
      setLoading(false);
    }
  }, [matchKey]);

  useEffect(() => { loadState(); }, [loadState]);

  // Check if needs bowler or batsman after loading
  useEffect(() => {
    if (state?.inningsState === 'NEEDS_BOWLER') setShowBowlerDialog(true);
    if (state?.inningsState === 'NEEDS_BATSMAN') setShowBatsmanDialog(true);
    if (state?.inningsState === 'NEEDS_OPENERS') navigate(`/match/${matchKey}/players`);
  }, [state?.inningsState, matchKey, navigate]);

  const recordBall = async (runs, opts = {}) => {
    setError('');
    try {
      await api.post(`/matches/${matchKey}/ball`, { runsScored: runs, ...opts });
      await loadState();
    } catch (e) {
      setError(e.response?.data || e.message);
    }
  };

  const recordWide = () => recordBall(0, { isWide: true, extraRuns: 0 });
  const recordNoBall = () => recordBall(0, { isNoBall: true, runsScored: 0 });
  const handleByeSubmit = () => { setShowByeDialog(false); recordBall(0, { isBye: byeType === 'BYE', isLegBye: byeType === 'LEG_BYE', extraRuns: byeRuns }); };

  const submitWicket = async () => {
    if (!wicket.batsmanOutId) { setError('Select batsman out'); return; }
    try {
      await api.post(`/matches/${matchKey}/wicket`, {
        ...wicket,
        batsmanOutId: parseInt(wicket.batsmanOutId),
        runsBeforeDismissal: parseInt(wicket.runsBeforeDismissal || 0),
      });
      setShowWicketDialog(false);
      await loadState();
    } catch (e) {
      setError(e.response?.data || e.message);
    }
  };

  const submitNextBowler = async () => {
    if (!nextBowlerId) { setError('Select a bowler'); return; }
    try {
      await api.post(`/matches/${matchKey}/next-bowler`, { nextBowlerId: parseInt(nextBowlerId), bowlingSide });
      setShowBowlerDialog(false);
      await loadState();
    } catch (e) {
      setError(e.response?.data || e.message);
    }
  };

  const submitNextBatsman = async () => {
    if (!nextBatsmanId) { setError('Select a batsman'); return; }
    try {
      await api.post(`/matches/${matchKey}/next-batsman`, { nextBatsmanId: parseInt(nextBatsmanId) });
      setShowBatsmanDialog(false);
      await loadState();
    } catch (e) {
      setError(e.response?.data || e.message);
    }
  };

  const handleSecondInnings = () => navigate(`/match/${matchKey}/players`);

  if (loading) return <div className="page-container"><div className="spinner" style={{ marginTop: '5rem' }}></div></div>;

  const inningsState = state?.inningsState;
  const isMatchOver = match?.status === 'COMPLETED';
  const isInningsBreak = match?.status === 'INNINGS_BREAK';
  const canScore = !isMatchOver && inningsState === 'IN_PROGRESS';

  const battingTeamPlayers = players.filter(p => p.teamName === state?.battingTeam);
  const bowlingTeamPlayers = players.filter(p => p.teamName === state?.bowlingTeam);
  const activeBatsmen = [state?.striker?.playerId, state?.nonStriker?.playerId].filter(Boolean);
  const remainingBatsmen = battingTeamPlayers.filter(p => !activeBatsmen.includes(p.id));

  return (
    <div className="live-scoring-page">
      {/* TOP BAR */}
      <div className="score-topbar glass-card">
        <div className="score-main">
          <span className="score-team">{state?.battingTeam}</span>
          <span className="score-runs">{state?.totalRuns}/{state?.totalWickets}</span>
          <span className="score-overs">({state?.oversDisplay} Ov)</span>
        </div>
        <div className="score-stats">
          <div className="stat-item">
            <span className="stat-label">RR</span>
            <span className="stat-val">{state?.currentRunRate?.toFixed(2)}</span>
          </div>
          {state?.inningsNumber === 1 ? (
            <div className="stat-item">
              <span className="stat-label">Proj.</span>
              <span className="stat-val">{Math.round(state?.projectedScore)}</span>
            </div>
          ) : (
            <>
              <div className="stat-item">
                <span className="stat-label">Target</span>
                <span className="stat-val target-val">{state?.target}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Need</span>
                <span className="stat-val">{state?.target - state?.totalRuns} off {match?.totalOvers * 6 - state?.legalBallsBowled}b</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">RRR</span>
                <span className="stat-val">{state?.requiredRunRate?.toFixed(2)}</span>
              </div>
            </>
          )}
          <div className="match-key-badge">🔑 {matchKey}</div>
        </div>
      </div>

      {state?.isFreehit && (
        <div className="freehit-banner">⚡ FREE HIT — Batsman cannot be out bowled/lbw/hit wicket!</div>
      )}

      {/* PLAYERS PANEL */}
      <div className="players-panel">
        <div className="batsmen-panel glass-card">
          <div className="panel-title">Batting</div>
          {[state?.striker, state?.nonStriker].filter(Boolean).map((b, i) => (
            <div key={i} className={`player-card ${b.isStriker ? 'striker' : ''}`}>
              <div className="player-card-name">
                {b.isStriker && <span className="strike-marker">*</span>} {b.name}
              </div>
              <div className="player-card-stats">
                <span className="stat-runs">{b.runs}</span>
                <span className="stat-sep">({b.balls})</span>
                <span className="stat-small">4s: {b.fours}</span>
                <span className="stat-small">6s: {b.sixes}</span>
                <span className="stat-small">SR: {b.strikeRate?.toFixed(1)}</span>
              </div>
            </div>
          ))}
        </div>

        <div className="bowler-panel glass-card">
          <div className="panel-title">Bowling</div>
          {state?.currentBowler && (
            <div className="player-card">
              <div className="player-card-name">{state.currentBowler.name}</div>
              <div className="player-card-stats">
                <span className="stat-runs">{state.currentBowler.wickets}/{state.currentBowler.runsConceded}</span>
                <span className="stat-sep">({state.currentBowler.oversDisplay})</span>
                <span className="stat-small">Eco: {state.currentBowler.economy?.toFixed(2)}</span>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* RECENT BALLS */}
      <div className="recent-balls glass-card">
        <span className="rb-label">This Over:</span>
        {(state?.recentBalls || []).map((b, i) => (
          <span key={i} className={`ball-bubble ${b === 'W' ? 'ball-w' : b === '4' ? 'ball-4' : b === '6' ? 'ball-6' : b === 'Wd' || b === 'Nb' ? 'ball-extra' : ''}`}>{b}</span>
        ))}
        {!state?.recentBalls?.length && <span className="rb-empty">—</span>}
      </div>

      {error && <div className="error-msg" style={{ margin: '0.5rem 0' }}>{error}</div>}

      {/* SCORING BUTTONS */}
      {canScore && (
        <div className="scoring-panel glass-card">
          <div className="run-buttons">
            {[0, 1, 2, 3, 4, 6].map(r => (
              <button key={r} id={`run-${r}`} className={`run-btn ${r === 4 ? 'run-4' : r === 6 ? 'run-6' : ''}`} onClick={() => recordBall(r)}>
                {r}
              </button>
            ))}
          </div>
          <div className="extra-buttons">
            <button id="btn-wide" className="extra-btn" onClick={recordWide}>Wide</button>
            <button id="btn-noball" className="extra-btn" onClick={recordNoBall}>No Ball</button>
            <button id="btn-bye" className="extra-btn" onClick={() => { setByeType('BYE'); setByeRuns(1); setShowByeDialog(true); }}>Bye</button>
            <button id="btn-legbye" className="extra-btn" onClick={() => { setByeType('LEG_BYE'); setByeRuns(1); setShowByeDialog(true); }}>Leg Bye</button>
          </div>
          <button id="btn-wicket" className="wicket-btn" onClick={() => { setWicket({ wicketType: 'BOWLED', batsmanOutId: state?.striker?.playerId || '', fielderName: '', runsBeforeDismissal: 0 }); setShowWicketDialog(true); }}>
            🔴 WICKET
          </button>
        </div>
      )}

      {isMatchOver && (
        <div className="match-over-banner glass-card">
          <div className="mo-icon">🏆</div>
          <div className="mo-result">{match.result}</div>
          <div className="mo-actions">
            <button className="btn btn-primary" onClick={() => navigate(`/past/${matchKey}`)}>View Full Scorecard</button>
          </div>
        </div>
      )}

      {isInningsBreak && (
        <div className="innings-break-banner glass-card">
          <div className="mo-icon">☕</div>
          <div className="mo-result">Innings Break — {state?.battingTeam} scored {state?.totalRuns}/{state?.totalWickets}</div>
          <div className="mo-actions">
            <button className="btn btn-primary" onClick={handleSecondInnings}>Start 2nd Innings →</button>
          </div>
        </div>
      )}

      {/* WICKET DIALOG */}
      {showWicketDialog && (
        <div className="dialog-overlay">
          <div className="dialog-box">
            <h2 className="dialog-title">🔴 Record Wicket</h2>
            <div className="form-group">
              <label className="form-label">Dismissal Type</label>
              <select id="wicket-type" className="form-select" value={wicket.wicketType} onChange={e => setWicket({ ...wicket, wicketType: e.target.value })}>
                {['BOWLED','CAUGHT','LBW','RUN_OUT','STUMPED','HIT_WICKET','RETIRED_HURT','CAUGHT_AND_BOWLED'].map(t => (
                  <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Batsman Out</label>
              <select id="wicket-batsman" className="form-select" value={wicket.batsmanOutId} onChange={e => setWicket({ ...wicket, batsmanOutId: e.target.value })}>
                <option value="">Select batsman</option>
                {[state?.striker, state?.nonStriker].filter(Boolean).map(b => (
                  <option key={b.playerId} value={b.playerId}>{b.name}{b.isStriker ? ' *' : ''}</option>
                ))}
              </select>
            </div>
            {['CAUGHT','RUN_OUT','STUMPED','CAUGHT_AND_BOWLED'].includes(wicket.wicketType) && (
              <div className="form-group">
                <label className="form-label">Fielder Name</label>
                <input id="wicket-fielder" className="form-input" placeholder="Fielder name" value={wicket.fielderName} onChange={e => setWicket({ ...wicket, fielderName: e.target.value })} />
              </div>
            )}
            {wicket.wicketType === 'RUN_OUT' && (
              <div className="form-group">
                <label className="form-label">Runs completed before dismissal</label>
                <input id="wicket-runs" type="number" min="0" max="6" className="form-input" value={wicket.runsBeforeDismissal} onChange={e => setWicket({ ...wicket, runsBeforeDismissal: e.target.value })} />
              </div>
            )}
            {error && <div className="error-msg">{error}</div>}
            <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1.5rem' }}>
              <button id="wicket-submit" className="btn btn-danger" style={{ flex: 1 }} onClick={submitWicket}>Confirm Wicket</button>
              <button className="btn btn-outline" onClick={() => setShowWicketDialog(false)}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {/* NEXT BATSMAN DIALOG */}
      {showBatsmanDialog && (
        <div className="dialog-overlay">
          <div className="dialog-box">
            <h2 className="dialog-title">Select Next Batsman</h2>
            <div className="form-group">
              <label className="form-label">Incoming Batsman</label>
              <select id="next-batsman" className="form-select" value={nextBatsmanId} onChange={e => setNextBatsmanId(e.target.value)}>
                <option value="">Select batsman</option>
                {remainingBatsmen.map(p => <option key={p.id} value={p.id}>{p.playerName}</option>)}
              </select>
            </div>
            {error && <div className="error-msg">{error}</div>}
            <button id="next-batsman-submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem' }} onClick={submitNextBatsman}>
              Confirm →
            </button>
          </div>
        </div>
      )}

      {/* NEXT BOWLER DIALOG */}
      {showBowlerDialog && (
        <div className="dialog-overlay">
          <div className="dialog-box">
            <h2 className="dialog-title">Select Next Bowler</h2>
            <div className="form-group">
              <label className="form-label">Bowler</label>
              <select id="next-bowler" className="form-select" value={nextBowlerId} onChange={e => setNextBowlerId(e.target.value)}>
                <option value="">Select bowler</option>
                {bowlingTeamPlayers.map(p => <option key={p.id} value={p.id}>{p.playerName}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Bowling Side</label>
              <select id="next-bowler-side" className="form-select" value={bowlingSide} onChange={e => setBowlingSide(e.target.value)}>
                <option value="OVER_THE_WICKET">Over the Wicket</option>
                <option value="ROUND_THE_WICKET">Round the Wicket</option>
              </select>
            </div>
            {error && <div className="error-msg">{error}</div>}
            <button id="next-bowler-submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem' }} onClick={submitNextBowler}>
              Confirm →
            </button>
          </div>
        </div>
      )}

      {/* BYE DIALOG */}
      {showByeDialog && (
        <div className="dialog-overlay">
          <div className="dialog-box">
            <h2 className="dialog-title">{byeType === 'BYE' ? 'Bye' : 'Leg Bye'} Runs</h2>
            <div className="form-group">
              <label className="form-label">Runs</label>
              <select id="bye-runs" className="form-select" value={byeRuns} onChange={e => setByeRuns(parseInt(e.target.value))}>
                {[1,2,3,4,5,6].map(r => <option key={r} value={r}>{r}</option>)}
              </select>
            </div>
            <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1.5rem' }}>
              <button id="bye-submit" className="btn btn-primary" style={{ flex: 1 }} onClick={handleByeSubmit}>Confirm</button>
              <button className="btn btn-outline" onClick={() => setShowByeDialog(false)}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {/* Bottom actions */}
      <div className="bottom-actions">
        <button className="btn btn-outline" onClick={() => navigate(`/match/${matchKey}/scorecard`)}>📋 Scorecard</button>
      </div>
    </div>
  );
};

export default LiveScoring;
