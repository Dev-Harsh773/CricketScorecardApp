import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import './Scorecard.css';

const Scorecard = () => {
  const { matchKey } = useParams();
  const navigate = useNavigate();
  const [scorecard, setScorecard] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get(`/matches/${matchKey}/scorecard`)
      .then(res => setScorecard(res.data))
      .catch(() => navigate('/dashboard'))
      .finally(() => setLoading(false));
  }, [matchKey, navigate]);

  if (loading) return <div className="page-container"><div className="spinner"></div></div>;

  return (
    <div className="page-container">
      <div className="scorecard-header glass-card">
        <h1 className="sc-title">{scorecard.team1Name} vs {scorecard.team2Name}</h1>
        <div className="sc-meta">
          {scorecard.venue && <span>📍 {scorecard.venue}</span>}
          {scorecard.matchDate && <span>📅 {scorecard.matchDate}</span>}
          <span>🔑 {matchKey}</span>
        </div>
        <div className="sc-status">
          {scorecard.status === 'COMPLETED' ? (
            <span className="sc-result">{scorecard.result}</span>
          ) : (
            <span className="sc-live">Live</span>
          )}
        </div>
      </div>

      <div className="innings-container">
        {scorecard.innings1 && <InningsView innings={scorecard.innings1} num={1} />}
        {scorecard.innings2 && <InningsView innings={scorecard.innings2} num={2} />}
      </div>
      
      {scorecard.status !== 'COMPLETED' && (
        <div style={{ textAlign: 'center', marginTop: '2rem' }}>
          <button className="btn btn-primary" onClick={() => navigate(`/match/${matchKey}/watch`)}>
            Watch Live →
          </button>
        </div>
      )}
    </div>
  );
};

const InningsView = ({ innings, num }) => (
  <div className="innings-card glass-card">
    <div className="innings-header">
      <h2 className="innings-title">Innings {num} — {innings.battingTeam}</h2>
      <div className="innings-score">
        <span className="sc-runs">{innings.totalRuns}/{innings.totalWickets}</span>
        <span className="sc-overs">({Math.floor(innings.legalBallsBowled/6)}.{innings.legalBallsBowled%6} Ov)</span>
      </div>
    </div>

    <h3 className="table-title">Batting</h3>
    <div className="table-responsive">
      <table className="scorecard-table">
        <thead>
          <tr>
            <th>Batsman</th>
            <th></th>
            <th>R</th>
            <th>B</th>
            <th>4s</th>
            <th>6s</th>
            <th>SR</th>
          </tr>
        </thead>
        <tbody>
          {innings.batting?.map(b => (
            <tr key={b.playerId}>
              <td><strong>{b.name}</strong></td>
              <td style={{ fontSize: '0.85em', color: 'rgba(255,255,255,0.6)' }}>{b.dismissalInfo}</td>
              <td>{b.runs}</td>
              <td>{b.balls}</td>
              <td>{b.fours}</td>
              <td>{b.sixes}</td>
              <td>{b.strikeRate?.toFixed(1)}</td>
            </tr>
          ))}
          <tr className="extras-row">
            <td><strong>Extras</strong></td>
            <td style={{ fontSize: '0.85em', color: 'rgba(255,255,255,0.6)' }}>
              (W {innings.wides}, NB {innings.noBalls}, B {innings.byes}, LB {innings.legByes})
            </td>
            <td colSpan="5"><strong>{innings.extrasTotal}</strong></td>
          </tr>
        </tbody>
      </table>
    </div>

    <h3 className="table-title" style={{ marginTop: '2rem' }}>Bowling</h3>
    <div className="table-responsive">
      <table className="scorecard-table">
        <thead>
          <tr>
            <th>Bowler</th>
            <th>O</th>
            <th>M</th>
            <th>R</th>
            <th>W</th>
            <th>Econ</th>
          </tr>
        </thead>
        <tbody>
          {innings.bowling?.map(b => (
            <tr key={b.playerId}>
              <td><strong>{b.name}</strong></td>
              <td>{b.oversDisplay}</td>
              <td>{b.maidens}</td>
              <td>{b.runsConceded}</td>
              <td><strong>{b.wickets}</strong></td>
              <td>{b.economy?.toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  </div>
);

export default Scorecard;
