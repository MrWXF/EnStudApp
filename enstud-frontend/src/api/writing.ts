import api from './client';
import type { SubmitWritingRequest, WritingDTO, ModelEssayDTO, CorrectionDTO, ApiResponse } from '../types';

export const submitWriting = (data: SubmitWritingRequest) =>
  api.post<ApiResponse<CorrectionDTO>>('/writing/submit', data);

export const getWritingHistory = () =>
  api.get<ApiResponse<WritingDTO[]>>('/writing/history');

export const getModels = (topicType = 'ESSAY') =>
  api.get<ApiResponse<ModelEssayDTO[]>>('/writing/models', { params: { topicType } });
