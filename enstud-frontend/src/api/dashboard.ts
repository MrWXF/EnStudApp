import { get, post } from './client';
import type { CheckinResult, DailyJourney, UserStats } from '../types';

export const getUserStats = () =>
  get<UserStats>('/user/stats');

/** 今日学习之旅：四项任务（阅读/复习/对话/打卡）进度 */
export const getDailyJourney = () =>
  get<DailyJourney>('/user/daily-journey');

/** 每日打卡（获得积分奖励） */
export const checkin = () =>
  post<CheckinResult>('/user/goal/checkin');

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
