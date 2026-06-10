import api from './client';
import type { ChatSession, MessageDTO, SendMessageResponse, ApiResponse } from '../types';

export const createSession = (scenario = 'FREE') =>
  api.post<ApiResponse<ChatSession>>('/chat/sessions', null, { params: { scenario } });

export const getSessions = () =>
  api.get<ApiResponse<ChatSession[]>>('/chat/sessions');

export const sendMessage = (sessionId: number, content: string) =>
  api.post<ApiResponse<SendMessageResponse>>(`/chat/sessions/${sessionId}/messages`, null, { params: { content } });

export const getMessages = (sessionId: number) =>
  api.get<ApiResponse<MessageDTO[]>>(`/chat/sessions/${sessionId}/messages`);

export const deleteSession = (sessionId: number) =>
  api.delete<ApiResponse<null>>(`/chat/sessions/${sessionId}`);
