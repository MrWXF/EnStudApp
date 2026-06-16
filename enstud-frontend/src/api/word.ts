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
