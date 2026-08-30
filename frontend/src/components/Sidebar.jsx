import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  LayoutDashboard, 
  ShoppingBag, 
  Scale, 
  Database, 
  TrendingUp, 
  AlertTriangle, 
  Building2,
  User,
  Sparkles,
  ClipboardList,
  ShieldCheck
} from 'lucide-react';

export const Sidebar = () => {
  const { user } = useAuth();
  const role = user?.role || 'ROLE_USER';

  const navItems = [
    // ── Consumer (ROLE_USER) ──
    { label: 'Market Overview',      path: '/',              icon: LayoutDashboard, roles: ['ROLE_USER', 'ROLE_AUTHORITY', 'ROLE_ADMIN'] },
    { label: 'Commodity Catalog',    path: '/catalog',       icon: ShoppingBag,     roles: ['ROLE_USER', 'ROLE_AUTHORITY', 'ROLE_ADMIN'] },
    { label: 'Price Fairness Tool',  path: '/fairness',      icon: Scale,           roles: ['ROLE_USER', 'ROLE_AUTHORITY', 'ROLE_ADMIN'] },
    { label: 'AI Trend Forecasts',   path: '/predictions',   icon: TrendingUp,      roles: ['ROLE_USER', 'ROLE_AUTHORITY', 'ROLE_ADMIN'] },
    { label: 'Report Price Gouging', path: '/complaints',    icon: AlertTriangle,   roles: ['ROLE_USER'] },
    { label: 'My Complaints',        path: '/my-complaints', icon: ClipboardList,   roles: ['ROLE_USER'] },

    // ── Authority (ROLE_AUTHORITY) ──
    { label: 'Authority Review Hub', path: '/authority',     icon: Building2,       roles: ['ROLE_AUTHORITY', 'ROLE_ADMIN'] },

    // ── Admin (ROLE_ADMIN) ──
    { label: 'Admin Control Center', path: '/admin',         icon: ShieldCheck,     roles: ['ROLE_ADMIN'] },
    { label: 'Agmarknet Data Sync',  path: '/agmarknet',     icon: Database,        roles: ['ROLE_ADMIN'] },

    // ── All roles ──
    { label: 'My Profile & Security', path: '/profile',     icon: User,            roles: ['ROLE_USER', 'ROLE_AUTHORITY', 'ROLE_ADMIN'] },
  ];

  const visibleItems = navItems.filter(item => item.roles.includes(role));

  return (
    <aside style={{
      width: '260px',
      backgroundColor: '#166534',
      color: '#ffffff',
      display: 'flex',
      flexDirection: 'column',
      minHeight: '100vh',
      flexShrink: 0
    }}>
      {/* Brand Header */}
      <div style={{
        padding: '24px 20px',
        borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
        display: 'flex',
        alignItems: 'center',
        gap: '12px'
      }}>
        <div style={{
          width: '36px',
          height: '36px',
          borderRadius: '10px',
          backgroundColor: '#ffffff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#166534',
          fontWeight: 800,
          fontSize: '18px'
        }}>
          F
        </div>
        <div>
          <h2 style={{ fontSize: '18px', fontWeight: 800, color: '#ffffff', lineHeight: 1.2 }}>
            FairPrice <span style={{ color: '#86efac' }}>AI</span>
          </h2>
          <p style={{ fontSize: '11px', color: '#bbf7d0', letterSpacing: '0.05em' }}>RETAIL PRICE MONITORING</p>
        </div>
      </div>

      {/* Navigation List */}
      <nav style={{ padding: '20px 12px', flex: 1, display: 'flex', flexDirection: 'column', gap: '6px' }}>
        {visibleItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              style={({ isActive }) => ({
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                padding: '12px 16px',
                borderRadius: '10px',
                textDecoration: 'none',
                fontSize: '14px',
                fontWeight: isActive ? 700 : 500,
                color: isActive ? '#ffffff' : '#bbf7d0',
                backgroundColor: isActive ? 'rgba(255, 255, 255, 0.15)' : 'transparent',
                transition: 'all 0.15s ease'
              })}
            >
              <Icon size={18} />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      {/* AI Assistant Callout */}
      <div style={{
        margin: '16px',
        padding: '16px',
        borderRadius: '12px',
        backgroundColor: 'rgba(255, 255, 255, 0.08)',
        border: '1px solid rgba(255, 255, 255, 0.15)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
          <Sparkles size={16} color="#86efac" />
          <span style={{ fontSize: '13px', fontWeight: 700, color: '#ffffff' }}>FairPrice AI Assistant</span>
        </div>
        <p style={{ fontSize: '12px', color: '#bbf7d0', lineHeight: 1.4 }}>
          Ask our AI assistant about fair price bounds & regional market spikes.
        </p>
      </div>
    </aside>
  );
};
