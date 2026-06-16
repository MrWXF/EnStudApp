import { get, post } from './client';
import type { ArticleDTO, ArticleDetailDTO, SourceDTO, WordLookupResponse } from '../types';

export const getHotArticles = (source = 'all') =>
  get<ArticleDTO[]>('/read/hot', { source });

export const getArticleDetail = (articleId: number) =>
  get<ArticleDetailDTO>(`/read/${articleId}`);

export const getArticleTranslation = (articleId: number) =>
  get<ArticleDetailDTO>(`/read/${articleId}/translate`);

export const toggleBookmark = (articleId: number) =>
  post<null>(`/read/${articleId}/bookmark`);

export const getBookmarks = () =>
  get<ArticleDTO[]>('/read/bookmarks');

export const getSources = () =>
  get<SourceDTO[]>('/read/sources');

export const syncArticles = () =>
  post<null>('/read/sync');

/**
 * 划词查词：翻译选中文本并加入生词本
 */
export const wordLookup = (params: { selectedText: string; articleId: number; contextSentence?: string }) =>
  post<WordLookupResponse>('/read/word-lookup', params);
