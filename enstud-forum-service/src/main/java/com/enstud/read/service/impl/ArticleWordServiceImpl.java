package com.enstud.read.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.read.entity.ArticleWord;
import com.enstud.read.mapper.ArticleMapper;
import com.enstud.read.mapper.ArticleWordMapper;
import com.enstud.read.entity.Article;
import com.enstud.read.service.ArticleWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文章-单词关联服务实现
 * <p>
 * 从文章英文内容中提取单词，与词库表匹配，建立关联记录。
 * 由于 read-service 和 word-service 共享同一个数据库，直接查询 enstud_word 表。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleWordServiceImpl implements ArticleWordService {

    private final ArticleWordMapper articleWordMapper;
    private final ArticleMapper articleMapper;
    private final JdbcTemplate jdbcTemplate;

    /** 匹配英文单词的正则 */
    private static final Pattern WORD_PATTERN = Pattern.compile("[a-zA-Z]{3,}");

    /** 停用词（不建立关联） */
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "are", "but", "not", "you", "all",
            "can", "had", "her", "was", "one", "our", "out", "has",
            "have", "some", "them", "than", "its", "over",
            "such", "that", "this", "with", "will", "each", "make",
            "like", "long", "look", "many", "most", "from", "into",
            "what", "when", "where", "which", "their", "there", "would",
            "could", "should", "about", "these", "other", "write", "been"
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int extractAndLinkWords(Long articleId, String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }

        // 1. 提取文章中的所有单词（小写，去重，统计出现次数）
        Map<String, Integer> wordCounts = extractWords(content);

        if (wordCounts.isEmpty()) {
            return 0;
        }

        // 2. 批量查询词库，找到匹配的单词
        List<String> candidateWords = new ArrayList<>(wordCounts.keySet());
        Map<String, Long> matchedWords = lookupWordsInDictionary(candidateWords);

        if (matchedWords.isEmpty()) {
            return 0;
        }

        // 3. 建立关联记录
        int count = 0;
        for (Map.Entry<String, Long> entry : matchedWords.entrySet()) {
            String wordText = entry.getKey();
            Long wordId = entry.getValue();
            int occurrences = wordCounts.getOrDefault(wordText, 1);

            ArticleWord aw = new ArticleWord();
            aw.setArticleId(articleId);
            aw.setWordId(wordId);
            aw.setWordText(wordText);
            aw.setOccurrenceCount(occurrences);
            articleWordMapper.insert(aw);
            count++;
        }

        log.info("Article {} linked with {} words", articleId, count);
        return count;
    }

    @Override
    public void processPendingArticles() {
        // 查找所有还没有词汇关联的文章
        List<Article> allArticles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .isNotNull(Article::getContent)
                        .last("LIMIT 50")
        );

        // 获取已有关联的文章ID
        Set<Long> processedIds = articleWordMapper.selectList(null)
                .stream().map(ArticleWord::getArticleId).collect(Collectors.toSet());

        int processed = 0;
        for (Article article : allArticles) {
            if (!processedIds.contains(article.getId())) {
                try {
                    extractAndLinkWords(article.getId(), article.getContent());
                    processed++;
                } catch (Exception e) {
                    log.error("Failed to extract words for article {}", article.getId(), e);
                }
            }
        }
        log.info("Processed {} articles for word extraction", processed);
    }

    /**
     * 从文本中提取英文单词
     */
    private Map<String, Integer> extractWords(String content) {
        Map<String, Integer> counts = new HashMap<>();
        Matcher matcher = WORD_PATTERN.matcher(content.toLowerCase());
        while (matcher.find()) {
            String word = matcher.group();
            if (!STOP_WORDS.contains(word) && word.length() >= 3) {
                counts.merge(word, 1, Integer::sum);
            }
        }
        return counts;
    }

    /**
     * 在词库中查找匹配的单词（共享数据库直接查询）
     */
    private Map<String, Long> lookupWordsInDictionary(List<String> words) {
        if (words.isEmpty()) return Map.of();

        // 分批查询，避免 SQL 过长
        Map<String, Long> result = new HashMap<>();
        int batchSize = 100;
        for (int i = 0; i < words.size(); i += batchSize) {
            List<String> batch = words.subList(i, Math.min(i + batchSize, words.size()));
            String placeholders = batch.stream().map(w -> "?").collect(Collectors.joining(","));
            String sql = "SELECT id, word FROM enstud_word WHERE LOWER(word) IN (" + placeholders + ") AND is_deleted = 0";

            try {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, batch.toArray());
                for (Map<String, Object> row : rows) {
                    Long id = ((Number) row.get("id")).longValue();
                    String word = ((String) row.get("word")).toLowerCase();
                    result.put(word, id);
                }
            } catch (Exception e) {
                log.warn("Failed to lookup words in dictionary", e);
            }
        }
        return result;
    }
}
