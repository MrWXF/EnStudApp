package com.enstud.chat.service;

import com.enstud.chat.dto.ChatTopicDTO;

import java.util.List;

/**
 * 对话主题推荐服务接口
 */
public interface ChatRecommendationService {

    /**
     * 推荐对话主题（混合方案：预置场景 + AI 生成）
     */
    List<ChatTopicDTO> recommendTopics(Long userId, int limit);
}
