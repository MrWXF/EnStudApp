import api from './client';

// Auth
export const register = (data: { username: string; password: string; email: string }) =>
  api.post('/user/register', data);
export const login = (data: { username: string; password: string }) =>
  api.post('/user/login', data);
export const getProfile = () => api.get('/user/profile');

// Words
export const getWordbooks = () => api.get('/word/wordbooks');
export const getWords = (wordbookId: number, cursor?: string) =>
  api.get(`/word/wordbooks/${wordbookId}/words`, { params: { cursor, limit: 50 } });
export const startStudy = (wordbookId: number, limit = 10) =>
  api.post('/word/study', null, { params: { wordbookId, limit } });
export const submitReview = (wordId: number, quality: number) =>
  api.post('/word/review', null, { params: { wordId, quality } });

// Chat
export const createSession = (scenario = 'FREE') =>
  api.post('/chat/sessions', null, { params: { scenario } });
export const getSessions = () => api.get('/chat/sessions');
export const sendMessage = (sessionId: number, content: string) =>
  api.post(`/chat/sessions/${sessionId}/messages`, null, { params: { content } });
export const getMessages = (sessionId: number) =>
  api.get(`/chat/sessions/${sessionId}/messages`);
export const deleteSession = (sessionId: number) =>
  api.delete(`/chat/sessions/${sessionId}`);

// Writing
export const submitWriting = (data: { title: string; content: string; topicType?: string }) =>
  api.post('/writing/submit', data);
export const getWritingHistory = () => api.get('/writing/history');
export const getModels = (topicType = 'ESSAY') =>
  api.get('/writing/models', { params: { topicType } });

// Translate
export const translateText = (data: { text: string; from?: string; to: string }) =>
  api.post('/translate/text', data);

// Forum
export const getCategories = () => api.get('/forum/categories');
export const getPosts = (categoryId?: number, cursor?: string) =>
  api.get('/forum/posts', { params: { categoryId, cursor, limit: 20 } });
export const getPostDetail = (postId: number) => api.get(`/forum/posts/${postId}`);
export const createPost = (data: { title: string; content: string; categoryId: number; tags?: string }) =>
  api.post('/forum/posts', data);
export const deletePost = (postId: number) => api.delete(`/forum/posts/${postId}`);
export const replyPost = (postId: number, content: string) =>
  api.post(`/forum/posts/${postId}/reply`, { content });
export const toggleLike = (targetType: string, targetId: number) =>
  api.post('/forum/like', null, { params: { targetType, targetId } });
