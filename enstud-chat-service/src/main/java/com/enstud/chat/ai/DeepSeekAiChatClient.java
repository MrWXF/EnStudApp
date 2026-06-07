package com.enstud.chat.ai;

import com.enstud.common.BusinessException;
import com.enstud.common.ai.DeepSeekClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基于 DeepSeek 的真实 AI 对话客户端
 * <p>
 * 通过 DeepSeek Chat Completion API 实现英语对话和语法检测。
 * 只有在 {@code ai.mock=false} 或未配置 {@code ai.mock} 时生效；
 * 若 {@code ai.mock=true} 则 {@link MockAiChatClient} 会优先生效。
 *
 * <p><b>系统设计：</b>本类与 {@link MockAiChatClient} 实现同一接口 {@link AiChatClient}，
 * 通过 {@code @ConditionalOnProperty} 实现运行时切换，无需修改业务代码。
 *
 * @author EnStudApp
 * @since 1.0.0
 * @see AiChatClient
 * @see MockAiChatClient
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.mock", havingValue = "false", matchIfMissing = true)
public class DeepSeekAiChatClient implements AiChatClient {

    /** 注入 DeepSeek 客户端（common 模块提供） */
    private final DeepSeekClient deepSeekClient;

    /** Jackson JSON 序列化工具 */
    private final ObjectMapper objectMapper;

    /**
     * 处理用户对话请求，调用 DeepSeek API 获取 AI 回复
     * <p>
     * 处理流程：
     * <ol>
     *   <li>根据场景类型构建系统提示词 {@link #buildSystemPrompt(String)}</li>
     *   <li>将数据库中的历史对话转换为 {@code DeepSeekRequest.ChatMessage} 列表</li>
     *   <li>调用 {@link DeepSeekClient#chat(List)} 获取 AI 回复</li>
     *   <li>从回复中提取语法问题（如 AI 按约定格式返回）</li>
     *   <li>去除回复中的语法标记，返回纯对话文本</li>
     * </ol>
     *
     * @param request 对话请求，包含场景类型、历史消息和当前用户输入
     * @return AI 响应，包含回复文本和检测到的语法问题列表
     * @throws BusinessException 当 AI 服务调用失败或返回空内容时抛出，
     *         错误码 {@code 5002}（服务异常）
     */
    @Override
    public AiResponse chat(AiRequest request) {
        log.info("DeepSeek AI chat: scenario={}, message={}", request.getScenario(), request.getCurrentMessage());

        // 根据场景构建系统提示词，引导 AI 行为
        String systemPrompt = buildSystemPrompt(request.getScenario());

        // 将数据库实体转换为 AI 请求格式（role + content）
        List<Map.Entry<String, String>> history = new ArrayList<>();
        if (request.getHistory() != null) {
            for (AiRequest.Message msg : request.getHistory()) {
                // 将前端 role 映射为 AI 需要的 user/assistant
                String role = "user".equals(msg.getRole()) ? "user" : "assistant";
                history.add(Map.entry(role, msg.getContent()));
            }
        }

        try {
            // 构建完整消息列表并调用 DeepSeek API
            List<com.enstud.common.ai.DeepSeekRequest.ChatMessage> messages =
                    DeepSeekClient.buildMessages(systemPrompt, history, request.getCurrentMessage());

            String aiReply = deepSeekClient.chat(messages);

            // AI 可能按约定在回复末尾附加 [GRAMMAR_ISSUES]...[/GRAMMAR_ISSUES] 标记
            List<AiResponse.GrammarIssue> issues = extractGrammarIssues(aiReply);

            // 去除标记部分，只保留纯对话文本返回给前端
            String cleanReply = extractCleanReply(aiReply);

            return new AiResponse(cleanReply, issues);
        } catch (BusinessException e) {
            // 业务异常直接向上抛出，由全局异常处理器统一返回
            throw e;
        } catch (Exception e) {
            // 其他异常（网络、JSON 解析等）包装为业务异常
            log.error("DeepSeek chat error", e);
            throw new BusinessException(5002, "AI 对话服务异常");
        }
    }

