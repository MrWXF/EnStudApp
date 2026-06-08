package com.enstud.word.service;

import com.enstud.word.dto.MemoryLevelDistributionDTO;
import com.enstud.word.dto.WordCardDTO;
import com.enstud.word.dto.WordStatsDTO;
import com.enstud.word.dto.WordbookDTO;

import java.util.List;

public interface WordService {

    /** 获取所有词库列表 */
    List<WordbookDTO> getWordbooks();

    /** 获取词库下的单词列表 */
    List<WordCardDTO> getWordsByWordbook(Long wordbookId, Long userId, String cursor, int limit);

    /** 开始学习：获取待学习/复习的单词 */
    List<WordCardDTO> getWordsForStudy(Long userId, Long wordbookId, int limit);

    /** 提交复习结果 */
    void submitReview(Long userId, Long wordId, int quality);

    /** 获取用户记忆等级分布统计 */
    MemoryLevelDistributionDTO getMemoryLevelDistribution(Long userId);

    /** 按记忆等级筛选单词 */
    List<WordCardDTO> getWordsByMemoryLevel(Long userId, Long wordbookId, Integer memoryLevel, String cursor, int limit);

    /** 手动调整单词记忆等级 */
    void adjustMemoryLevel(Long userId, Long wordId, Integer targetLevel);
}
