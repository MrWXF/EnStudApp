import api from './client';
import type { TranslateRequest, TranslateResponse, ApiResponse } from '../types';

export const translateText = (data: TranslateRequest) =>
  api.post<ApiResponse<TranslateResponse>>('/translate/text', data);
