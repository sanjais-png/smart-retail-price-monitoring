import axiosClient from './axiosClient';

export const alertApi = {
  createAlert: async (alertData) => {
    try {
      const response = await axiosClient.post('/api/v1/alerts', alertData);
      return {
        success: true,
        data: response.data.data,
        message: response.data.message || 'Price alert created successfully'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to create price alert'
      };
    }
  },

  getUserAlerts: async (page = 0, size = 20) => {
    try {
      const response = await axiosClient.get('/api/v1/alerts', { params: { page, size } });
      const pagedData = response.data.data;
      return {
        success: true,
        data: pagedData.content || [],
        totalElements: pagedData.totalElements || 0,
        totalPages: pagedData.totalPages || 0
      };
    } catch (error) {
      return {
        success: false,
        data: [],
        error: error.response?.data?.message || 'Failed to fetch user alerts'
      };
    }
  },

  getAlertById: async (id) => {
    try {
      const response = await axiosClient.get(`/api/v1/alerts/${id}`);
      return {
        success: true,
        data: response.data.data
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Alert not found'
      };
    }
  },

  toggleAlert: async (id, active) => {
    try {
      const response = await axiosClient.patch(`/api/v1/alerts/${id}/toggle`, null, {
        params: { active }
      });
      return {
        success: true,
        data: response.data.data,
        message: response.data.message
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to update alert status'
      };
    }
  },

  deleteAlert: async (id) => {
    try {
      const response = await axiosClient.delete(`/api/v1/alerts/${id}`);
      return {
        success: true,
        message: response.data.message || 'Price alert deleted successfully'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to delete price alert'
      };
    }
  }
};
