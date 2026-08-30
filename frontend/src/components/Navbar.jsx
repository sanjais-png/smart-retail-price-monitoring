import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { ShieldCheck, LogOut, Server, User, Sun, Moon } from 'lucide-react';

export const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  // Dark Mode state initialized from localStorage
  const [isDarkMode, setIsDarkMode] = useState(() => {
    return localStorage.getItem('theme') === 'dark';
  });

  useEffect(() => {
    if (isDarkMode) {
      document.body.classList.add('dark-mode');
      localStorage.setItem('theme', 'dark');
    } else {
      document.body.classList.remove('dark-mode');
      localStorage.setItem('theme', 'light');
    }
  }, [isDarkMode]);

  const toggleDarkMode = () => {
    setIsDarkMode(prev => !prev);
  };

  const getRoleLabel = (role, approvalStatus) => {
    if (approvalStatus === 'PENDING_AUTHORITY') {
      return { label: 'PENDING AUTHORITY (REVIEW IN PROGRESS)', bg: '#fffbebfb', color: '#b45309', border: '#fde68a' };
    }
    switch (role) {
      case 'ROLE_ADMIN':
        return { label: 'PLATFORM ADMIN', bg: '#faf5ff', color: '#7c3aed', border: '#ddd6fe' };
      case 'ROLE_AUTHORITY':
        return { label: 'GOVT AUTHORITY', bg: '#fef2f2', color: '#b91c1c', border: '#fecaca' };
      default:
        return { label: 'CONSUMER', bg: '#f0fdf4', color: '#15803d', border: '#bbf7d0' };
    }
  };

  const roleStyle = getRoleLabel(user?.role, user?.approvalStatus);

  return (
    <header style={{
      height: '70px',
      backgroundColor: isDarkMode ? '#1e293b' : '#ffffff',
      borderBottom: isDarkMode ? '1px solid #334155' : '1px solid #e2e8f0',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 32px',
      position: 'sticky',
      top: 0,
      zIndex: 10,
      transition: 'background-color 0.3s ease'
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          padding: '6px 12px',
          borderRadius: '20px',
          backgroundColor: isDarkMode ? '#0f172a' : '#f8fafc',
          border: isDarkMode ? '1px solid #334155' : '1px solid #e2e8f0',
          fontSize: '13px',
          color: isDarkMode ? '#94a3b8' : '#475569'
        }}>
          <span style={{
            width: '8px',
            height: '8px',
            borderRadius: '50%',
            backgroundColor: '#22c55e',
            display: 'inline-block',
            boxShadow: '0 0 8px #22c55e'
          }} />
          <Server size={14} color="#10b981" />
          <span>API Gateway (8090) Connected</span>
        </div>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        
        {/* Dark Mode Toggle Switch */}
        <button
          onClick={toggleDarkMode}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            padding: '8px 14px',
            borderRadius: '20px',
            backgroundColor: isDarkMode ? '#0f172a' : '#f1f5f9',
            border: isDarkMode ? '1px solid #334155' : '1px solid #cbd5e1',
            color: isDarkMode ? '#f59e0b' : '#0f172a',
            fontWeight: 700,
            fontSize: '12px',
            cursor: 'pointer',
            transition: 'all 0.2s ease'
          }}
          title="Toggle Light / Dark Mode"
        >
          {isDarkMode ? <Sun size={16} color="#f59e0b" /> : <Moon size={16} color="#475569" />}
          <span>{isDarkMode ? 'Light Mode' : 'Dark Mode'}</span>
        </button>

        {/* Role Badge */}
        <div style={{
          padding: '4px 12px',
          borderRadius: '16px',
          backgroundColor: roleStyle.bg,
          color: roleStyle.color,
          border: `1px solid ${roleStyle.border}`,
          fontSize: '12px',
          fontWeight: 700,
          letterSpacing: '0.05em',
          display: 'flex',
          alignItems: 'center',
          gap: '6px'
        }}>
          <ShieldCheck size={14} />
          {roleStyle.label}
        </div>

        {/* User Profile Link */}
        <Link
          to="/profile"
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            textDecoration: 'none',
            padding: '4px 8px',
            borderRadius: '10px',
            transition: 'background-color 0.2s ease'
          }}
        >
          <div style={{
            width: '38px',
            height: '38px',
            borderRadius: '50%',
            backgroundColor: '#166534',
            color: '#ffffff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontWeight: 700,
            fontSize: '15px'
          }}>
            {user?.username ? user.username.charAt(0).toUpperCase() : 'U'}
          </div>
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <span style={{ fontSize: '14px', fontWeight: 600, color: isDarkMode ? '#ffffff' : '#0f172a' }}>
              {user?.username || 'User'}
            </span>
            <span style={{ fontSize: '11px', color: isDarkMode ? '#94a3b8' : '#64748b' }}>
              My Account & Settings
            </span>
          </div>
        </Link>

        {/* Logout Button */}
        <button
          onClick={logout}
          style={{
            background: 'none',
            border: isDarkMode ? '1px solid #334155' : '1px solid #e2e8f0',
            borderRadius: '8px',
            padding: '8px',
            cursor: 'pointer',
            color: isDarkMode ? '#94a3b8' : '#64748b',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            transition: 'all 0.2s ease'
          }}
          title="Sign Out"
        >
          <LogOut size={18} />
        </button>
      </div>
    </header>
  );
};
