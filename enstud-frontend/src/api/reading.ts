import api from './client';
import type { ArticleDTO, ArticleDetailDTO, SourceDTO, ApiResponse } from '../types';

export const getHotArticles = (source = 'all') =>
  api.get<ApiResponse<ArticleDTO[]>>('/read/hot', { params: { source } });

export const getArticleDetail = (articleId: number) =>
  api.get<ApiResponse<ArticleDetailDTO>>(`/read/${articleId}`);

export const getArticleTranslation = (articleId: number) =>
  api.get<ApiResponse<string>>(`/read/${articleId}/translate`);

export const toggleBookmark = (articleId: number) =>
  api.post<ApiResponse<null>>(`/read/${articleId}/bookmark`);

export const getBookmarks = () =>
  api.get<ApiResponse<ArticleDTO[]>>('/read/bookmarks');

export const getSources = () =>
  api.get<ApiResponse<SourceDTO[]>>('/read/sources');

export const syncArticles = () =>
  api.post<ApiResponse<null>>('/read/sync');
