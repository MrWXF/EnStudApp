import axios from 'axios';
import { message } from 'antd';

const client = axios.create({
  baseURL: '/api',
  timeout: 15000,
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (res) => {
    // 拦截器统一拆解 axios 包装，页面直接使用 ApiResponse<T>
    return res.data;
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
  },
);

export default client;
