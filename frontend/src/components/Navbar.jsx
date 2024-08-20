import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/dashboard">
          <span className="brand-icon">🏏</span>
          <span className="brand-text">CricketScorer</span>
        </Link>
      </div>
      {user && (
        <div className="navbar-links">
          <Link to="/dashboard" className="nav-link">Dashboard</Link>
          <Link to="/match/new" className="nav-link">New Match</Link>
          <Link to="/enter-key" className="nav-link">Enter Key</Link>
          <Link to="/past-matches" className="nav-link">Past Matches</Link>
          <Link to="/analytics" className="nav-link">Analytics</Link>
          <div className="nav-user">
            <span className="username">👤 {user.username}</span>
            <button className="btn-logout" onClick={handleLogout}>Logout</button>
          </div>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
