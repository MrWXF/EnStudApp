package com.enstud.common.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DeepSeek AI 配置属性
 * <p>
 * 绑定 {@code application.yml} 中 {@code deepseek} 前缀的配置项。
 * 支持通过环境变量覆盖（如 {@code DEEPSEEK_API_KEY}）。
 *
 * <p><b>配置示例：</b>
 * <pre>{@code
 * deepseek:
 *   api-key: sk-xxxxxxxxxxxxxxxx
 *   base-url: https://api.deepseek.com
 *   model: deepseek-chat
 *   timeout: 60
 * }</pre>
 *
 * @author EnStudApp
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekProperties {

    /**
     * DeepSeek API Key
     * <p>
     * 从 <a href="https://platform.deepseek.com">DeepSeek 开放平台</a> 获取。
     * 生产环境请通过环境变量 {@code DEEPSEEK_API_KEY} 注入，不要写死在配置文件中。
     */
    private String apiKey = "";

    /**
     * DeepSeek API 基础地址
     * <p>
     * 默认值为 {@code https://api.deepseek.com}。
     * 如有代理或私有化部署，可修改为对应地址。
     */
    private String baseUrl = "https://api.deepseek.com";

    /**
     * 使用的模型名称
     * <p>
     * 可选值：
     * <ul>
     *   <li>{@code deepseek-chat} — 通用对话模型（默认）</li>
     *   <li>{@code deepseek-reasoner} — 推理模型，适合复杂逻辑场景</li>
     * </ul>
     */
    private String model = "deepseek-chat";

    /**
     * HTTP 请求超时时间（秒）
     * <p>
     * 同时应用于连接超时和读取超时。
     * AI 生成内容时可能较慢，建议设置不少于 30 秒。
     */
    private int timeout = 60;
}
