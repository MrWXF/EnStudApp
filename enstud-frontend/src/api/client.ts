import axios from 'axios';
import { message } from 'antd';

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (res) => {
    const data = res.data as any;
    // 统一处理业务错误码
    if (data && data.code !== undefined && data.code !== 0) {
      message.error(data.msg || '请求失败');
      return Promise.reject(data);
    }
    return data;
  },
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      message.error('登录已过期，请重新登录');
      window.location.href = '/login';
    } else {
      message.error(err.response?.data?.msg || '网络错误，请稍后重试');
    }
    return Promise.reject(err.response?.data || err);
  }
);

export default api;
