package com.enstud.read.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.common.ai.DeepSeekClient;
import com.enstud.common.entity.Word;
import com.enstud.common.feign.WordServiceClient;
import com.enstud.common.Result;
import com.enstud.read.ai.ArticleAggregator;
import com.enstud.read.dto.*;
import com.enstud.read.entity.Article;
import com.enstud.read.entity.ArticleReadRecord;
import com.enstud.read.mapper.ArticleMapper;
import com.enstud.read.mapper.ArticleReadRecordMapper;
import com.enstud.read.mapper.ArticleWordMapper;
import com.enstud.read.service.ReadService;
import com.enstud.word.dto.AddWordFromReadingRequest;
import com.enstud.word.mapper.WordMapper;
import com.enstud.word.service.WordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReadServiceImpl implements ReadService {

    private final ArticleMapper articleMapper;
    private final ArticleReadRecordMapper readRecordMapper;
    private final ArticleAggregator articleAggregator;
    private final ArticleWordMapper articleWordMapper;
    private final WordServiceClient wordServiceClient;

    /** DeepSeek 客户端（文章翻译与划词查词） */
    private final DeepSeekClient deepSeekClient;

    /** 单词服务（同服务内直调，加入生词本） */
    private final WordService wordService;

    /** 词库 Mapper（划词优先查本地词典） */
    private final WordMapper wordMapper;

    /** 翻译提示词（英文 → 中文） */
    private static final String TRANSLATE_PROMPT = """
            You are a professional translator. Translate the user's English text into natural Chinese.
            Output ONLY the translation, no explanations or quotes. Preserve line breaks and formatting.
            """;

    /** 单次翻译的最大字符数（防止超长正文超时） */
    private static final int MAX_TRANSLATE_LENGTH = 8000;

    /** 判定为"单个单词"的正则（可加入生词本） */
    private static final String SINGLE_WORD_PATTERN = "^[a-zA-Z][a-zA-Z\\-']{1,49}$";

    @Override
    public List<ArticleDTO> getHotArticles(Long userId, String source) {
        List<Article> articles;
        if (source != null && !source.isBlank() && !"all".equalsIgnoreCase(source)) {
            articles = articleMapper.selectBySource(source);
        } else {
            articles = articleMapper.selectHotArticles();
        }

        // 获取用户已收藏的文章 ID 集合
        Set<Long> bookmarkedIds = getBookmarkedArticleIds(userId);

        return articles.stream()
                .map(a -> toDTO(a, bookmarkedIds.contains(a.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public ArticleDetailDTO getArticleDetail(Long userId, Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) return null;

        // 记录阅读次数
        recordRead(userId, articleId);

        return toDetailDTO(article);
    }

    @Override
    public ArticleDetailDTO getArticleTranslation(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) return null;

        // 如果还没翻译，这里调用翻译服务（mock）
        if (article.getTitleCn() == null || article.getTitleCn().isBlank()) {
            translateArticle(article);
            articleMapper.updateById(article);
        }

        return toDetailDTO(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleBookmark(Long userId, Long articleId) {
        ArticleReadRecord record = readRecordMapper.selectByUserAndArticle(userId, articleId);
        if (record == null) {
            record = new ArticleReadRecord();
            record.setUserId(userId);
            record.setArticleId(articleId);
            record.setReadCount(1);
            record.setIsBookmarked(true);
            readRecordMapper.insert(record);
            return true;
        } else {
            record.setIsBookmarked(!Boolean.TRUE.equals(record.getIsBookmarked()));
            readRecordMapper.updateById(record);
            return Boolean.TRUE.equals(record.getIsBookmarked());
        }
    }

    @Override
    public List<ArticleDTO> getBookmarks(Long userId) {
        List<ArticleReadRecord> records = readRecordMapper.selectBookmarksByUserId(userId);
        if (records.isEmpty()) return Collections.emptyList();

        Set<Long> ids = records.stream().map(ArticleReadRecord::getArticleId).collect(Collectors.toSet());

        return articleMapper.selectBatchIds(ids).stream()
                .map(a -> toDTO(a, true))
                .collect(Collectors.toList());
    }

    @Override
    public List<SourceDTO> getSources(Long userId) {
        List<Map<String, Object>> counts = articleMapper.countBySource();
        Map<String, Integer> countMap = new HashMap<>();
        for (Map<String, Object> row : counts) {
            countMap.put((String) row.get("source"), ((Number) row.get("cnt")).intValue());
        }

        return articleAggregator.getSources().stream()
                .map(s -> new SourceDTO(
                        s.id(), s.name(), s.icon(),
                        countMap.getOrDefault(s.id(), 0)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncArticles() {
        ArticleAggregator.AggregatedResult result = articleAggregator.fetchAll();
        int saved = 0;

        for (Article article : result.articles()) {
            try {
                // 去重：按 URL+source 判断是否已存在
                Article existing = articleMapper.selectOne(
                        new LambdaQueryWrapper<Article>()
                                .eq(Article::getUrl, article.getUrl())
                                .eq(Article::getSource, article.getSource())
                                .last("LIMIT 1")
                );

                if (existing != null) {
                    // 更新热度分
                    existing.setSourceScore(article.getSourceScore());
                    existing.setScore(calcScore(article));
                    articleMapper.updateById(existing);
                } else {
                    // 计算热度分
                    article.setScore(calcScore(article));
                    article.setFetchedAt(LocalDateTime.now());
                    articleMapper.insert(article);
                    saved++;
                }
            } catch (Exception e) {
                log.warn("Failed to save article: {} - {}", article.getTitle(), e.getMessage());
            }
        }
        // 重新计算所有文章热度分（时间衰减）
        recalcAllScores();

        log.info("Sync completed: {} new, {} total from all sources", saved, result.articles().size());
        return saved;
    }

    // ===== 内部方法 =====

    /**
     * 热度计算：原始分 × 时间衰减系数
     * 衰减系数 = 1 / (1 + 0.1 × 天数差)
     */
    private int calcScore(Article article) {
        if (article.getSourceScore() == null) return 0;
        if (article.getPublishedAt() == null) return article.getSourceScore();

        long daysDiff = ChronoUnit.DAYS.between(article.getPublishedAt(), LocalDateTime.now());
        daysDiff = Math.max(0, daysDiff);
        double decay = 1.0 / (1.0 + 0.1 * daysDiff);
        return (int) Math.round(article.getSourceScore() * decay);
    }

    /**
     * 重新计算所有文章热度分
     */
    private void recalcAllScores() {
        List<Article> all = articleMapper.selectList(null);
        for (Article a : all) {
            a.setScore(calcScore(a));
        }
        // 批量更新（简化版：逐条更新，量不大）
        all.forEach(articleMapper::updateById);
    }

    /**
     * 调用 DeepSeek 翻译文章（结果持久化到 titleCn/summaryCn/contentCn，全局缓存）
     */
    private void translateArticle(Article article) {
        try {
            if ((article.getTitleCn() == null || article.getTitleCn().isBlank())
                    && article.getTitle() != null && !article.getTitle().isBlank()) {
                article.setTitleCn(translateEnToCn(article.getTitle(), null));
            }
            if ((article.getSummaryCn() == null || article.getSummaryCn().isBlank())
                    && article.getSummary() != null && !article.getSummary().isBlank()) {
                article.setSummaryCn(translateEnToCn(article.getSummary(), null));
            }
            if ((article.getContentCn() == null || article.getContentCn().isBlank())
                    && article.getContent() != null && !article.getContent().isBlank()) {
                String content = article.getContent();
                if (content.length() > MAX_TRANSLATE_LENGTH) {
                    content = content.substring(0, MAX_TRANSLATE_LENGTH) + "\n\n（正文过长，仅翻译前部分）";
                }
                article.setContentCn(translateEnToCn(content, null));
            }
        } catch (Exception e) {
            // 翻译失败不阻塞阅读流程，下次访问再试
            log.warn("Article translation failed, id={}: {}", article.getId(), e.getMessage());
        }
    }

    private Set<Long> getBookmarkedArticleIds(Long userId) {
        if (userId == null) return Collections.emptySet();
        List<ArticleReadRecord> records = readRecordMapper.selectBookmarksByUserId(userId);
        return records.stream().map(ArticleReadRecord::getArticleId).collect(Collectors.toSet());
    }

    private void recordRead(Long userId, Long articleId) {
        if (userId == null) return;
        ArticleReadRecord record = readRecordMapper.selectByUserAndArticle(userId, articleId);
        if (record == null) {
            record = new ArticleReadRecord();
            record.setUserId(userId);
            record.setArticleId(articleId);
            record.setReadCount(1);
            record.setIsBookmarked(false);
            readRecordMapper.insert(record);
        } else {
            readRecordMapper.incrementReadCount(record.getId());
        }
    }

    private ArticleDTO toDTO(Article a, boolean bookmarked) {
        return new ArticleDTO(
                a.getId(), a.getTitle(), a.getTitleCn(),
                a.getUrl(), a.getSource(), a.getSourceIcon(),
                a.getSummary(), a.getSummaryCn(),
                a.getCoverUrl(), a.getAuthor(),
                a.getScore(), a.getPublishedAt(), bookmarked
        );
    }

    private ArticleDetailDTO toDetailDTO(Article a) {
        return new ArticleDetailDTO(
                a.getId(), a.getTitle(), a.getTitleCn(),
                a.getUrl(), a.getSource(), a.getSourceIcon(),
                a.getAuthor(), a.getCoverUrl(),
                a.getContent(), a.getContentCn(),
                a.getSummary(), a.getSummaryCn(),
                a.getScore(), a.getSourceScore(), a.getPublishedAt()
        );
    }

    @Override
    public WordLookupResponse wordLookup(Long userId, WordLookupRequest request) {
        String text = request.getSelectedText().trim();
        boolean isSingleWord = text.matches(SINGLE_WORD_PATTERN);

        // 1. 优先查本地词库（命中即免 AI 调用，且自带音标/词性）
        Word dictWord = null;
        if (isSingleWord) {
            dictWord = wordMapper.selectOne(
                    new LambdaQueryWrapper<Word>()
                            .eq(Word::getWord, text.toLowerCase())
                            .last("LIMIT 1"));
        }

        // 2. 词典未命中再调 DeepSeek 翻译
        String translation;
        if (dictWord != null && dictWord.getDefinitionCn() != null && !dictWord.getDefinitionCn().isBlank()) {
            translation = dictWord.getDefinitionCn();
        } else {
            translation = translateEnToCn(text, userId);
        }

        // 3. 尝试加入生词本（单词服务本地直调，不再走 HTTP）
        Long wordRecordId = null;
        if (userId != null && isSingleWord) {
            try {
                AddWordFromReadingRequest addReq = new AddWordFromReadingRequest(
                        text.toLowerCase(), translation, null, request.getContextSentence(),
                        dictWord != null ? dictWord.getPartOfSpeech() : null);
                wordRecordId = wordService.addWordFromReading(userId, addReq);
            } catch (Exception e) {
                log.warn("Failed to add word '{}' to wordbook for user {}: {}",
                        text, userId, e.getMessage());
                // 加入生词本失败不阻塞查词流程，用户仍能看到翻译
            }
        }

        // 4. 构建响应
        WordLookupResponse resp = new WordLookupResponse();
        resp.setOriginalWord(text);
        resp.setWordCount(text.split("\\s+").length);
        resp.setTranslation(translation);
        resp.setPhonetic(dictWord != null && dictWord.getPhoneticUs() != null ? dictWord.getPhoneticUs() : "");
        resp.setPartOfSpeech(dictWord != null && dictWord.getPartOfSpeech() != null ? dictWord.getPartOfSpeech() : "");
        resp.setAddedToWordbook(wordRecordId != null);
        resp.setWordRecordId(wordRecordId);

        log.info("Word lookup: user={}, word='{}', translation='{}', added={}",
                userId, text, translation, wordRecordId != null);
        return resp;
    }

    /**
     * DeepSeek 英译中
     *
     * @param userId 用户 ID（非空时计入 translate 每日配额；文章翻译已全局缓存故传 null）
     */
    private String translateEnToCn(String text, Long userId) {
        return deepSeekClient.chat(TRANSLATE_PROMPT, text, userId, userId != null ? "translate" : null);
    }

    // ==================== 学习闭环：基于学习单词推荐文章 ====================

    @Override
    public List<ArticleDTO> getRecommendedArticles(Long userId, int limit) {
        // 1. 获取用户正在学习的单词
        List<String> learningWords = fetchLearningWords(userId);

        if (learningWords.isEmpty()) {
            // 没有学习单词，返回热门作为推荐
            return getHotArticles(userId, "all").stream().limit(limit).toList();
        }

        // 2. 查找包含这些单词的文章（按匹配数量降序）
        var matchedArticleWords = articleWordMapper.findArticlesByWords(learningWords, limit);
        Set<Long> recommendedIds = matchedArticleWords.stream()
                .map(aw -> aw.getArticleId())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 3. 如果推荐文章不够，补充热门文章
        if (recommendedIds.size() < limit) {
            List<Article> hotArticles = articleMapper.selectList(
                    new LambdaQueryWrapper<Article>()
                            .orderByDesc(Article::getScore)
                            .last("LIMIT " + (limit - recommendedIds.size() + 10))
            );
            for (Article a : hotArticles) {
                if (recommendedIds.size() >= limit) break;
                recommendedIds.add(a.getId());
            }
        }

        // 4. 查询文章详情并组装 DTO
        List<Article> articles = articleMapper.selectBatchIds(recommendedIds);
        Map<Long, Article> articleMap = articles.stream()
                .collect(Collectors.toMap(Article::getId, a -> a));

        // 查询收藏状态
        Set<Long> bookmarkedIds = getBookmarkedIds(userId, recommendedIds);

        // 保持推荐顺序
        return recommendedIds.stream()
                .map(articleMap::get)
                .filter(Objects::nonNull)
                .limit(limit)
                .map(a -> toDTO(a, bookmarkedIds.contains(a.getId())))
                .toList();
    }

    private Set<Long> getBookmarkedIds(Long userId, Set<Long> articleIds) {
        if (userId == null || articleIds.isEmpty()) return Set.of();
        return readRecordMapper.selectList(
                new LambdaQueryWrapper<ArticleReadRecord>()
                        .eq(ArticleReadRecord::getUserId, userId)
                        .in(ArticleReadRecord::getArticleId, articleIds)
        ).stream().map(ArticleReadRecord::getArticleId).collect(Collectors.toSet());
    }

    private List<String> fetchLearningWords(Long userId) {
        try {
            Result<List<Map<String, Object>>> result = wordServiceClient.getLearningWords(userId, 20);
            if (result != null && result.getData() != null) {
                return result.getData().stream()
                        .map(m -> String.valueOf(m.get("word")).toLowerCase())
                        .filter(w -> w != null && !w.isEmpty() && !"null".equals(w))
                        .toList();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch learning words for recommendation", e);
        }
        return List.of();
    }
}
