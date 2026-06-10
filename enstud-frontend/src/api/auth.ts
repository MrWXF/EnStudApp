import api from './client';
import type { LoginRequest, RegisterRequest, LoginResult, UserProfile, ApiResponse } from '../types';

export const register = (data: RegisterRequest) =>
  api.post<ApiResponse<LoginResult>>('/user/register', data);

export const login = (data: LoginRequest) =>
  api.post<ApiResponse<LoginResult>>('/user/login', data);

export const getProfile = () =>
  api.get<ApiResponse<UserProfile>>('/user/profile');
