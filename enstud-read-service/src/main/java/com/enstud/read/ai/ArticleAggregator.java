package com.enstud.read.ai;

import com.enstud.read.entity.Article;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 文章聚合管理器
 * 统一管理所有来源的抓取器
 */
@Component
public class ArticleAggregator {

    private static final Logger log = LoggerFactory.getLogger(ArticleAggregator.class);
    private final List<ArticleFetcher> fetchers;

    public ArticleAggregator(List<ArticleFetcher> fetchers) {
        this.fetchers = fetchers;
    }

    /**
     * 获取支持的所有来源信息
     */
    public List<SourceInfo> getSources() {
        return fetchers.stream()
                .map(f -> new SourceInfo(f.sourceId(), f.sourceName(), f.sourceIcon()))
                .toList();
    }

    /**
     * 从所有来源抓取文章（并行）
     */
    public AggregatedResult fetchAll() {
        List<Article> all = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (ArticleFetcher fetcher : fetchers) {
            try {
                List<Article> articles = fetcher.fetch();
                if (articles != null && !articles.isEmpty()) {
                    all.addAll(articles);
                    log.info("Fetched {} articles from {}", articles.size(), fetcher.sourceId());
                }
            } catch (Exception e) {
                log.error("Failed to fetch from {}: {}", fetcher.sourceId(), e.getMessage());
                errors.add(fetcher.sourceId() + ": " + e.getMessage());
            }
        }
        return new AggregatedResult(all, errors);
    }

    public record SourceInfo(String id, String name, String icon) {}
    public record AggregatedResult(List<Article> articles, List<String> errors) {}
}
