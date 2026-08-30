import React, { useState, useEffect } from 'react';
import { adminApi } from '../api/adminApi';
import { catalogApi } from '../api/catalogApi';
import {
  Users,
  ShieldCheck,
  Clock,
  AlertCircle,
  CheckCircle2,
  XCircle,
  Database,
  RefreshCw,
  FileText,
  Settings,
  Plus,
  Search,
  Activity,
  Layers,
  Edit2,
  Check
} from 'lucide-react';

export const AdminControlCenterPage = () => {
  const [activeTab, setActiveTab] = useState('overview');

  // Stats State
  const [stats, setStats] = useState(null);
  const [statsLoading, setStatsLoading] = useState(false);
  const [statsError, setStatsError] = useState('');

  // Authority Requests State
  const [requests, setRequests] = useState([]);
  const [requestsLoading, setRequestsLoading] = useState(false);
  const [requestsError, setRequestsError] = useState('');

  // Consumer Accounts State
  const [consumers, setConsumers] = useState([]);
  const [consumersLoading, setConsumersLoading] = useState(false);
  const [consumersError, setConsumersError] = useState('');

  // Authority Officers State
  const [authorities, setAuthorities] = useState([]);
  const [authoritiesLoading, setAuthoritiesLoading] = useState(false);
  const [authoritiesError, setAuthoritiesError] = useState('');

  // Audit Logs State
  const [auditLogs, setAuditLogs] = useState([]);
  const [logsLoading, setLogsLoading] = useState(false);
  const [logsError, setLogsError] = useState('');

  // System Settings State
  const [settings, setSettings] = useState([]);
  const [settingsLoading, setSettingsLoading] = useState(false);
  const [settingsError, setSettingsError] = useState('');
  const [editingSetting, setEditingSetting] = useState(null);
  const [editValue, setEditValue] = useState('');

  // Action status message
  const [actionMessage, setActionMessage] = useState(null);
  const [actionProcessing, setActionProcessing] = useState(false);

  // Load Dashboard Stats & Pending Requests count on mount
  useEffect(() => {
    loadStats();
  }, []);

  // Fetch tab-specific data when activeTab changes
  useEffect(() => {
    if (activeTab === 'overview') loadStats();
    if (activeTab === 'requests') loadAuthorityRequests();
    if (activeTab === 'consumers') loadConsumers();
    if (activeTab === 'authorities') loadAuthorities();
    if (activeTab === 'audit') loadAuditLogs();
    if (activeTab === 'settings') loadSettings();
  }, [activeTab]);

  const loadStats = async () => {
    setStatsLoading(true);
    setStatsError('');
    const res = await adminApi.getDashboardStats();
    if (res.success && res.data) {
      setStats(res.data);
    } else {
      setStatsError(res.message || 'Unable to load platform overview metrics.');
    }
    setStatsLoading(false);
  };

  const loadAuthorityRequests = async () => {
    setRequestsLoading(true);
    setRequestsError('');
    const res = await adminApi.getAuthorityRequests();
    if (res.success && res.data) {
      setRequests(res.data.content || []);
    } else {
      setRequestsError(res.message || 'Unable to load authority requests.');
    }
    setRequestsLoading(false);
  };

  const loadConsumers = async () => {
    setConsumersLoading(true);
    setConsumersError('');
    const res = await adminApi.getAllUsers();
    if (res.success && res.data) {
      setConsumers(res.data.content || []);
    } else {
      setConsumersError(res.message || 'Unable to load consumer accounts.');
    }
    setConsumersLoading(false);
  };

  const loadAuthorities = async () => {
    setAuthoritiesLoading(true);
    setAuthoritiesError('');
    const res = await adminApi.getUsersByRole('ROLE_AUTHORITY');
    if (res.success && res.data) {
      setAuthorities(res.data.content || []);
    } else {
      setAuthoritiesError(res.message || 'Unable to load authority officers.');
    }
    setAuthoritiesLoading(false);
  };

  const loadAuditLogs = async () => {
    setLogsLoading(true);
    setLogsError('');
    const res = await adminApi.getAuditLogs();
    if (res.success && res.data) {
      setAuditLogs(res.data.content || []);
    } else {
      setLogsError(res.message || 'Unable to load compliance audit logs.');
    }
    setLogsLoading(false);
  };

  const loadSettings = async () => {
    setSettingsLoading(true);
    setSettingsError('');
    const res = await adminApi.getSystemSettings();
    if (res.success && res.data) {
      setSettings(res.data || []);
    } else {
      setSettingsError(res.message || 'Unable to load system settings.');
    }
    setSettingsLoading(false);
  };

  const handleApprove = async (userId, username) => {
    if (!window.confirm(`Approve Authority privileges for '${username}'? This will replace their role with ROLE_AUTHORITY.`)) return;
    setActionProcessing(true);
    const res = await adminApi.approveAuthorityRequest(userId);
    setActionProcessing(false);
    if (res.success) {
      setActionMessage({ type: 'success', text: `Approved Authority access for ${username}. Role set to ROLE_AUTHORITY.` });
      loadAuthorityRequests();
      loadStats();
    } else {
      setActionMessage({ type: 'error', text: res.message || 'Failed to approve request.' });
    }
  };

  const handleDeny = async (userId, username) => {
    if (!window.confirm(`Deny Authority privileges for '${username}'? Their role will remain ROLE_USER.`)) return;
    setActionProcessing(true);
    const res = await adminApi.denyAuthorityRequest(userId);
    setActionProcessing(false);
    if (res.success) {
      setActionMessage({ type: 'success', text: `Denied Authority access for ${username}. User remains ROLE_USER.` });
      loadAuthorityRequests();
      loadStats();
    } else {
      setActionMessage({ type: 'error', text: res.message || 'Failed to deny request.' });
    }
  };

  const handleToggleUser = async (userId, currentEnabled, username) => {
    const nextState = !currentEnabled;
    if (!window.confirm(`${nextState ? 'Enable' : 'Disable'} account for '${username}'?`)) return;
    setActionProcessing(true);
    const res = await adminApi.toggleUserStatus(userId, nextState);
    setActionProcessing(false);
    if (res.success) {
      setActionMessage({ type: 'success', text: `Account for ${username} ${nextState ? 'enabled' : 'disabled'}.` });
      if (activeTab === 'consumers') loadConsumers();
      if (activeTab === 'authorities') loadAuthorities();
    } else {
      setActionMessage({ type: 'error', text: res.message || 'Failed to update user status.' });
    }
  };

  const handleSaveSetting = async () => {
    if (!editingSetting) return;
    setActionProcessing(true);
    const res = await adminApi.updateSystemSetting(editingSetting.settingKey, editValue);
    setActionProcessing(false);
    if (res.success) {
      setActionMessage({ type: 'success', text: `Setting '${editingSetting.settingKey}' updated successfully.` });
      setEditingSetting(null);
      loadSettings();
    } else {
      setActionMessage({ type: 'error', text: res.message || 'Failed to update setting.' });
    }
  };

  const pendingCount = stats?.pendingAuthorityRequests ?? 0;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
      
      {/* Header Banner */}
      <div style={{
        background: 'linear-gradient(135deg, #4c1d95 0%, #6b21a8 100%)',
        color: '#ffffff',
        borderRadius: '20px',
        padding: '32px 40px',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        boxShadow: '0 10px 25px -5px rgba(107, 33, 168, 0.25)'
      }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <ShieldCheck size={28} color="#e9d5ff" />
            <h2 style={{ fontSize: '28px', fontWeight: 800 }}>Admin Control Center</h2>
          </div>
          <p style={{ fontSize: '15px', color: '#e9d5ff', marginTop: '6px' }}>
            Platform Governance, Authority Provisioning, Compliance Audit & Database Metrics
          </p>
        </div>

        <button
          onClick={() => { loadStats(); if (activeTab === 'requests') loadAuthorityRequests(); }}
          style={{
            backgroundColor: '#ffffff',
            color: '#581c87',
            padding: '10px 20px',
            borderRadius: '12px',
            border: 'none',
            fontWeight: 700,
            fontSize: '14px',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}
        >
          <RefreshCw size={16} />
          Refresh Panel
        </button>
      </div>

      {/* Action Notification Toast */}
      {actionMessage && (
        <div style={{
          padding: '14px 20px',
          borderRadius: '12px',
          backgroundColor: actionMessage.type === 'success' ? '#f0fdf4' : '#fef2f2',
          border: actionMessage.type === 'success' ? '1px solid #bbf7d0' : '1px solid #fecaca',
          color: actionMessage.type === 'success' ? '#166534' : '#991b1b',
          fontSize: '14px',
          fontWeight: 600,
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <span>{actionMessage.text}</span>
          <button onClick={() => setActionMessage(null)} style={{ background: 'none', border: 'none', cursor: 'pointer', fontWeight: 700 }}>✕</button>
        </div>
      )}

      {/* Navigation Tabs */}
      <div style={{
        display: 'flex',
        gap: '8px',
        borderBottom: '2px solid #e2e8f0',
        paddingBottom: '2px',
        overflowX: 'auto'
      }}>
        {[
          { id: 'overview', label: 'Platform Overview', icon: Activity },
          { id: 'requests', label: `Authority Requests`, icon: Clock, badge: pendingCount },
          { id: 'consumers', label: 'Consumer Accounts', icon: Users },
          { id: 'authorities', label: 'Authority Officers', icon: ShieldCheck },
          { id: 'audit', label: 'Audit Logs', icon: FileText },
          { id: 'settings', label: 'System Settings', icon: Settings }
        ].map(tab => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '12px 20px',
                borderRadius: '10px 10px 0 0',
                border: 'none',
                backgroundColor: isActive ? '#6b21a8' : 'transparent',
                color: isActive ? '#ffffff' : '#64748b',
                fontWeight: 700,
                fontSize: '14px',
                cursor: 'pointer',
                transition: 'all 0.15s ease'
              }}
            >
              <Icon size={16} />
              <span>{tab.label}</span>
              {tab.badge > 0 && (
                <span style={{
                  backgroundColor: isActive ? '#f59e0b' : '#ef4444',
                  color: '#ffffff',
                  borderRadius: '12px',
                  padding: '2px 8px',
                  fontSize: '11px',
                  fontWeight: 800
                }}>
                  {tab.badge}
                </span>
              )}
            </button>
          );
        })}
      </div>

      {/* TAB 1: PLATFORM OVERVIEW */}
      {activeTab === 'overview' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          {statsError && (
            <div style={{ padding: '16px', borderRadius: '12px', backgroundColor: '#fef2f2', border: '1px solid #fecaca', color: '#991b1b', display: 'flex', justifyContent: 'space-between' }}>
              <span>{statsError}</span>
              <button onClick={loadStats} style={{ backgroundColor: '#991b1b', color: '#fff', border: 'none', borderRadius: '6px', padding: '4px 12px', cursor: 'pointer' }}>Retry</button>
            </div>
          )}

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '20px' }}>
            <div style={{ backgroundColor: '#ffffff', borderRadius: '16px', padding: '24px', border: '1px solid #e2e8f0' }}>
              <div style={{ fontSize: '13px', fontWeight: 600, color: '#64748b' }}>Total User Accounts</div>
              <div style={{ fontSize: '32px', fontWeight: 800, color: '#0f172a', marginTop: '8px' }}>
                {statsLoading ? '...' : (stats?.totalUsers ?? 0)}
              </div>
              <div style={{ fontSize: '12px', color: '#166534', marginTop: '4px' }}>Registered consumers in MySQL</div>
            </div>

            <div style={{ backgroundColor: '#ffffff', borderRadius: '16px', padding: '24px', border: '1px solid #e2e8f0' }}>
              <div style={{ fontSize: '13px', fontWeight: 600, color: '#64748b' }}>Active Authority Officers</div>
              <div style={{ fontSize: '32px', fontWeight: 800, color: '#6b21a8', marginTop: '8px' }}>
                {statsLoading ? '...' : (stats?.authorityUsers ?? 0)}
              </div>
              <div style={{ fontSize: '12px', color: '#6b21a8', marginTop: '4px' }}>Approved ROLE_AUTHORITY accounts</div>
            </div>

            <div style={{ backgroundColor: '#ffffff', borderRadius: '16px', padding: '24px', border: '1px solid #e2e8f0' }}>
              <div style={{ fontSize: '13px', fontWeight: 600, color: '#64748b' }}>Pending Authority Requests</div>
              <div style={{ fontSize: '32px', fontWeight: 800, color: '#d97706', marginTop: '8px' }}>
                {statsLoading ? '...' : (stats?.pendingAuthorityRequests ?? 0)}
              </div>
              <div style={{ fontSize: '12px', color: '#d97706', marginTop: '4px' }}>Awaiting Admin approval</div>
            </div>

            <div style={{ backgroundColor: '#ffffff', borderRadius: '16px', padding: '24px', border: '1px solid #e2e8f0' }}>
              <div style={{ fontSize: '13px', fontWeight: 600, color: '#64748b' }}>Total Complaints Filed</div>
              <div style={{ fontSize: '32px', fontWeight: 800, color: '#2563eb', marginTop: '8px' }}>
                {statsLoading ? '...' : (stats?.totalComplaints ?? 0)}
              </div>
              <div style={{ fontSize: '12px', color: '#2563eb', marginTop: '4px' }}>Consumer price reports</div>
            </div>
          </div>
        </div>
      )}

      {/* TAB 2: AUTHORITY REQUESTS */}
      {activeTab === 'requests' && (
        <div style={{ backgroundColor: '#ffffff', borderRadius: '16px', padding: '28px', border: '1px solid #e2e8f0' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <div>
              <h3 style={{ fontSize: '18px', fontWeight: 800, color: '#0f172a' }}>Pending Authority Access Requests</h3>
              <p style={{ fontSize: '13px', color: '#64748b', marginTop: '4px' }}>
                Review applicants requesting ROLE_AUTHORITY privileges. Approving will atomically upgrade their role to ROLE_AUTHORITY and set approval status to APPROVED.
              </p>
            </div>
            <button onClick={loadAuthorityRequests} style={{ padding: '8px 16px', borderRadius: '8px', border: '1px solid #cbd5e1', backgroundColor: '#ffffff', cursor: 'pointer' }}>
              <RefreshCw size={14} /> Refresh
            </button>
          </div>

          {requestsError && (
            <div style={{ padding: '16px', borderRadius: '8px', backgroundColor: '#fef2f2', border: '1px solid #fecaca', color: '#991b1b', marginBottom: '16px', display: 'flex', justifyContent: 'space-between' }}>
              <span>{requestsError}</span>
              <button onClick={loadAuthorityRequests} style={{ backgroundColor: '#991b1b', color: '#fff', border: 'none', borderRadius: '6px', padding: '4px 12px', cursor: 'pointer' }}>Retry</button>
            </div>
          )}

          {requestsLoading ? (
            <div style={{ padding: '40px', textAlign: 'center', color: '#64748b' }}>Loading authority requests...</div>
          ) : requests.length === 0 ? (
            <div style={{ padding: '40px', textAlign: 'center', color: '#64748b', backgroundColor: '#f8fafc', borderRadius: '12px' }}>
              No pending authority access requests found.
            </div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
              <thead>
                <tr style={{ backgroundColor: '#f8fafc', borderBottom: '2px solid #e2e8f0', textAlign: 'left' }}>
                  <th style={{ padding: '12px 16px' }}>User ID</th>
                  <th style={{ padding: '12px 16px' }}>Username</th>
                  <th style={{ padding: '12px 16px' }}>Email</th>
                  <th style={{ padding: '12px 16px' }}>Current Status</th>
                  <th style={{ padding: '12px 16px' }}>Registered At</th>
                  <th style={{ padding: '12px 16px', textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {requests.map(req => (
                  <tr key={req.id} style={{ borderBottom: '1px solid #e2e8f0' }}>
                    <td style={{ padding: '14px 16px', fontWeight: 600 }}>#{req.id}</td>
                    <td style={{ padding: '14px 16px', fontWeight: 700, color: '#0f172a' }}>{req.username}</td>
                    <td style={{ padding: '14px 16px', color: '#64748b' }}>{req.email}</td>
                    <td style={{ padding: '14px 16px' }}>
                      <span style={{ padding: '4px 10px', borderRadius: '12px', backgroundColor: '#fffbe3', color: '#b45309', fontWeight: 700, fontSize: '12px', border: '1px solid #fde68a' }}>
                        PENDING_AUTHORITY
                      </span>
                    </td>
                    <td style={{ padding: '14px 16px', color: '#64748b', fontSize: '13px' }}>
                      {req.createdAt ? new Date(req.createdAt).toLocaleString() : 'N/A'}
                    </td>
                    <td style={{ padding: '14px 16px', textAlign: 'right' }}>
                      <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
                        <button
                          disabled={actionProcessing}
                          onClick={() => handleApprove(req.id, req.username)}
                          style={{
                            padding: '6px 14px',
                            borderRadius: '8px',
                            backgroundColor: '#166534',
                            color: '#ffffff',
                            border: 'none',
                            fontWeight: 700,
                            fontSize: '13px',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '4px'
                          }}
                        >
                          <CheckCircle2 size={14} /> Approve
                        </button>
                        <button
                          disabled={actionProcessing}
                          onClick={() => handleDeny(req.id, req.username)}
                          style={{
                            padding: '6px 14px',
                            borderRadius: '8px',
                            backgroundColor: '#ffffff',
                            color: '#991b1b',
                            border: '1px solid #fecaca',
                            fontWeight: 700,
                            fontSize: '13px',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '4px'
                          }}
                        >
                          <XCircle size={14} /> Deny
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* TAB 3: CONSUMER ACCOUNTS */}
      {activeTab === 'consumers' && (
        <div style={{ backgroundColor: '#ffffff', borderRadius: '16px', padding: '28px', border: '1px solid #e2e8f0' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <h3 style={{ fontSize: '18px', fontWeight: 800, color: '#0f172a' }}>Consumer Accounts</h3>
            <button onClick={loadConsumers} style={{ padding: '8px 16px', borderRadius: '8px', border: '1px solid #cbd5e1', backgroundColor: '#ffffff', cursor: 'pointer' }}>
              <RefreshCw size={14} /> Refresh
            </button>
          </div>

          {consumersError ? (
            <div style={{ padding: '24px', textAlign: 'center', backgroundColor: '#fef2f2', border: '1px solid #fecaca', borderRadius: '12px', color: '#991b1b' }}>
              <p style={{ fontWeight: 700, fontSize: '15px', marginBottom: '12px' }}>{consumersError}</p>
              <button onClick={loadConsumers} style={{ backgroundColor: '#991b1b', color: '#ffffff', border: 'none', borderRadius: '8px', padding: '8px 20px', fontWeight: 700, cursor: 'pointer' }}>
                Retry Loading Consumer Accounts
              </button>
            </div>
          ) : consumersLoading ? (
            <div style={{ padding: '40px', textAlign: 'center', color: '#64748b' }}>Loading consumer accounts...</div>
          ) : consumers.length === 0 ? (
            <div style={{ padding: '40px', textAlign: 'center', color: '#64748b', backgroundColor: '#f8fafc', borderRadius: '12px' }}>
              No consumer accounts found.
            </div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
              <thead>
                <tr style={{ backgroundColor: '#f8fafc', borderBottom: '2px solid #e2e8f0', textAlign: 'left' }}>
                  <th style={{ padding: '12px 16px' }}>ID</th>
                  <th style={{ padding: '12px 16px' }}>Username</th>
                  <th style={{ padding: '12px 16px' }}>Email</th>
                  <th style={{ padding: '12px 16px' }}>Role</th>
                  <th style={{ padding: '12px 16px' }}>Status</th>
                  <th style={{ padding: '12px 16px', textAlign: 'right' }}>Account Toggle</th>
                </tr>
              </thead>
              <tbody>
                {consumers.map(u => (
                  <tr key={u.id} style={{ borderBottom: '1px solid #e2e8f0' }}>
                    <td style={{ padding: '14px 16px', fontWeight: 600 }}>#{u.id}</td>
                    <td style={{ padding: '14px 16px', fontWeight: 700 }}>{u.username}</td>
                    <td style={{ padding: '14px 16px', color: '#64748b' }}>{u.email}</td>
                    <td style={{ padding: '14px 16px' }}>
                      <span style={{ padding: '4px 8px', borderRadius: '8px', backgroundColor: '#f0fdf4', color: '#166534', fontWeight: 700, fontSize: '12px' }}>
                        {Array.from(u.roles || []).join(', ')}
                      </span>
                    </td>
                    <td style={{ padding: '14px 16px' }}>
                      <span style={{ padding: '4px 8px', borderRadius: '8px', backgroundColor: u.enabled ? '#f0fdf4' : '#fef2f2', color: u.enabled ? '#166534' : '#991b1b', fontWeight: 700, fontSize: '12px' }}>
                        {u.enabled ? 'Enabled' : 'Disabled'}
                      </span>
                    </td>
                    <td style={{ padding: '14px 16px', textAlign: 'right' }}>
                      <button
                        onClick={() => handleToggleUser(u.id, u.enabled, u.username)}
                        style={{
                          padding: '6px 12px',
                          borderRadius: '6px',
                          border: u.enabled ? '1px solid #fecaca' : '1px solid #bbf7d0',
                          backgroundColor: u.enabled ? '#ffffff' : '#f0fdf4',
                          color: u.enabled ? '#991b1b' : '#166534',
                          fontWeight: 600,
                          fontSize: '12px',
                          cursor: 'pointer'
                        }}
                      >
                        {u.enabled ? 'Disable' : 'Enable'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* TAB 4: AUTHORITY OFFICERS */}
      {activeTab === 'authorities' && (
        <div style={{ backgroundColor: '#ffffff', borderRadius: '16px', padding: '28px', border: '1px solid #e2e8f0' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <h3 style={{ fontSize: '18px', fontWeight: 800, color: '#0f172a' }}>Approved Authority Officers</h3>
            <button onClick={loadAuthorities} style={{ padding: '8px 16px', borderRadius: '8px', border: '1px solid #cbd5e1', backgroundColor: '#ffffff', cursor: 'pointer' }}>
              <RefreshCw size={14} /> Refresh
            </button>
          </div>

          {authoritiesError && (
            <div style={{ padding: '16px', borderRadius: '8px', backgroundColor: '#fef2f2', border: '1px solid #fecaca', color: '#991b1b', marginBottom: '16px', display: 'flex', justifyContent: 'space-between' }}>
              <span>{authoritiesError}</span>
              <button onClick={loadAuthorities} style={{ backgroundColor: '#991b1b', color: '#fff', border: 'none', borderRadius: '6px', padding: '4px 12px', cursor: 'pointer' }}>Retry</button>
            </div>
          )}

          {authoritiesLoading ? (
            <div style={{ padding: '40px', textAlign: 'center', color: '#64748b' }}>Loading authority officers...</div>
          ) : authorities.length === 0 ? (
            <div style={{ padding: '40px', textAlign: 'center', color: '#64748b', backgroundColor: '#f8fafc', borderRadius: '12px' }}>
              No approved authority officers found.
            </div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
              <thead>
                <tr style={{ backgroundColor: '#f8fafc', borderBottom: '2px solid #e2e8f0', textAlign: 'left' }}>
                  <th style={{ padding: '12px 16px' }}>ID</th>
                  <th style={{ padding: '12px 16px' }}>Username</th>
                  <th style={{ padding: '12px 16px' }}>Email</th>
                  <th style={{ padding: '12px 16px' }}>Approval Status</th>
                  <th style={{ padding: '12px 16px' }}>Account Status</th>
                  <th style={{ padding: '12px 16px', textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {authorities.map(u => (
                  <tr key={u.id} style={{ borderBottom: '1px solid #e2e8f0' }}>
                    <td style={{ padding: '14px 16px', fontWeight: 600 }}>#{u.id}</td>
                    <td style={{ padding: '14px 16px', fontWeight: 700, color: '#6b21a8' }}>{u.username}</td>
                    <td style={{ padding: '14px 16px', color: '#64748b' }}>{u.email}</td>
                    <td style={{ padding: '14px 16px' }}>
                      <span style={{ padding: '4px 8px', borderRadius: '8px', backgroundColor: '#f0fdf4', color: '#166534', fontWeight: 700, fontSize: '12px' }}>
                        {u.approvalStatus || 'APPROVED'}
                      </span>
                    </td>
                    <td style={{ padding: '14px 16px' }}>
                      <span style={{ padding: '4px 8px', borderRadius: '8px', backgroundColor: u.enabled ? '#f0fdf4' : '#fef2f2', color: u.enabled ? '#166534' : '#991b1b', fontWeight: 700, fontSize: '12px' }}>
                        {u.enabled ? 'Enabled' : 'Disabled'}
                      </span>
                    </td>
                    <td style={{ padding: '14px 16px', textAlign: 'right' }}>
                      <button
                        onClick={() => handleToggleUser(u.id, u.enabled, u.username)}
                        style={{
                          padding: '6px 12px',
                          borderRadius: '6px',
                          border: u.enabled ? '1px solid #fecaca' : '1px solid #bbf7d0',
                          backgroundColor: u.enabled ? '#ffffff' : '#f0fdf4',
                          color: u.enabled ? '#991b1b' : '#166534',
                          fontWeight: 600,
                          fontSize: '12px',
                          cursor: 'pointer'
                        }}
                      >
                        {u.enabled ? 'Disable' : 'Enable'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* TAB 5: AUDIT LOGS */}
      {activeTab === 'audit' && (
        <div style={{ backgroundColor: '#ffffff', borderRadius: '16px', padding: '28px', border: '1px solid #e2e8f0' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <div>
              <h3 style={{ fontSize: '18px', fontWeight: 800, color: '#0f172a' }}>System Audit Logs</h3>
              <p style={{ fontSize: '13px', color: '#64748b', marginTop: '4px' }}>
                Immutable compliance audit trail tracking all administrative approvals, status updates, and configuration changes.
              </p>
            </div>
            <button onClick={loadAuditLogs} style={{ padding: '8px 16px', borderRadius: '8px', border: '1px solid #cbd5e1', backgroundColor: '#ffffff', cursor: 'pointer' }}>
              <RefreshCw size={14} /> Refresh Logs
            </button>
          </div>

          {logsError && (
            <div style={{ padding: '16px', borderRadius: '8px', backgroundColor: '#fef2f2', border: '1px solid #fecaca', color: '#991b1b', marginBottom: '16px', display: 'flex', justifyContent: 'space-between' }}>
              <span>{logsError}</span>
              <button onClick={loadAuditLogs} style={{ backgroundColor: '#991b1b', color: '#fff', border: 'none', borderRadius: '6px', padding: '4px 12px', cursor: 'pointer' }}>Retry</button>
            </div>
          )}

          {logsLoading ? (
            <div style={{ padding: '40px', textAlign: 'center', color: '#64748b' }}>Loading audit logs...</div>
          ) : auditLogs.length === 0 ? (
            <div style={{ padding: '40px', textAlign: 'center', color: '#64748b', backgroundColor: '#f8fafc', borderRadius: '12px' }}>
              No audit log entries recorded yet.
            </div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px' }}>
              <thead>
                <tr style={{ backgroundColor: '#f8fafc', borderBottom: '2px solid #e2e8f0', textAlign: 'left' }}>
                  <th style={{ padding: '12px 16px' }}>Timestamp</th>
                  <th style={{ padding: '12px 16px' }}>Actor</th>
                  <th style={{ padding: '12px 16px' }}>Action</th>
                  <th style={{ padding: '12px 16px' }}>Target Entity</th>
                  <th style={{ padding: '12px 16px' }}>Details</th>
                </tr>
              </thead>
              <tbody>
                {auditLogs.map(log => (
                  <tr key={log.id} style={{ borderBottom: '1px solid #e2e8f0' }}>
                    <td style={{ padding: '12px 16px', color: '#64748b', whiteSpace: 'nowrap' }}>
                      {log.timestamp ? new Date(log.timestamp).toLocaleString() : 'N/A'}
                    </td>
                    <td style={{ padding: '12px 16px', fontWeight: 700, color: '#4c1d95' }}>{log.username}</td>
                    <td style={{ padding: '12px 16px' }}>
                      <span style={{ padding: '3px 8px', borderRadius: '6px', backgroundColor: '#faf5ff', color: '#7c3aed', fontWeight: 700, fontSize: '11px', border: '1px solid #ddd6fe' }}>
                        {log.action}
                      </span>
                    </td>
                    <td style={{ padding: '12px 16px', color: '#334155' }}>
                      {log.entityName} #{log.entityId}
                    </td>
                    <td style={{ padding: '12px 16px', color: '#0f172a' }}>{log.details}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* TAB 6: SYSTEM SETTINGS */}
      {activeTab === 'settings' && (
        <div style={{ backgroundColor: '#ffffff', borderRadius: '16px', padding: '28px', border: '1px solid #e2e8f0' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <div>
              <h3 style={{ fontSize: '18px', fontWeight: 800, color: '#0f172a' }}>Platform System Settings</h3>
              <p style={{ fontSize: '13px', color: '#64748b', marginTop: '4px' }}>
                Database-backed system configuration parameters. Editable settings update MySQL dynamically and log an audit event.
              </p>
            </div>
            <button onClick={loadSettings} style={{ padding: '8px 16px', borderRadius: '8px', border: '1px solid #cbd5e1', backgroundColor: '#ffffff', cursor: 'pointer' }}>
              <RefreshCw size={14} /> Refresh Settings
            </button>
          </div>

          {settingsError && (
            <div style={{ padding: '16px', borderRadius: '8px', backgroundColor: '#fef2f2', border: '1px solid #fecaca', color: '#991b1b', marginBottom: '16px', display: 'flex', justifyContent: 'space-between' }}>
              <span>{settingsError}</span>
              <button onClick={loadSettings} style={{ backgroundColor: '#991b1b', color: '#fff', border: 'none', borderRadius: '6px', padding: '4px 12px', cursor: 'pointer' }}>Retry</button>
            </div>
          )}

          {settingsLoading ? (
            <div style={{ padding: '40px', textAlign: 'center', color: '#64748b' }}>Loading system settings...</div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
              <thead>
                <tr style={{ backgroundColor: '#f8fafc', borderBottom: '2px solid #e2e8f0', textAlign: 'left' }}>
                  <th style={{ padding: '12px 16px' }}>Setting Key</th>
                  <th style={{ padding: '12px 16px' }}>Description</th>
                  <th style={{ padding: '12px 16px' }}>Current Value</th>
                  <th style={{ padding: '12px 16px' }}>Mode</th>
                  <th style={{ padding: '12px 16px', textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {settings.map(s => (
                  <tr key={s.id} style={{ borderBottom: '1px solid #e2e8f0' }}>
                    <td style={{ padding: '14px 16px', fontWeight: 700, color: '#0f172a', fontFamily: 'monospace' }}>
                      {s.settingKey}
                    </td>
                    <td style={{ padding: '14px 16px', color: '#64748b', maxWidth: '300px' }}>{s.description}</td>
                    <td style={{ padding: '14px 16px', fontWeight: 700, color: '#166534' }}>
                      {s.settingValue}
                    </td>
                    <td style={{ padding: '14px 16px' }}>
                      <span style={{ padding: '4px 8px', borderRadius: '6px', backgroundColor: s.editable ? '#f0fdf4' : '#f8fafc', color: s.editable ? '#166534' : '#64748b', fontWeight: 700, fontSize: '11px', border: '1px solid #e2e8f0' }}>
                        {s.editable ? 'Editable' : 'Read-Only'}
                      </span>
                    </td>
                    <td style={{ padding: '14px 16px', textAlign: 'right' }}>
                      {s.editable && (
                        <button
                          onClick={() => { setEditingSetting(s); setEditValue(s.settingValue); }}
                          style={{
                            padding: '6px 12px',
                            borderRadius: '6px',
                            border: '1px solid #cbd5e1',
                            backgroundColor: '#ffffff',
                            color: '#0f172a',
                            fontWeight: 600,
                            fontSize: '12px',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '4px',
                            marginLeft: 'auto'
                          }}
                        >
                          <Edit2 size={12} /> Edit
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* Edit Setting Modal */}
      {editingSetting && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(15, 23, 42, 0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{ backgroundColor: '#ffffff', borderRadius: '16px', padding: '32px', maxWidth: '440px', width: '90%', border: '1px solid #e2e8f0' }}>
            <h3 style={{ fontSize: '18px', fontWeight: 800, color: '#0f172a', marginBottom: '8px' }}>
              Edit Setting: {editingSetting.settingKey}
            </h3>
            <p style={{ fontSize: '13px', color: '#64748b', marginBottom: '20px' }}>
              {editingSetting.description}
            </p>

            <div style={{ marginBottom: '20px' }}>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '8px' }}>
                New Value
              </label>
              <input
                type="text"
                value={editValue}
                onChange={(e) => setEditValue(e.target.value)}
                style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #cbd5e1', fontSize: '14px', outline: 'none' }}
              />
            </div>

            <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
              <button
                onClick={() => setEditingSetting(null)}
                style={{ padding: '10px 18px', borderRadius: '8px', border: '1px solid #cbd5e1', backgroundColor: '#ffffff', cursor: 'pointer', fontWeight: 600 }}
              >
                Cancel
              </button>
              <button
                disabled={actionProcessing}
                onClick={handleSaveSetting}
                style={{ padding: '10px 18px', borderRadius: '8px', border: 'none', backgroundColor: '#6b21a8', color: '#ffffff', cursor: 'pointer', fontWeight: 700 }}
              >
                {actionProcessing ? 'Saving...' : 'Save Setting'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
