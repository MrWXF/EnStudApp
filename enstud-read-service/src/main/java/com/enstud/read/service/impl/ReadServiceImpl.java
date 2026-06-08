package com.enstud.read.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.read.ai.ArticleAggregator;
import com.enstud.read.dto.*;
import com.enstud.read.entity.Article;
import com.enstud.read.entity.ArticleReadRecord;
import com.enstud.read.mapper.ArticleMapper;
import com.enstud.read.mapper.ArticleReadRecordMapper;
import com.enstud.read.service.ReadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReadServiceImpl implements ReadService {

    private static final Logger log = LoggerFactory.getLogger(ReadServiceImpl.class);

    private final ArticleMapper articleMapper;
    private final ArticleReadRecordMapper readRecordMapper;
    private final ArticleAggregator articleAggregator;

    public ReadServiceImpl(ArticleMapper articleMapper,
                           ArticleReadRecordMapper readRecordMapper,
                           ArticleAggregator articleAggregator) {
        this.articleMapper = articleMapper;
        this.readRecordMapper = readRecordMapper;
        this.articleAggregator = articleAggregator;
    }

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
     * 模拟翻译（实际接入 translate-service）
     */
    private void translateArticle(Article article) {
        // 模拟翻译标题
        if (article.getTitle() != null && !article.getTitle().contains("（")) {
            article.setTitleCn("（" + article.getTitle() + " 的中文翻译）");
        }
        // 模拟翻译摘要
        if (article.getSummary() != null && !article.getSummary().startsWith("（")) {
            String cn = article.getSummary().length() > 50
                    ? article.getSummary().substring(0, 50) + "..."
                    : article.getSummary();
            article.setSummaryCn("（" + cn + " 的中文翻译）");
        }
        // 模拟翻译正文
        if (article.getContent() != null && article.getContentCn() == null) {
            String cn = article.getContent().length() > 100
                    ? article.getContent().substring(0, 100) + "……"
                    : article.getContent();
            article.setContentCn("（" + cn + " 的中文翻译）");
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
}
