package com.enstud.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 全局限流过滤器
 * <p>
 * 基于 Redis 实现两级限流：
 * <ol>
 *   <li>IP 维度：每 IP 每分钟最多 100 次请求</li>
 *   <li>用户维度：从 JWT 中提取 userId，每用户每分钟最多 60 次请求</li>
 * </ol>
 * <p>
 * 超限时返回 429 Too Many Requests + 标准 Result 格式。
 * <p>
 * 注意：此过滤器运行在 WebFlux 环境中，使用 Reactive Redis。
 * AI 每日配额检查在 {@code AiUsageTracker}（common 模块）中，由业务服务调用。
 *
 * @author EnStudApp
 * @since 1.1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final int IP_LIMIT_PER_MINUTE = 100;
    private static final int USER_LIMIT_PER_MINUTE = 60;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.BASIC_ISO_DATE;

    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String clientIp = getClientIp(request);
        String userId = extractUserId(request);

        // IP 维度限流
        String ipKey = "rate:ip:" + clientIp + ":" + LocalDate.now().format(DATE_FMT) + ":"
                + java.time.LocalDateTime.now().getMinute();
        return checkLimit(ipKey, IP_LIMIT_PER_MINUTE, exchange, chain, "IP")
                .flatMap(ipAllowed -> {
                    if (!ipAllowed) {
                        return reject(exchange, "请求过于频繁（IP），请稍后再试");
                    }
                    // 用户维度限流（仅对已登录用户）
                    if (userId != null) {
                        String userKey = "rate:user:" + userId + ":" + LocalDate.now().format(DATE_FMT) + ":"
                                + java.time.LocalDateTime.now().getMinute();
                        return checkLimit(userKey, USER_LIMIT_PER_MINUTE, exchange, chain, "USER")
                                .flatMap(userAllowed -> {
                                    if (!userAllowed) {
                                        return reject(exchange, "请求过于频繁，请稍后再试");
                                    }
                                    return chain.filter(exchange);
                                });
                    }
                    return chain.filter(exchange);
                });
    }

    private Mono<Boolean> checkLimit(String key, int limit, ServerWebExchange exchange,
                                      GatewayFilterChain chain, String type) {
        return redisTemplate.opsForValue()
                .increment(key)
                .flatMap(count -> {
                    // 首次设置过期时间
                    if (count != null && count == 1L) {
                        return redisTemplate.expire(key, Duration.ofMinutes(2))
                                .thenReturn(count <= limit);
                    }
                    return Mono.justOrEmpty(count).map(c -> c <= limit);
                })
                .defaultIfEmpty(true);
    }

    private Mono<Void> reject(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"code\":5004,\"msg\":\"" + message + "\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        log.warn("Rate limit exceeded: {}", message);
        return response.writeWith(Mono.just(buffer));
    }

    private String getClientIp(ServerHttpRequest request) {
        String xff = request.getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    /**
     * 从请求头中提取 userId（由 JwtAuthFilter 注入）
     */
    private String extractUserId(ServerHttpRequest request) {
        return request.getHeaders().getFirst("X-User-Id");
    }

    @Override
    public int getOrder() {
        // 在 JwtAuthFilter 之后执行（JwtAuthFilter order=-100）
        return -90;
    }
}
