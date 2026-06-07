package com.enstud.common.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DeepSeek API 响应体（OpenAI 兼容格式）
 * <p>
 * 对应 DeepSeek Chat Completion API 的响应结构，
 * 解析后通过 {@link #getContent()} 便捷获取 AI 回复文本。
 *
 * @author EnStudApp
 * @since 1.0.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeepSeekResponse {

    /** 本次响应唯一标识 */
    private String id;

    /** 对象类型，固定值 chat.completion */
    private String object;

    /** 响应创建时间戳（秒） */
    private Long created;

    /** 实际使用的模型名称 */
    private String model;

    /** 候选回复列表（通常只有一个） */
    private List<Choice> choices;

    /** Token 用量统计 */
    private Usage usage;

    /**
     * 候选回复实体
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {

        /** 候选索引，固定为 0 */
        private Integer index;

        /** 回复消息内容 */
        private ChatMessage message;

        /** 结束原因：stop / length / content_filter */
        private String finishReason;
    }

    /**
     * 消息实体（响应中的 message 字段）
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatMessage {

        /** 角色，固定为 assistant */
        private String role;

        /** 回复文本内容 */
        private String content;
    }

    /**
     * Token 用量统计
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {

        /** 提示词消耗的 Token 数 */
        private Integer promptTokens;

        /** 回复生成的 Token 数 */
        private Integer completionTokens;

        /** 总 Token 消耗 */
        private Integer totalTokens;
    }

    /**
     * 便捷方法：获取第一条回复的文本内容
     *
     * @return AI 回复文本；若解析失败返回 {@code null}
     */
    public String getContent() {
        if (choices != null && !choices.isEmpty() && choices.get(0).message != null) {
            return choices.get(0).message.getContent();
        }
        return null;
    }
}
