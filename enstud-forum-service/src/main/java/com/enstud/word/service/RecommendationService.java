package com.enstud.word.service;

import com.enstud.word.dto.TopicRecommendationDTO;
import com.enstud.word.dto.WordRecommendationDTO;

import java.util.List;

/**
 * 学习推荐服务接口
 */
public interface RecommendationService {

    /**
     * 推荐单词（基于记忆等级分布）
     */
    List<WordRecommendationDTO> recommendWords(Long userId, int limit);

    /**
     * 推荐学习主题（基于已学词库分布）
     */
    List<TopicRecommendationDTO> recommendTopics(Long userId, int limit);
}
