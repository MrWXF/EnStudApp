package com.enstud.chat.ai;

/**
 * AI 对话客户端抽象接口
 * 支持切换不同的 AI 后端（OpenAI / 文心一言 / 通义千问 / Mock）
 */
public interface AiChatClient {

    /** 发送消息并获取 AI 回复 */
    AiResponse chat(AiRequest request);
}
