import { get } from './client';
import type { UserStats } from '../types';

export const getUserStats = () =>
  get<UserStats>('/user/stats');

/** AI 每日用量 */
export interface AiUsageDetail {
  usedToday: number;
  dailyLimit: number;
}

export interface AiUsageResponse {
  features: {
    chat: AiUsageDetail;
    writing: AiUsageDetail;
    translate: AiUsageDetail;
  };
}

export const getAiUsage = () =>
  get<AiUsageResponse>('/user/internal/ai-usage/0'); // userId 由网关注入
