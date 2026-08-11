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

/** 开始复习对话 — AI 围绕用户待复习单词生成对话（学习闭环） */
export const startReviewSession = () =>
  post<SendMessageResponse>('/chat/review-session');

/** 单词练习对话 — AI 围绕指定单词生成练习（学习闭环） */
export const startWordPractice = (wordId: number) =>
  post<SendMessageResponse>(`/chat/word-practice/${wordId}`);
