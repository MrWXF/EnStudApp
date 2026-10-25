package com.enstud.user.client;

import com.enstud.common.Result;
import com.enstud.common.entity.UserProfile;
import com.enstud.user.dto.UserStatsDTO;
import com.enstud.user.mapper.UserProfileMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 跨服务统计聚合客户端：调用各微服务内部接口获取用户学习数据
 */
@Slf4j
@Component
public class StatsClient {

    private final RestTemplate restTemplate;
    private final UserProfileMapper profileMapper;

    @Value("${service.word:http://localhost:8082}")
    private String wordServiceUrl;

    @Value("${service.read:http://localhost:8087}")
    private String readServiceUrl;

    @Value("${service.writing:http://localhost:8084}")
    private String writingServiceUrl;

    @Value("${service.chat:http://localhost:8083}")
    private String chatServiceUrl;

    @Value("${service.forum:http://localhost:8086}")
    private String forumServiceUrl;

    public StatsClient(RestTemplate restTemplate, UserProfileMapper profileMapper) {
        this.restTemplate = restTemplate;
        this.profileMapper = profileMapper;
    }

    /**
     * 聚合用户所有学习统计数据
     */
    public Result<UserStatsDTO> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        // 单词学习统计
        fetchWordStats(userId, stats);

        // 阅读统计
        fetchReadStats(userId, stats);

        // 写作统计
        fetchWritingStats(userId, stats);

        // 对话统计
        fetchChatStats(userId, stats);

        // 论坛统计
        fetchForumStats(userId, stats);

        return Result.success(toUserStatsDTO(userId, stats));
    }

    /**
     * 将各服务内部接口的原始字段组装为前端契约 DTO（UserStatsDTO）
     */
    @SuppressWarnings("unchecked")
    private UserStatsDTO toUserStatsDTO(Long userId, Map<String, Object> stats) {
        Map<String, Object> dist = stats.get("memoryDistribution") instanceof Map<?, ?> map
                ? (Map<String, Object>) map : Map.of();

        UserProfile profile = profileMapper.selectById(userId);
        long streakDays = profile != null && profile.getCurrentStreak() != null
                ? profile.getCurrentStreak() : 0L;

        return new UserStatsDTO(
                toLong(stats.get("todayLearned")),
                toLong(stats.get("totalLearned")),
                toLong(stats.get("totalReadArticles")),
                toLong(stats.get("totalWritings")),
                toDouble(stats.get("avgScore")),
                toLong(stats.get("totalChats")),
                toLong(stats.get("totalPosts")),
                streakDays,
                new UserStatsDTO.MemoryLevelDistribution(
                        toLong(dist.get("notLearned")),
                        toLong(dist.get("fuzzy")),
                        toLong(dist.get("familiar")),
                        toLong(dist.get("basic")),
                        toLong(dist.get("proficient")),
                        toLong(dist.get("mastered"))
                )
        );
    }

    private static long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private static double toDouble(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0.0;
    }

    /**
     * 获取今日学习之旅任务数据（今日学词/到期复习/今日阅读/今日对话）
     */
    public Map<String, Object> getJourneyStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        fetchWordStats(userId, stats);
        fetchReadStats(userId, stats);
        fetchChatStats(userId, stats);
        return stats;
    }

    private void fetchWordStats(Long userId, Map<String, Object> stats) {
        try {
            String url = wordServiceUrl + "/word/internal/stats/" + userId + "?today=" + LocalDate.now();
            var typeRef = new ParameterizedTypeReference<Result<Map<String, Object>>>() {};
            var resp = restTemplate.exchange(url, HttpMethod.GET, null, typeRef);
            if (resp.getBody() != null && resp.getBody().getData() != null) {
                stats.putAll(resp.getBody().getData());
            }
        } catch (Exception e) {
            log.warn("获取单词统计失败: userId={}", userId, e);
            stats.put("todayLearned", 0L);
            stats.put("totalLearned", 0L);
        }
    }

    private void fetchReadStats(Long userId, Map<String, Object> stats) {
        try {
            String url = readServiceUrl + "/read/internal/stats/" + userId + "?today=" + LocalDate.now();
            var typeRef = new ParameterizedTypeReference<Result<Map<String, Object>>>() {};
            var resp = restTemplate.exchange(url, HttpMethod.GET, null, typeRef);
            if (resp.getBody() != null && resp.getBody().getData() != null) {
                stats.putAll(resp.getBody().getData());
            }
        } catch (Exception e) {
            log.warn("获取阅读统计失败: userId={}", userId, e);
            stats.put("totalReadArticles", 0L);
            stats.put("todayReadArticles", 0L);
        }
    }

    private void fetchWritingStats(Long userId, Map<String, Object> stats) {
        try {
            String url = writingServiceUrl + "/writing/internal/stats/" + userId;
            var typeRef = new ParameterizedTypeReference<Result<Map<String, Object>>>() {};
            var resp = restTemplate.exchange(url, HttpMethod.GET, null, typeRef);
            if (resp.getBody() != null && resp.getBody().getData() != null) {
                stats.putAll(resp.getBody().getData());
            }
        } catch (Exception e) {
            log.warn("获取写作统计失败: userId={}", userId, e);
            stats.put("totalWritings", 0L);
            stats.put("avgScore", 0.0);
        }
    }

    private void fetchChatStats(Long userId, Map<String, Object> stats) {
        try {
            String url = chatServiceUrl + "/chat/internal/stats/" + userId + "?today=" + LocalDate.now();
            var typeRef = new ParameterizedTypeReference<Result<Map<String, Object>>>() {};
            var resp = restTemplate.exchange(url, HttpMethod.GET, null, typeRef);
            if (resp.getBody() != null && resp.getBody().getData() != null) {
                stats.putAll(resp.getBody().getData());
            }
        } catch (Exception e) {
            log.warn("获取对话统计失败: userId={}", userId, e);
            stats.put("totalChats", 0L);
        }
    }

    private void fetchForumStats(Long userId, Map<String, Object> stats) {
        try {
            String url = forumServiceUrl + "/forum/internal/stats/" + userId;
            var typeRef = new ParameterizedTypeReference<Result<Map<String, Object>>>() {};
            var resp = restTemplate.exchange(url, HttpMethod.GET, null, typeRef);
            if (resp.getBody() != null && resp.getBody().getData() != null) {
                stats.putAll(resp.getBody().getData());
            }
        } catch (Exception e) {
            log.warn("获取论坛统计失败: userId={}", userId, e);
            stats.put("totalPosts", 0L);
        }
    }
}
