import axios from 'axios';

const API_BASE = '/api/v1';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Redirect to login on 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth
export const authAPI = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
};

// Dashboard
export const dashboardAPI = {
  getStats: () => api.get('/dashboard/stats'),
  getRecentAlerts: (hours = 24) => api.get(`/dashboard/recent-alerts?hours=${hours}`),
};

// Submissions
export const submissionAPI = {
  create: (data) => api.post('/submissions', data),
  getAll: () => api.get('/submissions'),
  getByClinic: (clinicId) => api.get(`/submissions/clinic/${clinicId}`),
  getByDateRange: (start, end) => api.get(`/submissions/date-range?start=${start}&end=${end}`),
  getCategories: () => api.get('/submissions/categories'),
};

// Alerts
export const alertAPI = {
  getAll: () => api.get('/alerts'),
  getById: (id) => api.get(`/alerts/${id}`),
  getByStatus: (status) => api.get(`/alerts/status/${status}`),
  getByClinic: (clinicId) => api.get(`/alerts/clinic/${clinicId}`),
  acknowledge: (id) => api.put(`/alerts/${id}/acknowledge`),
  resolve: (id, notes) => api.put(`/alerts/${id}/resolve`, { resolutionNotes: notes }),
};

// Clinics
export const clinicAPI = {
  getAll: () => api.get('/clinics'),
  getById: (id) => api.get(`/clinics/${id}`),
  create: (data) => api.post('/clinics', data),
  update: (id, data) => api.put(`/clinics/${id}`, data),
};

// Visualization
export const vizAPI = {
  getHeatmap: (startDate, endDate) => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    return api.get(`/visualization/heatmap?${params}`);
  },
  getClinicDrilldown: (clinicId, startDate, endDate) => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    return api.get(`/visualization/heatmap/clinic/${clinicId}?${params}`);
  },
};

// Admin
export const adminAPI = {
  getUsers: () => api.get('/admin/users'),
  updateRole: (id, role) => api.put(`/admin/users/${id}/role`, { role }),
  toggleStatus: (id) => api.put(`/admin/users/${id}/status`),
  getAuditLogs: () => api.get('/admin/audit-logs'),
};

// Settings
export const settingsAPI = {
  getAll: () => api.get('/settings'),
  getByCategory: (category) => api.get(`/settings/category/${category}`),
  update: (key, value) => api.put(`/settings/${key}`, { value }),
};

export default api;
