package com.enstud.read.ai;

import com.enstud.read.entity.Article;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Hacker News 热门文章抓取器
 * 通过 HN API 获取 Top Stories / Best Stories
 */
@Component
public class HackerNewsFetcher implements ArticleFetcher {

    private static final Logger log = LoggerFactory.getLogger(HackerNewsFetcher.class);
    private static final String TOP_STORIES_API = "https://hacker-news.firebaseio.com/v0/topstories.json";
    private static final String ITEM_API = "https://hacker-news.firebaseio.com/v0/item/%d.json";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HackerNewsFetcher() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String sourceId() { return "HN"; }

    @Override
    public String sourceName() { return "Hacker News"; }

    @Override
    public String sourceIcon() {
        return "https://news.ycombinator.com/favicon.ico";
    }

    @Override
    public List<Article> fetch() {
        List<Article> articles = new ArrayList<>();
        try {
            // 1. 获取 Top Stories ID 列表
            String idsJson = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(TOP_STORIES_API))
                            .timeout(Duration.ofSeconds(15))
                            .GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            ).body();

            int[] ids = objectMapper.readValue(idsJson, int[].class);

            // 取前 30 条
            int limit = Math.min(ids.length, 30);
            for (int i = 0; i < limit; i++) {
                Article article = fetchItem(ids[i]);
                if (article != null) {
                    articles.add(article);
                }
                // 避免 API 限流
                if (i % 10 == 9) Thread.sleep(500);
            }

            log.info("HackerNews fetcher: fetched {} articles", articles.size());
        } catch (Exception e) {
            log.error("HackerNews fetch failed", e);
        }
        return articles;
    }

    private Article fetchItem(int id) {
        try {
            String json = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(String.format(ITEM_API, id)))
                            .timeout(Duration.ofSeconds(10))
                            .GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            ).body();

            JsonNode node = objectMapper.readTree(json);
            if (node == null || node.isNull()) return null;
            if (!node.has("title")) return null;

            Article article = new Article();
            article.setTitle(node.get("title").asText());
            article.setUrl(node.has("url") ? node.get("url").asText() :
                    "https://news.ycombinator.com/item?id=" + id);
            article.setSource(sourceId());
            article.setSourceIcon(sourceIcon());

            if (node.has("score")) {
                article.setSourceScore(node.get("score").asInt());
            }

            if (node.has("by")) {
                article.setAuthor(node.get("by").asText());
            }

            if (node.has("time")) {
                long unix = node.get("time").asLong();
                article.setPublishedAt(
                        LocalDateTime.ofEpochSecond(unix, 0, ZoneId.of("UTC").getRules().getOffset(java.time.Instant.now()))
                );
            }

            // 取摘要：从 desc/text 字段或第一段内容
            String text = node.has("text") ? node.get("text").asText() : "";
            if (!text.isEmpty()) {
                // 去除 HTML 标签
                text = text.replaceAll("<[^>]+>", "");
                if (text.length() > 300) text = text.substring(0, 300) + "...";
                article.setSummary(text);
            }

            // 对于 HN 的文章，没有文章内容本身，只保存标题/链接
            article.setContent(article.getTitle());
            article.setFetchedAt(LocalDateTime.now());

            return article;
        } catch (Exception e) {
            log.debug("Failed to fetch HN item {}", id, e);
            return null;
        }
    }
}
