import api from './client';
import type { UserStats, ApiResponse } from '../types';

export const getUserStats = () =>
  api.get<ApiResponse<UserStats>>('/user/stats');
