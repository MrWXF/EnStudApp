package com.enstud.common.ai;

import com.enstud.common.BusinessException;
import com.enstud.common.constant.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek AI 客户端（OpenAI 兼容格式）
 * <p>
 * 封装 HTTP 调用 DeepSeek Chat Completion API 的能力，
 * 可被 {@code chat-service} 和 {@code writing-service} 共用。
 * <p>
 * 底层使用 {@link HttpURLConnection} 发送 HTTP 请求，
 * 避免引入额外 HTTP 客户端依赖（如 OkHttp、Apache HttpClient）。
 *
 * @author EnStudApp
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DeepSeekClient {

    /** 注入 DeepSeek 配置属性 */
    private final DeepSeekProperties properties;

    /** Jackson JSON 序列化工具 */
    private final ObjectMapper objectMapper;

    /**
     * 单轮对话：直接传入系统提示词和用户消息
     *
     * @param systemPrompt 系统提示词，用于设定 AI 角色和行为规则
     * @param userMessage  用户当前输入的消息内容
     * @return AI 生成的回复文本（非空）
     * @throws BusinessException 当 API 调用失败或返回空内容时抛出，
     *         错误码 {@code 5001}（空内容）或 {@code 5002}（调用异常）
     */
    public String chat(String systemPrompt, String userMessage) {
        List<DeepSeekRequest.ChatMessage> messages = new ArrayList<>();
        messages.add(new DeepSeekRequest.ChatMessage("system", systemPrompt));
        messages.add(new DeepSeekRequest.ChatMessage("user", userMessage));
        return chat(messages);
    }

    /**
     * 多轮对话：传入完整对话历史
     * <p>
     * 消息列表中应包含历史对话（system → user → assistant → ... → user），
     * 此方法会自动附加到 {@link DeepSeekRequest} 中并发送 API 请求。
     *
     * @param messages 按时间顺序排列的完整对话消息列表
     * @return AI 生成的回复文本（非空）
     * @throws BusinessException 当 API 调用失败时抛出
     */
    public String chat(List<DeepSeekRequest.ChatMessage> messages) {
        DeepSeekRequest request = DeepSeekRequest.create(properties.getModel(), messages);

        try {
            // 构建请求 URL 和请求体
            String url = properties.getBaseUrl() + "/v1/chat/completions";
            String body = objectMapper.writeValueAsString(request);

            log.debug("DeepSeek request: {}", body);

            // 发起 HTTP POST 请求
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + properties.getApiKey());
            conn.setConnectTimeout(properties.getTimeout() * 1000);
            conn.setReadTimeout(properties.getTimeout() * 1000);
            conn.setDoOutput(true);

            // 写入请求体
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code == 200) {
                // 解析响应
                String responseStr = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                log.debug("DeepSeek response: {}", responseStr);

                DeepSeekResponse response = objectMapper.readValue(responseStr, DeepSeekResponse.class);
                String content = response.getContent();
                if (content == null || content.isBlank()) {
                    log.warn("DeepSeek returned empty content");
                    throw new BusinessException(ErrorCode.AI_RESPONSE_EMPTY);
                }
                return content;
            } else {
                // API 返回错误
                String errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                log.error("DeepSeek API error: code={}, body={}", code, errorBody);
                throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "AI 服务调用失败: HTTP " + code);
            }
        } catch (BusinessException e) {
            // 业务异常直接向上抛出
            throw e;
        } catch (Exception e) {
            // 其他异常（网络、JSON 解析等）统一包装
            log.error("DeepSeek API call failed", e);
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "AI 服务调用异常: " + e.getMessage());
        }
    }

    /**
     * 便捷方法：根据对话历史构建 {@link DeepSeekRequest.ChatMessage} 列表
     * <p>
     * 典型用法：
     * <pre>{@code
     * List<Map.Entry<String, String>> history = ...;  // 历史对话
     * List<ChatMessage> messages = DeepSeekClient.buildMessages(
     *     "你是一个英语老师", history, "Hello!");
     * String reply = deepSeekClient.chat(messages);
     * }</pre>
     *
     * @param systemPrompt      系统提示词（可为 {@code null}，表示不设置 system 消息）
     * @param conversationHistory 历史对话列表，每个 Entry 的 key 为角色（user/assistant），value 为消息内容
     * @param currentUserMessage 当前用户输入（可为 {@code null}，表示只构建历史）
     * @return 构建好的消息列表，可直接传给 {@link #chat(List)}
     */
    public static List<DeepSeekRequest.ChatMessage> buildMessages(
            String systemPrompt,
            List<Map.Entry<String, String>> conversationHistory,
            String currentUserMessage) {

        List<DeepSeekRequest.ChatMessage> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(new DeepSeekRequest.ChatMessage("system", systemPrompt));
        }
        if (conversationHistory != null) {
            for (Map.Entry<String, String> entry : conversationHistory) {
                messages.add(new DeepSeekRequest.ChatMessage(entry.getKey(), entry.getValue()));
            }
        }
        if (currentUserMessage != null) {
            messages.add(new DeepSeekRequest.ChatMessage("user", currentUserMessage));
        }
        return messages;
    }
}
