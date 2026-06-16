import { get, post, del } from './client';
import type {
  ForumCategory,
  ForumPost,
  PostDetail,
  CreatePostRequest,
} from '../types';

export const getCategories = () =>
  get<ForumCategory[]>('/forum/categories');

export const getPosts = (categoryId?: number, cursor?: string) =>
  get<ForumPost[]>('/forum/posts', { categoryId, cursor, limit: 20 });

export const getPostDetail = (postId: number) =>
  get<PostDetail>(`/forum/posts/${postId}`);

export const createPost = (data: CreatePostRequest) =>
  post<ForumPost>('/forum/posts', data);

export const deletePost = (postId: number) =>
  del<null>(`/forum/posts/${postId}`);

export const replyPost = (postId: number, content: string) =>
  post<ForumPost>(`/forum/posts/${postId}/reply`, { content });

export const toggleLike = (targetType: string, targetId: number) =>
  post<null>('/forum/like', null, { params: { targetType, targetId } });
