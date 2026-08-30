import React, { createContext, useContext, useState } from 'react';
import { authApi } from '../api/authApi';
import { getRoleDefaultPath } from '../utils/authRoleUtils';

// ---------------------------------------------------------------------------
// Default context — only used when rendered outside of AuthProvider (e.g. tests)
// ---------------------------------------------------------------------------
const defaultAuthContext = {
  token: null,
  user: null,
  login: async () => ({ success: false }),
  register: async () => ({ success: false }),
  logout: () => {},
  hasRole: () => false,
  isAuthenticated: false
};

const AuthContext = createContext(defaultAuthContext);

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(() => localStorage.getItem('fairprice_token') || null);
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('fairprice_user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });

  /**
   * Login: authenticates against the backend and ALWAYS overwrites any previously
   * cached user/role state with the fresh server-returned values before returning.
   *
   * The server-issued role in the API response is the authoritative source.
   * localStorage is a convenience cache only — it is never used to derive the
   * post-login navigation destination when a fresh login response is available.
   */
  const login = async (email, password) => {
    const res = await authApi.login(email, password);
    if (res.success && res.data) {
      const freshUser = {
        email: res.data.email || email,
        username: res.data.username || email.split('@')[0],
        role: res.data.role || 'ROLE_USER',
        approvalStatus: res.data.approvalStatus || 'ACTIVE'
      };

      setToken(res.data.token);
      setUser(freshUser);
      localStorage.setItem('fairprice_token', res.data.token);
      localStorage.setItem('fairprice_user', JSON.stringify(freshUser));

      return { success: true, user: freshUser };
    }
    return { success: false, message: res.message || 'Login failed' };
  };

  const register = async (username, email, password, requestAuthorityAccess = false) => {
    const res = await authApi.register(username, email, password, requestAuthorityAccess);
    if (res.success) {
      return login(email, password);
    }
    return res;
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    // Clear both keys so stale role cannot persist into next session
    localStorage.removeItem('fairprice_token');
    localStorage.removeItem('fairprice_user');
  };

  const hasRole = (allowedRoles) => {
    if (!user) return false;
    if (typeof allowedRoles === 'string') return user.role === allowedRoles;
    return allowedRoles.includes(user.role);
  };

  return (
    <AuthContext.Provider value={{ token, user, login, register, logout, hasRole, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  return ctx || defaultAuthContext;
};

// Re-export for convenience so components can import both from the same place
export { getRoleDefaultPath };
