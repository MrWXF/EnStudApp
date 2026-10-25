import { get, post } from './client';
import type { VocabTestResult, VocabTestWord, WordbookDTO, WordCardDTO } from '../types';

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

// ===== 词汇量测试 =====

/** 获取一组测试单词（6 频段 × 5 词 + 2 伪词，共 32 词） */
export const getVocabTestWords = () =>
  get<VocabTestWord[]>('/word/vocab-test/words');

/** 提交作答：knowledge 2=认识, 1=模糊, 0=不认识 */
export const submitVocabTest = (answers: { word: string; knowledge: number }[]) =>
  post<VocabTestResult>('/word/vocab-test/submit', { answers });

/** 最近一次测试结果 */
export const getLatestVocabResult = () =>
  get<VocabTestResult | null>('/word/vocab-test/latest');