    /**
     * 根据场景类型构建系统提示词
     * <p>
     * 系统提示词决定了 AI 的回复风格和关注点。
     * 基础提示词要求 AI 作为英语学习伙伴，
     * 并根据不同场景（商务 / 旅行 / 日常）叠加特定指令。
     *
     * @param scenario 场景类型，可选值：{@code FREE} / {@code BUSINESS} / {@code TRAVEL} / {@code DAILY}
     * @return 完整的系统提示词字符串
     */
    private String buildSystemPrompt(String scenario) {
        // 基础提示词：定义 AI 角色为面向中国学习者的英语对话伙伴
        String basePrompt = """
                You are an English conversation partner for Chinese learners. Your goals:
                1. Respond naturally and conversationally in English
                2. Use vocabulary and grammar appropriate for intermediate learners (B1-B2 level)
                3. Keep responses concise but meaningful (3-5 sentences typically)
                4. Ask follow-up questions to keep the conversation going
                5. Occasionally explain useful expressions or grammar points

                IMPORTANT: When you detect grammar or vocabulary errors in the user's message, \
                include a grammar correction section at the END of your response in this exact JSON format:

                [GRAMMAR_ISSUES]
                [{"error":"description of error","correction":"corrected version","explanation":"简体中文解释"}]
                [/GRAMMAR_ISSUES]

                Only include grammar issues if you actually find errors. For minor or debatable issues, skip them.
                """;

        // 根据场景叠加特定指令
        return switch (scenario != null ? scenario : "FREE") {
            case "BUSINESS" -> basePrompt + """

                    The conversation topic is BUSINESS. Role-play as a business colleague. \
                    Discuss meetings, emails, negotiations, office culture, etc. \
                    Use formal business English when appropriate.
                    """;
            case "TRAVEL" -> basePrompt + """

                    The conversation topic is TRAVEL. Discuss travel experiences, \
                    booking hotels, asking for directions, sightseeing, local cuisine, etc. \
                    Introduce useful travel-related expressions.
                    """;
            case "DAILY" -> basePrompt + """

                    The conversation topic is DAILY LIFE. Discuss daily routines, \
                    hobbies, food, weather, movies, music, sports, etc. \
                    Keep the tone casual and friendly.
                    """;
            default -> basePrompt;
        };
    }

    /**
     * 从 AI 回复文本中提取语法问题列表
     * <p>
     * 如果 AI 按 {@link #buildSystemPrompt(String)} 中的约定格式返回了
     * {@code [GRAMMAR_ISSUES]...[/GRAMMAR_ISSUES]} 标记，
     * 则解析其中的 JSON 数组并返回；否则返回 {@code null}。
     *
     * @param reply AI 的完整回复文本
     * @return 解析出的语法问题列表；若格式不正确或无标记则返回 {@code null}
     */
    private List<AiResponse.GrammarIssue> extractGrammarIssues(String reply) {
        int start = reply.indexOf("[GRAMMAR_ISSUES]");
        int end = reply.indexOf("[/GRAMMAR_ISSUES]");
        if (start < 0 || end < 0) {
            return null;
        }
        // 提取标记内的 JSON 字符串
        String json = reply.substring(start + "[GRAMMAR_ISSUES]".length(), end).trim();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse grammar issues from AI response: {}", json);
            return null;
        }
    }

    /**
     * 去除 AI 回复中的语法标记，返回纯对话文本
     * <p>
     * 若回复中包含 {@code [GRAMMAR_ISSUES]} 标记，
     * 则只保留标记之前的内容（即纯对话文本）。
     *
     * @param reply AI 的完整回复文本
     * @return 去除标记后的纯文本；若无标记则返回原文本（已 trim）
     */
    private String extractCleanReply(String reply) {
        int start = reply.indexOf("[GRAMMAR_ISSUES]");
        if (start > 0) {
            return reply.substring(0, start).trim();
        }
        return reply.trim();
    }
}
