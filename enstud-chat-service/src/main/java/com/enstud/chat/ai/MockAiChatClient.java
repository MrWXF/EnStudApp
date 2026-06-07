package com.enstud.chat.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟 AI 对话客户端（用于开发测试）
 * <p>
 * 当 {@code application.yml} 中配置 {@code ai.mock=true} 时生效；
 * 若未配置或 {@code ai.mock=false}，则 {@link DeepSeekAiChatClient} 优先生效。
 * <p>
 * 模拟回复基于关键词匹配，覆盖常见英语对话场景，
 * 仅用于前后端联调，<b>不可用于生产环境</b>。
 *
 * @author EnStudApp
 * @since 1.0.0
 * @see DeepSeekAiChatClient
 * @see AiChatClient
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "ai.mock", havingValue = "true")
public class MockAiChatClient implements AiChatClient {

    /**
     * 处理对话请求，返回模拟回复
     * <p>
     * 根据用户输入的关键词匹配预设回复模板；
     * 同时调用 {@link #detectSimpleErrors(String)} 模拟语法错误检测。
     *
     * @param request 对话请求，包含场景类型和用户输入
     * @return AI 响应，包含模拟回复文本和语法问题列表
     */
    @Override
    public AiResponse chat(AiRequest request) {
        log.info("Mock AI chat: scenario={}, message={}", request.getScenario(), request.getCurrentMessage());

        String reply = generateReply(request);
        List<AiResponse.GrammarIssue> issues = detectSimpleErrors(request.getCurrentMessage());

        return new AiResponse(reply, issues);
    }

    /**
     * 根据关键词匹配生成模拟回复
     * <p>
     * 匹配优先级（从上到下）：
     * <ol>
     *   <li>{@code hello / hi / 你好} → 问候回复</li>
     *   <li>{@code how are you / 你好吗} → 状态询问回复</li>
     *   <li>{@code weather / 天气} → 天气话题回复</li>
     *   <li>{@code hobby / like / 爱好} → 爱好话题回复</li>
     *   <li>{@code food / eat / 美食} → 美食话题回复</li>
     *   <li>场景为 {@code BUSINESS} → 商务场景回复</li>
     *   <li>场景为 {@code TRAVEL} → 旅行场景回复</li>
     *   <li>其他 → 通用兜底回复</li>
     * </ol>
     *
     * @param request 对话请求
     * @return 匹配到的模拟回复；若无匹配则返回通用回复
     */
    private String generateReply(AiRequest request) {
        String input = request.getCurrentMessage().toLowerCase();
        String scenario = request.getScenario();

        // 问候类关键词
        if (input.contains("hello") || input.contains("hi") || input.contains("你好")) {
            return "Hello! Nice to meet you. How are you doing today? Tell me about your day!";
        }
        // 状态询问
        if (input.contains("how are you") || input.contains("你好吗")) {
            return "I am great, thank you for asking! How about you? What have you been up to recently?";
        }
        // 天气话题
        if (input.contains("weather") || input.contains("天气")) {
            return "The weather is beautiful today! It's a great day for outdoor activities. Do you like sunny days? By the way, we could say 'It is sunny today' or 'What a lovely day!'";
        }
        // 爱好话题
        if (input.contains("hobby") || input.contains("like") || input.contains("爱好")) {
            return "That's interesting! I enjoy reading and learning new things. What kinds of books or movies do you enjoy?";
        }
        // 美食话题
        if (input.contains("food") || input.contains("eat") || input.contains("美食")) {
            return "Oh, I love talking about food! Have you tried any new dishes recently? My favorite cuisine is Italian - pasta and pizza are classics. How about you?";
        }

        // 场景化回复
        if ("BUSINESS".equals(scenario)) {
            return "Thank you for your message. In a business context, I would suggest we schedule a meeting to discuss this further. Shall we talk about your project requirements in more detail?";
        }
        if ("TRAVEL".equals(scenario)) {
            return "That sounds exciting! Traveling is a wonderful way to experience different cultures. Have you been to many countries? I can help you practice travel-related English phrases!";
        }

        // 兜底通用回复
        return "That's a great point! Let me ask you a question to keep our conversation going: What do you think about this topic? Feel free to express your thoughts in English - I am here to help!";
    }

    /**
     * 简单语法错误检测（模拟实现）
     * <p>
     * 仅检测最常见的几类错误，用于演示语法检测功能。
     * 生产环境应使用 {@link DeepSeekAiChatClient}，由 AI 自动检测。
     *
     * @param text 用户输入的文本
     * @return 检测到的语法问题列表；若无则返回空列表
     */
    private List<AiResponse.GrammarIssue> detectSimpleErrors(String text) {
        List<AiResponse.GrammarIssue> issues = new ArrayList<>();

        // 检测：第一人称代词未大写
        if (text.contains("i am")) {
            issues.add(new AiResponse.GrammarIssue(
                    "\"i am\" should be capitalized",
                    "I am",
                    "在英语中，第一人称代词 'I' 无论出现在句子什么位置都必须大写。"
            ));
        }
        // 检测：第三人称单数动词未加 -s
        if (text.contains("he go") || text.contains("she go")) {
            issues.add(new AiResponse.GrammarIssue(
                    "Subject-verb agreement error with \"go\"",
                    "he/she goes",
                    "第三人称单数主语后动词需要加 -s 或 -es。"
            ));
        }
        // 检测：过去时间状语搭配现在时动词
        if (text.contains("yesterday") && (text.contains("go") && !text.contains("went"))) {
            issues.add(new AiResponse.GrammarIssue(
                    "Tense inconsistency with 'yesterday'",
                    "Use past tense verb",
                    "当句子中有 'yesterday' 时，应使用过去时态。如 go→went, eat→ate。"
            ));
        }

        return issues;
    }
}
