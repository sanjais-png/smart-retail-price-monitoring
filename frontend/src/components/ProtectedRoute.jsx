import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getRoleDefaultPath } from '../utils/authRoleUtils';

export const ProtectedRoute = ({ children, allowedRoles, isPendingRoute = false }) => {
  const { isAuthenticated, user, hasRole } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/auth" replace />;
  }

  const isPending = user?.approvalStatus === 'PENDING_AUTHORITY';

  // 1. Pending Authority Applicant Security Safeguard:
  // If user is PENDING_AUTHORITY and trying to access ANY non-pending route -> redirect to /pending
  if (isPending && !isPendingRoute && location.pathname !== '/pending') {
    return <Navigate to="/pending" replace />;
  }

  // 2. If user is NOT pending and trying to access /pending -> redirect to their landing page
  if (!isPending && (isPendingRoute || location.pathname === '/pending')) {
    return <Navigate to={getRoleDefaultPath(user)} replace />;
  }

  // 3. Role-based permission check
  if (allowedRoles && !hasRole(allowedRoles)) {
    return (
      <div style={{ padding: '40px', textAlign: 'center' }}>
        <h2 style={{ color: '#ef4444' }}>Access Denied</h2>
        <p style={{ color: '#64748b', marginTop: '8px' }}>
          Your active role (<strong>{user?.role}</strong>) does not have authorization to view this page.
        </p>
      </div>
    );
  }

  return children;
};
