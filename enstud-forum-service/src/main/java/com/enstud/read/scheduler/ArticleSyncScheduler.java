package com.enstud.read.scheduler;

import com.enstud.read.service.ArticleWordService;
import com.enstud.read.service.ReadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 文章定时同步任务
 * 每 4 小时自动从各来源抓取最新热门文章
 * 同步后自动提取文章中的单词并建立关联（学习闭环）
 */
@Component
public class ArticleSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(ArticleSyncScheduler.class);
    private final ReadService readService;
    private final ArticleWordService articleWordService;

    public ArticleSyncScheduler(ReadService readService, ArticleWordService articleWordService) {
        this.readService = readService;
        this.articleWordService = articleWordService;
    }

    /**
     * 每 4 小时同步一次文章
     */
    @Scheduled(fixedRate = 4 * 60 * 60 * 1000L)
    public void syncArticles() {
        log.info("[Scheduled] Starting article sync...");
        try {
            int count = readService.syncArticles();
            log.info("[Scheduled] Sync completed: {} new articles", count);
        } catch (Exception e) {
            log.error("[Scheduled] Sync failed", e);
        }
    }

    /**
     * 每 6 小时处理一次文章词汇提取（学习闭环）
     */
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000L, initialDelay = 5 * 60 * 1000L)
    public void extractArticleWords() {
        log.info("[Scheduled] Starting article word extraction...");
        try {
            articleWordService.processPendingArticles();
            log.info("[Scheduled] Article word extraction completed");
        } catch (Exception e) {
            log.error("[Scheduled] Article word extraction failed", e);
        }
    }
}
