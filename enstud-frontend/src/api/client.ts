import axios from 'axios';
import { message } from 'antd';
import type { ApiResponse } from '../types';

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

// 包装 axios 方法，返回正确的 Promise<ApiResponse<T>> 类型
// 拦截器已在运行时拆包，这里仅修正 TypeScript 类型推断
export async function get<T>(
  url: string,
  params?: Record<string, unknown>,
): Promise<ApiResponse<T>> {
  return client.get(url, { params }) as unknown as Promise<ApiResponse<T>>;
}

export async function post<T>(
  url: string,
  data?: unknown,
  config?: Record<string, unknown>,
): Promise<ApiResponse<T>> {
  return client.post(url, data, config) as unknown as Promise<ApiResponse<T>>;
}

export async function del<T>(
  url: string,
): Promise<ApiResponse<T>> {
  return client.delete(url) as unknown as Promise<ApiResponse<T>>;
}

export default client;
