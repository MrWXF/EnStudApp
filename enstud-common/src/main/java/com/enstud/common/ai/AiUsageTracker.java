package com.enstud.common.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * AI 用量追踪器 — 基于 Redis 的每日配额管理
 * <p>
 * 每个用户每个功能每天有一个调用次数上限。
 * 配额值通过 {@link DeepSeekProperties} 配置，支持运行时从 Nacos 热更新。
 * <p>
 * Redis key 格式：{@code ai:usage:{userId}:{feature}:{yyyyMMdd}}
 *
 * @author EnStudApp
 * @since 1.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class AiUsageTracker {

    private static final String KEY_PREFIX = "ai:usage:";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.BASIC_ISO_DATE;

    private final RedissonClient redissonClient;
    private final DeepSeekProperties properties;

    /**
     * 检查用户是否还有剩余额度
     *
     * @param userId  用户ID
     * @param feature 功能类型（chat / writing / translate）
     * @return true 表示可以继续使用
     */
    public boolean checkQuota(Long userId, String feature) {
        if (userId == null) {
            return true; // 未登录用户不做限制（由 Gateway 层处理）
        }
        int limit = getLimit(feature);
        if (limit <= 0) {
            return true; // limit <= 0 表示不限制
        }
        long used = getUsedToday(userId, feature);
        boolean ok = used < limit;
        if (!ok) {
            log.warn("AI quota exceeded: userId={}, feature={}, used={}, limit={}", userId, feature, used, limit);
        }
        return ok;
    }

    /**
     * 记录一次 AI 调用
     *
     * @param userId  用户ID
     * @param feature 功能类型
     */
    public void recordUsage(Long userId, String feature) {
        if (userId == null) {
            return;
        }
        String key = buildKey(userId, feature);
        RAtomicLong counter = redissonClient.getAtomicLong(key);
        long count = counter.incrementAndGet();
        // 首次写入时设置过期时间（25小时，留一些余量避免跨天边界问题）
        if (count == 1) {
            counter.expire(Duration.ofHours(25));
        }
        log.debug("AI usage recorded: userId={}, feature={}, count={}", userId, feature, count);
    }

    /**
     * 查询今日已用次数
     */
    public long getUsedToday(Long userId, String feature) {
        String key = buildKey(userId, feature);
        RAtomicLong counter = redissonClient.getAtomicLong(key);
        return counter.get();
    }

    /**
     * 获取指定功能的每日限额
     */
    public int getLimit(String feature) {
        return switch (feature) {
            case "chat" -> properties.getChatDailyLimit();
            case "writing" -> properties.getWritingDailyLimit();
            case "translate" -> properties.getTranslateDailyLimit();
            default -> 0; // 未配置的功能不限制
        };
    }

    private String buildKey(Long userId, String feature) {
        String date = LocalDate.now().format(DATE_FMT);
        return KEY_PREFIX + userId + ":" + feature + ":" + date;
    }
}
