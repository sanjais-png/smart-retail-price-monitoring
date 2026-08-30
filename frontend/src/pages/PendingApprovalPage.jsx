import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Clock, ShieldAlert, LogOut, UserCheck } from 'lucide-react';

export const PendingApprovalPage = () => {
  const { user, logout } = useAuth();

  return (
    <div style={{
      minHeight: '80vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '24px'
    }}>
      <div style={{
        backgroundColor: '#ffffff',
        borderRadius: '24px',
        border: '1px solid #e2e8f0',
        boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.01)',
        maxWidth: '540px',
        width: '100%',
        padding: '40px 36px',
        textAlign: 'center'
      }}>
        {/* Icon Header */}
        <div style={{
          width: '72px',
          height: '72px',
          borderRadius: '50%',
          backgroundColor: '#fffbe3',
          border: '2px solid #fde68a',
          color: '#d97706',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          margin: '0 auto 24px auto',
          boxShadow: '0 0 20px rgba(217, 119, 6, 0.15)'
        }}>
          <Clock size={36} />
        </div>

        {/* Status Badge */}
        <div style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '6px',
          padding: '6px 14px',
          borderRadius: '20px',
          backgroundColor: '#fffbe3',
          border: '1px solid #fde68a',
          color: '#b45309',
          fontSize: '12px',
          fontWeight: 800,
          letterSpacing: '0.05em',
          marginBottom: '16px'
        }}>
          <ShieldAlert size={14} />
          APPROVAL PENDING REVIEW
        </div>

        {/* Title */}
        <h2 style={{ fontSize: '24px', fontWeight: 800, color: '#0f172a', marginBottom: '12px' }}>
          Authority Access Pending
        </h2>

        {/* Message Body */}
        <p style={{ fontSize: '15px', color: '#475569', lineHeight: 1.6, marginBottom: '28px' }}>
          Your request for Government Inspector / Authority privileges has been submitted and is currently being reviewed by a Platform Administrator.
        </p>

        {/* User Details Card */}
        <div style={{
          backgroundColor: '#f8fafc',
          borderRadius: '12px',
          padding: '16px 20px',
          border: '1px solid #e2e8f0',
          textAlign: 'left',
          marginBottom: '28px'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
            <span style={{ fontSize: '13px', color: '#64748b', fontWeight: 500 }}>Applicant Account:</span>
            <span style={{ fontSize: '13px', color: '#0f172a', fontWeight: 700 }}>{user?.username}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
            <span style={{ fontSize: '13px', color: '#64748b', fontWeight: 500 }}>Email Address:</span>
            <span style={{ fontSize: '13px', color: '#0f172a', fontWeight: 600 }}>{user?.email}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span style={{ fontSize: '13px', color: '#64748b', fontWeight: 500 }}>Requested Privileges:</span>
            <span style={{ fontSize: '13px', color: '#b45309', fontWeight: 700 }}>ROLE_AUTHORITY</span>
          </div>
        </div>

        <p style={{ fontSize: '13px', color: '#64748b', marginBottom: '28px' }}>
          Once approved by an administrator, sign in again to access the Authority Review Hub.
        </p>

        {/* Action Button */}
        <button
          onClick={logout}
          style={{
            width: '100%',
            backgroundColor: '#0f172a',
            color: '#ffffff',
            padding: '12px 24px',
            borderRadius: '12px',
            border: 'none',
            fontWeight: 700,
            fontSize: '14px',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '8px',
            transition: 'all 0.2s ease'
          }}
        >
          <LogOut size={16} />
          Sign Out & Return to Login
        </button>
      </div>
    </div>
  );
};
