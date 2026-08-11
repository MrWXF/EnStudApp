package com.enstud.common.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek AI 客户端自动配置
 * <p>
 * 将 {@link DeepSeekClient} 及其依赖的 {@link DeepSeekProperties} 注册为 Spring Bean，
 * 确保所有引用 common 模块的微服务（如 chat-service、writing-service）
 * 无需手动声明即可通过 {@code @Autowired} 注入。
 * <p>
 * 本配置类通过 {@code AutoConfiguration.imports} 机制自动加载，
 * 不依赖 {@code @ComponentScan}，因此各微服务无需显式扫描 {@code com.enstud.common.ai} 包。
 *
 * @author EnStudApp
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(DeepSeekProperties.class)
public class DeepSeekAutoConfiguration {

    /**
     * 注册 DeepSeek 客户端 Bean
     */
    @Bean
    public DeepSeekClient deepSeekClient(DeepSeekProperties properties, ObjectMapper objectMapper,
                                          org.springframework.beans.factory.ObjectProvider<AiUsageTracker> trackerProvider) {
        return new DeepSeekClient(properties, objectMapper, trackerProvider.getIfAvailable());
    }

    /**
     * 注册 AI 用量追踪器 Bean
     * <p>
     * 仅在 RedissonClient 存在时生效（即服务配置了 Redis）。
     */
    @Bean
    @ConditionalOnBean(RedissonClient.class)
    public AiUsageTracker aiUsageTracker(RedissonClient redissonClient, DeepSeekProperties properties) {
        return new AiUsageTracker(redissonClient, properties);
    }
}
