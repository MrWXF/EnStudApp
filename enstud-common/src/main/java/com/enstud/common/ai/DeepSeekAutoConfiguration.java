package com.enstud.common.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
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
     * <p>
     * 原始 {@code @Component} 注解在非同包微服务中无法被扫描到，
     * 此处通过 {@code @Bean} 显式注册，确保跨服务可用。
     *
     * @param properties  DeepSeek 配置属性（由 {@code @EnableConfigurationProperties} 自动注册）
     * @param objectMapper Jackson JSON 序列化工具（Spring Boot 自动提供）
     * @return DeepSeek 客户端实例
     */
    @Bean
    public DeepSeekClient deepSeekClient(DeepSeekProperties properties, ObjectMapper objectMapper) {
        return new DeepSeekClient(properties, objectMapper);
    }
}
