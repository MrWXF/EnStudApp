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
  accessToken: string;
  userId: number;
  username: string;
  nickname: string;
  // email is NOT in backend LoginResultDTO — only shown if needed
}

export interface UserProfile {
  id: number;
  username: string;
  email: string;
  avatarUrl?: string;    // BE field name (was avatar)
  level: number;
  points: number;        // BE field name (was experience)
  nickname?: string;     // BE has it but FE ignores
  lastLoginAt?: string;  // BE has it but FE ignores
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
  isOfficial: boolean;   // BE field name (was official)
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
  role: 'USER' | 'AI';
  content: string;
  grammarIssues?: GrammarIssue[];
  createdAt: string;
  // sessionId is NOT in BE MessageDTO
}

export interface GrammarIssue {
  error: string;         // BE field (was original)
  correction: string;    // BE field (was suggestion)
  explanation: string;
}

export interface SessionDTO {
  id: number;
  title?: string;
  scenario: string;
  createdAt: string;
}

export interface SendMessageResponse {
  userMessage: MessageDTO;  // BE returns userMessage + aiMessage (not single message)
  aiMessage: MessageDTO;    // BE returns userMessage + aiMessage
  grammarIssues: GrammarIssue[]; // BE field name (was grammarSuggestions)
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
  analysis: string;  // BE field name (was comment)
}

// ===== Translate =====
export interface TranslateRequest {
  text: string;
  from?: string;
  to: string;
}

export interface TranslateResponse {
  sourceText: string;
  translatedText: string;  // BE field name
  from: string;
  to: string;
}

// ===== Forum =====
export interface ForumCategory {
  id: number;
  name: string;
  description?: string;
  icon?: string;
  // postCount is NOT in BE CategoryDTO
}

export interface ForumPost {
  id: number;
  title: string;
  summary: string;      // BE returns summary in list (was content)
  authorName: string;
  authorId: number;     // BE has it
  categoryId: number;   // BE has it
  categoryName: string;
  replyCount: number;
  likeCount: number;
  viewCount: number;
  isPinned: boolean;
  isEssence: boolean;
  tags?: string;
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
  categoryId: number;
  categoryName: string;
  likeCount: number;
  replyCount: number;
  viewCount: number;
  isPinned: boolean;
  isEssence: boolean;
  tags?: string;
  replies: ForumReply[];
  createdAt: string;
}

export interface ForumReply {
  id: number;
  authorName: string;
  authorId: number;
  content: string;
  likeCount: number;
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
  // bookmarked is NOT in BE ArticleDetailDTO (only in list ArticleDTO)
}

export interface SourceDTO {
  id: string;
  name: string;
  icon: string;
  activeCount: number;  // BE field name (was articleCount)
}

/** 划词查词响应 */
export interface WordLookupResponse {
  originalWord: string;
  wordCount: number;
  translation: string;
  phonetic: string;
  partOfSpeech: string;
  addedToWordbook: boolean;
  wordRecordId: number | null;
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
  streakDays: number;
  memoryLevelDistribution: MemoryLevelDistribution;
}

export interface MemoryLevelDistribution {
  [level: string]: number;
}
