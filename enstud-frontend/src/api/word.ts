import { get, post } from './client';
import type { WordbookDTO, WordCardDTO } from '../types';

export const getWordbooks = () =>
  get<WordbookDTO[]>('/word/wordbooks');

export const getWords = (wordbookId: number, cursor?: string) =>
  get<WordCardDTO[]>(`/word/wordbooks/${wordbookId}/words`, { cursor, limit: 50 });

export const startStudy = (wordbookId: number, limit = 10) =>
  post<WordCardDTO[]>('/word/study', null, { params: { wordbookId, limit } });

export const submitReview = (wordId: number, quality: number) =>
  post<null>('/word/review', null, { params: { wordId, quality } });

/** 获取用户正在学习的单词列表（用于阅读高亮、Chat 融入提示等） */
export interface LearningWordDTO {
  word: string;
  definitionCn?: string;
  masteryLevel?: number;
}

export const getLearningWords = (limit = 50) =>
  get<LearningWordDTO[]>('/word/learning-words', { limit });
