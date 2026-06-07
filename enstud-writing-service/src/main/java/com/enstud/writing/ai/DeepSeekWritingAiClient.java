package com.enstud.writing.ai;

import com.enstud.common.BusinessException;
import com.enstud.common.ai.DeepSeekClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于 DeepSeek 的写作批改客户端
 * <p>
 * 通过 DeepSeek Chat Completion API 实现英语作文智能批改，
 * 返回评分、总评和详细的批改意见列表。
 * <p>
 * 只有在 {@code ai.mock=false} 或未配置 {@code ai.mock} 时生效；
 * 若 {@code ai.mock=true} 则 {@link MockWritingAiClient} 会优先生效。
 *
 * <p><b>系统设计：</b>本类与 {@link MockWritingAiClient} 实现同一接口 {@link WritingAiClient}，
 * 通过 {@code @ConditionalOnProperty} 实现运行时切换，无需修改业务代码。
 *
 * @author EnStudApp
 * @since 1.0.0
 * @see WritingAiClient
 * @see MockWritingAiClient
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.mock", havingValue = "false", matchIfMissing = true)
public class DeepSeekWritingAiClient implements WritingAiClient {

    /** 注入 DeepSeek 客户端（common 模块提供） */
    private final DeepSeekClient deepSeekClient;

    /** Jackson JSON 序列化工具 */
    private final ObjectMapper objectMapper;

    /**
     * 批改英语作文
     * <p>
     * 处理流程：
     * <ol>
     *   <li>构建系统提示词（设定 AI 为英语写作导师角色）</li>
     *   <li>构建用户提示词（包含作文标题、类型、内容）</li>
     *   <li>调用 {@link DeepSeekClient#chat(String, String)} 获取 AI 批改结果</li>
     *   <li>清理 AI 回复中可能存在的 markdown 代码块标记</li>
     *   <li>将 JSON 字符串反序列化为 {@link WritingCorrection}</li>
     *   <li>校验并补全返回结果的必填字段</li>
     * </ol>
     *
     * @param request 批改请求，包含作文标题、内容、话题类型
     * @return 批改结果，包含评分、总评和批改意见列表
     * @throws BusinessException 当 AI 服务调用失败或返回内容无法解析时抛出，
     *         错误码 {@code 5002}（服务异常）或包装自 DeepSeekClient 的异常
     */
    @Override
    public WritingCorrection correct(WritingRequest request) {
        log.info("DeepSeek AI writing correction: title={}, topicType={}, wordCount={}",
                request.getTitle(), request.getTopicType(),
                request.getContent() != null ? request.getContent().split("\\s+").length : 0);

        // 系统提示词：设定 AI 为英语写作导师，定义输出格式和评分标准
        String systemPrompt = """
                You are an expert English writing tutor for Chinese learners. Analyze the given essay and provide corrections.

                You MUST respond with a JSON object in this exact format (no markdown, no code fences, pure JSON only):
                {
                  "score": <number 0-100>,
                  "overallComment": "<detailed feedback in Chinese, 2-3 sentences>",
                  "items": [
                    {
                      "type": "Grammar|Spelling|Structure|Vocabulary|Cohesion",
                      "original": "<the problematic text>",
                      "suggestion": "<corrected or improved text>",
                      "explanation": "<简体中文解释>"
                    }
                  ]
                }

                Scoring criteria:
                - 90-100: Excellent, few to no errors, well-structured
                - 75-89: Good, minor errors, generally well-written
                - 60-74: Fair, noticeable errors but understandable
                - 40-59: Below average, significant errors
                - 0-39: Poor, major issues throughout

                Always include at least 2 correction items. Focus on the most impactful issues.
                Provide the overall comment in Chinese (简体中文) and explanations in Chinese.
                """;

        // 用户提示词：包含待批改的作文内容
        String userPrompt = String.format("""
                Please correct and evaluate this English essay:

                Title: %s
                Topic Type: %s

                ---
                %s
                ---
                """,
                request.getTitle(),
                request.getTopicType(),
                request.getContent());

        try {
            // 调用 DeepSeek API 获取批改结果
            String aiReply = deepSeekClient.chat(systemPrompt, userPrompt);

            // 清理可能存在的 markdown 代码块标记（AI 有时会返回 ```json ... ```）
            String cleanJson = aiReply.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();

            log.debug("DeepSeek writing correction JSON: {}", cleanJson);

            // 反序列化为 WritingCorrection 对象
            WritingCorrection correction = objectMapper.readValue(cleanJson, WritingCorrection.class);

            // 校验基本字段：确保 score 在 [0, 100] 范围内
            if (correction.getScore() < 0 || correction.getScore() > 100) {
                correction.setScore(Math.min(100, Math.max(0, correction.getScore())));
            }
            // 确保 items 不为 null（前端需要遍历）
            if (correction.getItems() == null) {
                correction.setItems(List.of());
            }
            // 确保 overallComment 不为 null
            if (correction.getOverallComment() == null) {
                correction.setOverallComment("批改完成。");
            }

            return correction;
        } catch (BusinessException e) {
            // 业务异常直接向上抛出（由全局异常处理器统一返回）
            throw e;
        } catch (Exception e) {
            // 其他异常（JSON 解析失败、网络异常等）包装为业务异常
            log.error("Failed to parse DeepSeek writing correction response", e);
            // 解析失败时返回基本反馈，避免前端崩溃
            return new WritingCorrection(60, "AI 批改服务暂时无法提供详细分析，请稍后重试。", List.of());
        }
    }
}
