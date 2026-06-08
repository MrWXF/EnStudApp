package com.enstud.read.scheduler;

import com.enstud.read.service.ReadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 文章定时同步任务
 * 每 4 小时自动从各来源抓取最新热门文章
 */
@Component
public class ArticleSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(ArticleSyncScheduler.class);
    private final ReadService readService;

    public ArticleSyncScheduler(ReadService readService) {
        this.readService = readService;
    }

    /**
     * 每 4 小时同步一次
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
}
