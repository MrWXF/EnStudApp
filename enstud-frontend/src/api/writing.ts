import { get, post } from './client';
import type { SubmitWritingRequest, WritingDTO, ModelEssayDTO, CorrectionDTO } from '../types';

export const submitWriting = (data: SubmitWritingRequest) =>
  post<CorrectionDTO>('/writing/submit', data);

export const getWritingHistory = () =>
  get<WritingDTO[]>('/writing/history');

export const getModels = (topicType = 'ESSAY') =>
  get<ModelEssayDTO[]>('/writing/models', { topicType });
