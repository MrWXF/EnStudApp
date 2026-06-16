import { get, post } from './client';
import type { LoginRequest, RegisterRequest, LoginResult, UserProfile } from '../types';

export const register = (data: RegisterRequest) =>
  post<LoginResult>('/user/register', data);

export const login = (data: LoginRequest) =>
  post<LoginResult>('/user/login', data);

export const getProfile = () =>
  get<UserProfile>('/user/profile');
