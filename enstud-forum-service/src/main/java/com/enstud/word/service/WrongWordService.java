package com.enstud.word.service;

import com.enstud.word.dto.WrongWordDTO;

import java.util.List;

/**
 * 错词本服务接口
 */
public interface WrongWordService {

    /**
     * 获取错词列表
     */
    List<WrongWordDTO> getWrongWords(Long userId, String timeFilter, String cursor, int limit);

    /**
     * 批量复习错词
     */
    void reviewBatch(Long userId, List<Long> wordIds);

    /**
     * 重置错词状态（掌握后清除）
     */
    void resetWrongWord(Long userId, Long recordId);
}
