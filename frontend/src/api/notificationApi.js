import axiosClient from './axiosClient';

export const notificationApi = {
  getNotifications: async (page = 0, size = 20) => {
    try {
      const response = await axiosClient.get('/api/v1/notifications', { params: { page, size } });
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
        error: error.response?.data?.message || 'Failed to fetch notifications'
      };
    }
  },

  getUnreadCount: async () => {
    try {
      const response = await axiosClient.get('/api/v1/notifications/unread-count');
      return {
        success: true,
        count: response.data.data || 0
      };
    } catch (error) {
      return {
        success: false,
        count: 0,
        error: error.response?.data?.message || 'Failed to fetch unread count'
      };
    }
  },

  markAsRead: async (id) => {
    try {
      const response = await axiosClient.patch(`/api/v1/notifications/${id}/read`);
      return {
        success: true,
        data: response.data.data
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to mark notification as read'
      };
    }
  },

  markAllAsRead: async () => {
    try {
      const response = await axiosClient.patch('/api/v1/notifications/read-all');
      return {
        success: true,
        message: response.data.message || 'All notifications marked as read'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to mark all notifications as read'
      };
    }
  }
};
