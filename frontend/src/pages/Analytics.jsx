import React, { useState, useEffect } from 'react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import api from '../services/api';
import './Analytics.css';

const Analytics = () => {
  const [topScorers, setTopScorers] = useState([]);
  const [topBowlers, setTopBowlers] = useState([]);
  const [matches, setMatches] = useState([]);
  
  // 'ALL' means all time for tables. For charts, it shows nothing.
  const [selectedMatch, setSelectedMatch] = useState('ALL');
  const [manualKey, setManualKey] = useState('');
  
  const [runRateData, setRunRateData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Initial load: get my matches
    api.get('/matches/my').then(res => {
      const compMatches = res.data.filter(m => m.status === 'COMPLETED');
      setMatches(compMatches);
      setLoading(false);
    }).catch(e => {
      console.error(e);
      setLoading(false);
    });
  }, []);

  useEffect(() => {
    if (selectedMatch === 'ALL') {
      Promise.all([
        api.get('/analytics/top-scorers'),
        api.get('/analytics/top-bowlers')
      ]).then(([sRes, bRes]) => {
        setTopScorers(sRes.data);
        setTopBowlers(bRes.data);
        setRunRateData([]); // no chart for all time
      }).catch(console.error);
    } else if (selectedMatch) {
      Promise.all([
        api.get(`/analytics/${selectedMatch}/top-scorers`),
        api.get(`/analytics/${selectedMatch}/top-bowlers`),
        api.get(`/analytics/${selectedMatch}/run-rate`)
      ]).then(([sRes, bRes, rRes]) => {
        setTopScorers(sRes.data);
        setTopBowlers(bRes.data);
        
        // Format for Recharts
        const formatted = [];
        rRes.data.forEach(d => {
          let item = formatted.find(f => f.over === d.over);
          if (!item) {
            item = { over: d.over };
            formatted.push(item);
          }
          if (d.innings === 1) item.inn1 = d.runRate;
          if (d.innings === 2) item.inn2 = d.runRate;
        });
        setRunRateData(formatted);
      }).catch(err => {
        console.error(err);
        // If error, maybe clear them
        setTopScorers([]);
        setTopBowlers([]);
        setRunRateData([]);
      });
    }
  }, [selectedMatch]);

  const handleManualSearch = () => {
    if (manualKey.trim()) {
      setSelectedMatch(manualKey.trim());
    }
  };

  if (loading) return <div className="page-container"><div className="spinner"></div></div>;

  return (
    <div className="page-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <h1 className="page-title" style={{ marginBottom: 0 }}>📊 Analytics</h1>
        
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap' }}>
          <select className="form-select" style={{ width: 'auto', minWidth: '200px', margin: 0 }} 
            value={selectedMatch} 
            onChange={e => setSelectedMatch(e.target.value)}>
            <option value="ALL">All Time (All Matches)</option>
            {matches.map(m => (
              <option key={m.matchKey} value={m.matchKey}>{m.team1Name} vs {m.team2Name} ({m.matchKey})</option>
            ))}
          </select>
          
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <input className="form-input" style={{ width: '150px', margin: 0 }} placeholder="Match Key..." 
              value={manualKey} onChange={e => setManualKey(e.target.value)} 
              onKeyDown={e => e.key === 'Enter' && handleManualSearch()} />
            <button className="btn btn-primary" onClick={handleManualSearch}>Load</button>
          </div>
        </div>
      </div>

      <div className="analytics-grid">
        <div className="analytics-card glass-card">
          <h2 className="card-title">{selectedMatch === 'ALL' ? 'Top 10 Run Scorers (All Time)' : 'Top Run Scorers (Match)'}</h2>
          <table className="scorecard-table">
            <thead>
              <tr><th>Player</th><th>Runs</th><th>Balls</th><th>SR</th></tr>
            </thead>
            <tbody>
              {topScorers.map((s, i) => (
                <tr key={i}>
                  <td>{s.playerName}</td>
                  <td><strong>{s.runs}</strong></td>
                  <td>{s.balls}</td>
                  <td>{(s.runs * 100 / (s.balls || 1)).toFixed(1)}</td>
                </tr>
              ))}
              {topScorers.length === 0 && <tr><td colSpan="4" className="empty-cell">No data yet</td></tr>}
            </tbody>
          </table>
        </div>

        <div className="analytics-card glass-card">
          <h2 className="card-title">{selectedMatch === 'ALL' ? 'Top 10 Wicket Takers (All Time)' : 'Top Wicket Takers (Match)'}</h2>
          <table className="scorecard-table">
            <thead>
              <tr><th>Player</th><th>W</th><th>R</th><th>M</th></tr>
            </thead>
            <tbody>
              {topBowlers.map((b, i) => (
                <tr key={i}>
                  <td>{b.playerName}</td>
                  <td><strong>{b.wickets}</strong></td>
                  <td>{b.runs}</td>
                  <td>{b.maidens}</td>
                </tr>
              ))}
              {topBowlers.length === 0 && <tr><td colSpan="4" className="empty-cell">No data yet</td></tr>}
            </tbody>
          </table>
        </div>
      </div>

      {selectedMatch !== 'ALL' && (
        <div className="analytics-card glass-card" style={{ marginTop: '2rem' }}>
          <h2 className="card-title">Run Rate Progression</h2>
          {runRateData.length > 0 ? (
            <div style={{ width: '100%', height: 400 }}>
              <ResponsiveContainer>
                <AreaChart data={runRateData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                  <defs>
                    <linearGradient id="color1" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#7b2ff7" stopOpacity={0.8}/>
                      <stop offset="95%" stopColor="#7b2ff7" stopOpacity={0}/>
                    </linearGradient>
                    <linearGradient id="color2" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#00d2ff" stopOpacity={0.8}/>
                      <stop offset="95%" stopColor="#00d2ff" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <XAxis dataKey="over" stroke="#fff" opacity={0.5} tickMargin={10} />
                  <YAxis stroke="#fff" opacity={0.5} tickMargin={10} />
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
                  <Tooltip contentStyle={{ backgroundColor: '#1a1a2e', borderColor: 'rgba(255,255,255,0.1)', color: '#fff', borderRadius: '8px' }} />
                  <Area type="monotone" dataKey="inn1" name="Innings 1 RR" stroke="#7b2ff7" strokeWidth={3} fillOpacity={1} fill="url(#color1)" />
                  <Area type="monotone" dataKey="inn2" name="Innings 2 RR" stroke="#00d2ff" strokeWidth={3} fillOpacity={1} fill="url(#color2)" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="empty-state" style={{ padding: '3rem' }}>
              <div className="empty-text">No data available for this match</div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default Analytics;
