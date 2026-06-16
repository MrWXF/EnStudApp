import { get, post, del } from './client';
import type { ChatSession, MessageDTO, SendMessageResponse } from '../types';

export const createSession = (scenario = 'FREE') =>
  post<ChatSession>('/chat/sessions', null, { params: { scenario } });

export const getSessions = () =>
  get<ChatSession[]>('/chat/sessions');

export const sendMessage = (sessionId: number, content: string) =>
  post<SendMessageResponse>(`/chat/sessions/${sessionId}/messages`, null, { params: { content } });

export const getMessages = (sessionId: number) =>
  get<MessageDTO[]>(`/chat/sessions/${sessionId}/messages`);

export const deleteSession = (sessionId: number) =>
  del<null>(`/chat/sessions/${sessionId}`);
