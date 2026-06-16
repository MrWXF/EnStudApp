import { post } from './client';
import type { TranslateRequest, TranslateResponse } from '../types';

export const translateText = (data: TranslateRequest) =>
  post<TranslateResponse>('/translate/text', data);
