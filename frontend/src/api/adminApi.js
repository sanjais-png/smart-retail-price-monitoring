import axiosClient from './axiosClient';

export const adminApi = {
  // Platform overview statistics
  getDashboardStats: async () => {
    try {
      const response = await axiosClient.get('/api/v1/admin/dashboard-stats');
      return response.data;
    } catch (error) {
      return { success: false, message: error.response?.data?.message || 'Unable to load platform dashboard metrics.' };
    }
  },

  // Paged authority requests (status = PENDING_AUTHORITY)
  getAuthorityRequests: async (page = 0, size = 10) => {
    try {
      const response = await axiosClient.get(`/api/v1/admin/authority-requests?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      return { success: false, message: error.response?.data?.message || 'Unable to load authority requests.' };
    }
  },

  // Approve an authority access request
  approveAuthorityRequest: async (userId) => {
    try {
      const response = await axiosClient.post(`/api/v1/admin/authority-requests/${userId}/approve`);
      return response.data;
    } catch (error) {
      return { success: false, message: error.response?.data?.message || 'Failed to approve authority request.' };
    }
  },

  // Deny an authority access request
  denyAuthorityRequest: async (userId) => {
    try {
      const response = await axiosClient.post(`/api/v1/admin/authority-requests/${userId}/deny`);
      return response.data;
    } catch (error) {
      return { success: false, message: error.response?.data?.message || 'Failed to deny authority request.' };
    }
  },

  // Filter users by role (e.g. ROLE_AUTHORITY or ROLE_USER)
  getUsersByRole: async (role, page = 0, size = 10) => {
    try {
      const response = await axiosClient.get(`/api/v1/admin/users/by-role?role=${role}&page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      return { success: false, message: error.response?.data?.message || `Unable to load ${role} users.` };
    }
  },

  // Get all registered users (Consumer accounts tab)
  getAllUsers: async (page = 0, size = 10) => {
    try {
      const response = await axiosClient.get(`/api/v1/users?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      return { success: false, message: error.response?.data?.message || 'Unable to load consumer accounts.' };
    }
  },

  // Toggle user account enabled/disabled status
  toggleUserStatus: async (userId, enabled) => {
    try {
      const response = await axiosClient.patch(`/api/v1/users/${userId}/status?enabled=${enabled}`);
      return response.data;
    } catch (error) {
      return { success: false, message: error.response?.data?.message || 'Failed to update user status.' };
    }
  },

  // Get system audit logs
  getAuditLogs: async (page = 0, size = 20) => {
    try {
      const response = await axiosClient.get(`/api/v1/audit-logs?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      return { success: false, message: error.response?.data?.message || 'Unable to load audit logs.' };
    }
  },

  // Get system settings
  getSystemSettings: async () => {
    try {
      const response = await axiosClient.get('/api/v1/admin/settings');
      return response.data;
    } catch (error) {
      return { success: false, message: error.response?.data?.message || 'Unable to load system settings.' };
    }
  },

  // Update a system setting
  updateSystemSetting: async (key, value) => {
    try {
      const response = await axiosClient.put(`/api/v1/admin/settings/${key}`, { settingValue: value });
      return response.data;
    } catch (error) {
      return { success: false, message: error.response?.data?.message || 'Failed to update system setting.' };
    }
  }
};
