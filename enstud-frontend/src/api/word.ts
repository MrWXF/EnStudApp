import api from './client';
import type { WordbookDTO, WordCardDTO, ApiResponse } from '../types';

export const getWordbooks = () =>
  api.get<ApiResponse<WordbookDTO[]>>('/word/wordbooks');

export const getWords = (wordbookId: number, cursor?: string) =>
  api.get<ApiResponse<WordCardDTO[]>>(`/word/wordbooks/${wordbookId}/words`, { params: { cursor, limit: 50 } });

export const startStudy = (wordbookId: number, limit = 10) =>
  api.post<ApiResponse<WordCardDTO[]>>('/word/study', null, { params: { wordbookId, limit } });

export const submitReview = (wordId: number, quality: number) =>
  api.post<ApiResponse<null>>('/word/review', null, { params: { wordId, quality } });
