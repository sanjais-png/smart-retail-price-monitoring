import React, { Component } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Navbar } from './components/Navbar';
import { Sidebar } from './components/Sidebar';
import { AiChatWidget } from './components/AiChatWidget';

import { AuthPage } from './pages/AuthPage';
import { DashboardPage } from './pages/DashboardPage';
import { CatalogPage } from './pages/CatalogPage';
import { CommodityDetailPage } from './pages/CommodityDetailPage';
import { FairnessCheckerPage } from './pages/FairnessCheckerPage';
import { AgmarknetPage } from './pages/AgmarknetPage';
import { PredictionsPage } from './pages/PredictionsPage';
import { ComplaintsPage } from './pages/ComplaintsPage';
import { MyComplaintsPage } from './pages/MyComplaintsPage';
import { AuthorityAdminPage } from './pages/AuthorityAdminPage';
import { AdminControlCenterPage } from './pages/AdminControlCenterPage';
import { PendingApprovalPage } from './pages/PendingApprovalPage';
import { ProfilePage } from './pages/ProfilePage';
import { getRoleDefaultPath } from './utils/authRoleUtils';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, info) {
    console.error('ErrorBoundary caught:', error, info);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{ padding: '40px', textAlign: 'center' }}>
          <h3>Something went wrong loading this section.</h3>
          <button
            onClick={() => { this.setState({ hasError: false }); window.location.href = '/'; }}
            style={{ padding: '10px 20px', borderRadius: '8px', backgroundColor: '#166534', color: '#fff', border: 'none', cursor: 'pointer', marginTop: '12px' }}
          >
            Reload Dashboard
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}

const AppLayout = ({ children }) => {
  return (
    <div className="desktop-layout" style={{ display: 'flex', minHeight: '100vh', width: '100%' }}>
      <Sidebar />
      <div className="desktop-main" style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
        <Navbar />
        <main className="desktop-content" style={{ flex: 1, padding: '32px 40px', width: '100%', maxWidth: '1600px', margin: '0 auto' }}>
          <ErrorBoundary>
            {children}
          </ErrorBoundary>
        </main>
      </div>
      <AiChatWidget />
    </div>
  );
};

/**
 * RoleBasedRedirect — rendered at the root '/' path.
 *
 * - ROLE_USER      → renders DashboardPage (stays at /)
 * - ROLE_AUTHORITY → redirects to /authority
 * - ROLE_ADMIN     → redirects to /admin
 *
 * No loop risk: /authority renders AuthorityAdminPage directly (no further redirect).
 * /admin renders AdminControlCenterPage directly (no further redirect).
 * Only '/' uses RoleBasedRedirect.
 */
const RoleBasedRedirect = () => {
  const { user } = useAuth();
  const targetPath = getRoleDefaultPath(user);
  if (targetPath !== '/') {
    return <Navigate to={targetPath} replace />;
  }
  return <DashboardPage />;
};

const AppRoutes = () => {
  const { isAuthenticated } = useAuth();

  return (
    <Routes>
      <Route path="/auth" element={!isAuthenticated ? <AuthPage /> : <Navigate to="/" replace />} />

      <Route
        path="/"
        element={
          <ProtectedRoute>
            <AppLayout>
              <RoleBasedRedirect />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/pending"
        element={
          <ProtectedRoute isPendingRoute={true}>
            <AppLayout>
              <PendingApprovalPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/catalog"
        element={
          <ProtectedRoute>
            <AppLayout>
              <CatalogPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/catalog/:commodityId"
        element={
          <ProtectedRoute>
            <AppLayout>
              <CommodityDetailPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/fairness"
        element={
          <ProtectedRoute>
            <AppLayout>
              <FairnessCheckerPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/agmarknet"
        element={
          <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
            <AppLayout>
              <AgmarknetPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/predictions"
        element={
          <ProtectedRoute>
            <AppLayout>
              <PredictionsPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/complaints"
        element={
          <ProtectedRoute allowedRoles={['ROLE_USER']}>
            <AppLayout>
              <ComplaintsPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/my-complaints"
        element={
          <ProtectedRoute allowedRoles={['ROLE_USER']}>
            <AppLayout>
              <MyComplaintsPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/authority"
        element={
          <ProtectedRoute allowedRoles={['ROLE_AUTHORITY', 'ROLE_ADMIN']}>
            <AppLayout>
              <AuthorityAdminPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/admin"
        element={
          <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
            <AppLayout>
              <AdminControlCenterPage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <AppLayout>
              <ProfilePage />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}
