// ===== Auth =====
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
}

export interface LoginResult {
  token: string;
  userId: number;
  username: string;
}

export interface UserProfile {
  id: number;
  username: string;
  email: string;
  avatar?: string;
  level: number;
  experience: number;
  createdAt: string;
}

// ===== Word =====
export interface WordbookDTO {
  id: number;
  name: string;
  description?: string;
  coverUrl?: string;
  wordCount: number;
  difficulty?: string;
  category?: string;
  official: boolean;
}

export interface WordCardDTO {
  id: number;
  word: string;
  phoneticUk?: string;
  phoneticUs?: string;
  definitionCn: string;
  definitionEn?: string;
  exampleSentence?: string;
  partOfSpeech?: string;
  masteryLevel: number;
  status: string;
}

export interface AdjustMemoryLevelRequest {
  wordId: number;
  targetLevel: number;
}

export interface MemoryLevelDistribution {
  notLearned: number;
  fuzzy: number;
  familiar: number;
  basic: number;
  proficient: number;
  mastered: number;
}

// ===== Chat =====
export interface ChatSession {
  id: number;
  scenario: string;
  title?: string;
  createdAt: string;
  messageCount: number;
}

export interface MessageDTO {
  id: number;
  sessionId: number;
  role: 'USER' | 'AI';
  content: string;
  grammarIssues?: GrammarIssue[];
  createdAt: string;
}

export interface GrammarIssue {
  start: number;
  end: number;
  original: string;
  suggestion: string;
  explanation: string;
}

export interface SessionDTO {
  id: number;
  title?: string;
  scenario: string;
  createdAt: string;
}

export interface SendMessageResponse {
  message: MessageDTO;
  grammarSuggestions?: string[];
}

// ===== Writing =====
export interface SubmitWritingRequest {
  title: string;
  content: string;
  topicType?: string;
}

export interface CorrectionItemDTO {
  type: string;
  original: string;
  suggestion: string;
  explanation: string;
}

export interface CorrectionDTO {
  writingId: number;
  score: number;
  overallComment: string;
  items: CorrectionItemDTO[];
}

export interface WritingDTO {
  id: number;
  title: string;
  content: string;
  wordCount: number;
  topicType: string;
  score?: number;
  createdAt: string;
}

export interface ModelEssayDTO {
  title: string;
  content: string;
  topicType: string;
  comment: string;
}

// ===== Translate =====
export interface TranslateRequest {
  text: string;
  from?: string;
  to: string;
}

export interface TranslateResponse {
  original: string;
  translated: string;
  from: string;
  to: string;
}

// ===== Forum =====
export interface ForumCategory {
  id: number;
  name: string;
  description?: string;
  postCount: number;
  icon?: string;
}

export interface ForumPost {
  id: number;
  title: string;
  content: string;
  authorName: string;
  categoryName: string;
  replyCount: number;
  likeCount: number;
  isLiked?: boolean;
  createdAt: string;
}

export interface CreatePostRequest {
  title: string;
  content: string;
  categoryId: number;
  tags?: string;
}

export interface PostDetail {
  id: number;
  title: string;
  content: string;
  authorName: string;
  authorId: number;
  categoryName: string;
  likeCount: number;
  replyCount: number;
  isLiked: boolean;
  tags?: string;
  replies: ForumReply[];
  createdAt: string;
}

export interface ForumReply {
  id: number;
  postId: number;
  authorName: string;
  content: string;
  createdAt: string;
}

export interface CreateReplyRequest {
  content: string;
}

// ===== Reading =====
export interface ArticleDTO {
  id: number;
  title: string;
  titleCn?: string;
  url: string;
  source: string;
  sourceIcon?: string;
  summary?: string;
  summaryCn?: string;
  coverUrl?: string;
  author?: string;
  score: number;
  publishedAt?: string;
  bookmarked: boolean;
}

export interface ArticleDetailDTO {
  id: number;
  title: string;
  titleCn?: string;
  url: string;
  source: string;
  sourceIcon?: string;
  author?: string;
  coverUrl?: string;
  content?: string;
  contentCn?: string;
  summary?: string;
  summaryCn?: string;
  score: number;
  sourceScore: number;
  publishedAt?: string;
}

export interface SourceDTO {
  id: string;
  name: string;
  icon?: string;
  articleCount: number;
}

// ===== API 响应 =====
export interface ApiResponse<T> {
  code: number;
  msg: string;
  data: T;
}

// ===== Dashboard =====
export interface UserStats {
  todayLearnedWords: number;
  totalLearnedWords: number;
  totalReadArticles: number;
  totalWritings: number;
  avgWritingScore: number;
  totalChats: number;
  totalPosts: number;
  memoryLevelDistribution: MemoryLevelDistribution;
}
