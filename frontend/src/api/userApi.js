import axiosClient from './axiosClient';

export const userApi = {
  getMyProfile: async () => {
    try {
      const response = await axiosClient.get('/api/v1/users/me');
      if (response.data && response.data.success && response.data.data) {
        return { success: true, data: response.data.data };
      }
      return { success: false, message: response.data?.message || 'Failed to retrieve profile.' };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || 'Unable to load user profile.'
      };
    }
  },

  updateProfile: async (profileData) => {
    try {
      const response = await axiosClient.put('/api/v1/users/me', profileData);
      if (response.data && response.data.success && response.data.data) {
        return { success: true, data: response.data.data, message: response.data.message };
      }
      return { success: false, message: response.data?.message || 'Failed to update profile.' };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || 'Unable to update profile. Please verify your details.'
      };
    }
  },

  verifyPassword: async (currentPassword) => {
    try {
      const response = await axiosClient.post('/api/v1/users/me/verify-password', { currentPassword });
      if (response.data && response.data.success) {
        return { success: true, message: 'Current password verified.' };
      }
      return { success: false, message: response.data?.message || 'Current password is incorrect.' };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || 'Current password is incorrect.'
      };
    }
  },

  changePassword: async (currentPassword, newPassword) => {
    try {
      const response = await axiosClient.post('/api/v1/users/me/change-password', {
        currentPassword,
        newPassword
      });
      if (response.data && response.data.success) {
        return { success: true, message: response.data.message || 'Your password has been updated successfully.' };
      }
      return { success: false, message: response.data?.message || 'Unable to update password.' };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || 'Unable to update password. Please check requirements.'
      };
    }
  }
};
