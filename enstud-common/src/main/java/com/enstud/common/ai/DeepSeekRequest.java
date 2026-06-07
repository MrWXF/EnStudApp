package com.enstud.common.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DeepSeek API 请求体（OpenAI 兼容格式）
 * <p>
 * 封装调用 DeepSeek Chat Completion API 所需的所有参数，
 * 包括模型名称、对话历史、温度等超参数。
 *
 * @author EnStudApp
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeepSeekRequest {

    /** 模型名称，如 deepseek-chat、deepseek-reasoner */
    private String model;

    /** 对话消息列表，按时间顺序排列 */
    private List<ChatMessage> messages;

    /** 采样温度，范围 [0, 2]。越高越随机，越低越确定。默认 0.7 */
    private Double temperature;

    /** 最大生成 token 数，默认 2048 */
    private Integer maxTokens;

    /** 是否启用流式输出，当前仅支持 false（非流式） */
    private Boolean stream;

    /**
     * 对话消息实体
     * <p>
     * 对应 OpenAI Chat Completion API 的 message 对象，
     * role 必须为 system / user / assistant 之一。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatMessage {

        /**
         * 消息角色
         * <ul>
         *   <li>{@code system} — 系统提示词，设置 AI 行为规则</li>
         *   <li>{@code user} — 用户输入消息</li>
         *   <li>{@code assistant} — AI 历史回复</li>
         * </ul>
         */
        private String role;

        /** 消息内容（纯文本，不包含多模态内容） */
        private String content;
    }

    /**
     * 快捷构造方法，使用默认超参数
     *
     * @param model   模型名称
     * @param messages 完整对话历史
     * @return 配置好的请求对象
     */
    public static DeepSeekRequest create(String model, List<ChatMessage> messages) {
        DeepSeekRequest req = new DeepSeekRequest();
        req.setModel(model);
        req.setMessages(messages);
        req.setTemperature(0.7);
        req.setMaxTokens(2048);
        req.setStream(false);
        return req;
    }
}
