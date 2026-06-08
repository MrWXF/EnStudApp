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
 * GitHub Trending 热门仓库抓取器
 * 通过 GitHub 搜索 API 获取每日热门仓库（根据 Stars 增长）
 */
@Component
public class GitHubTrendingFetcher implements ArticleFetcher {

    private static final Logger log = LoggerFactory.getLogger(GitHubTrendingFetcher.class);
    // 用 GitHub 搜索 API 获取近一周 Stars 增长最多的仓库
    private static final String SEARCH_API = "https://api.github.com/search/repositories?q=created:>%s&sort=stars&order=desc&per_page=25";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GitHubTrendingFetcher() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String sourceId() { return "GitHub"; }

    @Override
    public String sourceName() { return "GitHub 热门"; }

    @Override
    public String sourceIcon() {
        return "https://github.githubassets.com/favicons/favicon.svg";
    }

    @Override
    public List<Article> fetch() {
        List<Article> articles = new ArrayList<>();
        try {
            // 查询近 7 天创建的热门仓库
            String date = LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String url = String.format(SEARCH_API, date);

            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Accept", "application/vnd.github.v3+json")
                            .timeout(Duration.ofSeconds(15))
                            .GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode items = root.get("items");
            if (items == null || !items.isArray()) return articles;

            for (JsonNode item : items) {
                Article article = new Article();
                String fullName = item.get("full_name").asText();
                String description = item.has("description") && !item.get("description").isNull()
                        ? item.get("description").asText() : "";
                String lang = item.has("language") && !item.get("language").isNull()
                        ? item.get("language").asText() : "";

                article.setTitle(fullName + (lang.isEmpty() ? "" : " (" + lang + ")"));
                article.setTitleCn(fullName); // 仓库名不翻译
                article.setUrl(item.get("html_url").asText());
                article.setSource(sourceId());
                article.setSourceIcon(sourceIcon());
                article.setAuthor(item.get("owner").get("login").asText());

                // 用描述作为摘要
                article.setSummary(description);
                if (description.length() > 300) {
                    article.setSummary(description.substring(0, 300) + "...");
                }

                article.setContent(fullName + " - " + description);

                if (item.has("stargazers_count")) {
                    article.setSourceScore(item.get("stargazers_count").asInt());
                }

                if (item.has("created_at")) {
                    article.setPublishedAt(LocalDateTime.parse(
                            item.get("created_at").asText().substring(0, 19),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                    ));
                }

                if (item.has("owner") && item.get("owner").has("avatar_url")) {
                    article.setCoverUrl(item.get("owner").get("avatar_url").asText());
                }

                article.setFetchedAt(LocalDateTime.now());
                articles.add(article);
            }

            log.info("GitHub trending fetcher: fetched {} repos", articles.size());
        } catch (Exception e) {
            log.error("GitHub trending fetch failed", e);
        }
        return articles;
    }
}
