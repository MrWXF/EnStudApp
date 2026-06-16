import { get } from './client';
import type { UserStats } from '../types';

export const getUserStats = () =>
  get<UserStats>('/user/stats');
