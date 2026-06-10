import api from './client';
import type {
  ForumCategory,
  ForumPost,
  PostDetail,
  CreatePostRequest,
  ApiResponse,
} from '../types';

export const getCategories = () =>
  api.get<ApiResponse<ForumCategory[]>>('/forum/categories');

export const getPosts = (categoryId?: number, cursor?: string) =>
  api.get<ApiResponse<ForumPost[]>>('/forum/posts', { params: { categoryId, cursor, limit: 20 } });

export const getPostDetail = (postId: number) =>
  api.get<ApiResponse<PostDetail>>(`/forum/posts/${postId}`);

export const createPost = (data: CreatePostRequest) =>
  api.post<ApiResponse<ForumPost>>('/forum/posts', data);

export const deletePost = (postId: number) =>
  api.delete<ApiResponse<null>>(`/forum/posts/${postId}`);

export const replyPost = (postId: number, content: string) =>
  api.post<ApiResponse<ForumPost>>(`/forum/posts/${postId}/reply`, { content });

export const toggleLike = (targetType: string, targetId: number) =>
  api.post<ApiResponse<null>>('/forum/like', null, { params: { targetType, targetId } });
