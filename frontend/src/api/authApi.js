import axiosClient from './axiosClient';

export const authApi = {
  login: async (usernameOrEmail, password) => {
    try {
      const response = await axiosClient.post('/api/v1/auth/login', {
        usernameOrEmail: usernameOrEmail,
        password: password
      });

      if (response.data && response.data.success && response.data.data) {
        const d = response.data.data;
        return {
          success: true,
          message: response.data.message || 'Login successful',
          data: {
            token: d.accessToken || d.token,
            username: d.username,
            email: d.email,
            role: (d.roles && d.roles.length > 0) ? d.roles[0] : 'ROLE_USER',
            approvalStatus: d.approvalStatus || 'ACTIVE'
          }
        };
      }
      return {
        success: false,
        message: response.data?.message || 'Login failed. Please check your credentials.'
      };
    } catch (error) {
      const errMsg = error.response?.data?.message || 'Invalid email or password. Please verify your credentials or register first.';
      return {
        success: false,
        message: errMsg
      };
    }
  },

  register: async (username, email, password, requestAuthorityAccess = false) => {
    try {
      const response = await axiosClient.post('/api/v1/auth/register', {
        username: username,
        email: email,
        password: password,
        requestAuthorityAccess: requestAuthorityAccess
      });

      if (response.data && response.data.success) {
        return {
          success: true,
          message: response.data.message || 'Account registered successfully in MySQL database!',
          data: response.data.data
        };
      }
      return {
        success: false,
        message: response.data?.message || 'Registration failed'
      };
    } catch (error) {
      const errMsg = error.response?.data?.message || 'Registration failed. User or email may already exist.';
      return {
        success: false,
        message: errMsg
      };
    }
  },

  getProfile: async () => {
    try {
      const response = await axiosClient.get('/api/v1/auth/profile');
      return response.data;
    } catch (error) {
      return null;
    }
  }
};
