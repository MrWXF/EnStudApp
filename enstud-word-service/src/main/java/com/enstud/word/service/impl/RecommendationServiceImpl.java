package com.enstud.word.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.common.entity.Word;
import com.enstud.word.dto.MemoryLevelDistributionDTO;
import com.enstud.word.dto.TopicRecommendationDTO;
import com.enstud.word.dto.WordRecommendationDTO;
import com.enstud.word.entity.UserWordRecord;
import com.enstud.word.entity.Wordbook;
import com.enstud.word.mapper.UserWordRecordMapper;
import com.enstud.word.mapper.WordMapper;
import com.enstud.word.mapper.WordbookMapper;
import com.enstud.word.service.RecommendationService;
import com.enstud.word.service.WordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final UserWordRecordMapper recordMapper;
    private final WordMapper wordMapper;
    private final WordbookMapper wordbookMapper;
    private final WordService wordService;

    @Override
    public List<WordRecommendationDTO> recommendWords(Long userId, int limit) {
        // 获取记忆等级分布
        MemoryLevelDistributionDTO distribution = wordService.getMemoryLevelDistribution(userId);
        long total = distribution.notLearned() + distribution.fuzzy() + distribution.familiar()
                + distribution.basic() + distribution.proficient() + distribution.mastered();

        if (total == 0) {
            // 新用户，推荐基础单词
            return recommendBasicWords(limit);
        }

        // 计算各等级比例
        double fuzzyRatio = (distribution.fuzzy() + distribution.familiar()) * 1.0 / total;
        double masteredRatio = distribution.mastered() * 1.0 / total;
        double notLearnedRatio = distribution.notLearned() * 1.0 / total;

        List<WordRecommendationDTO> recommendations = new ArrayList<>();

        if (fuzzyRatio > 0.5) {
            // 模糊/熟悉比例 > 50%：推荐复习类单词（memory_level 2-3）
            recommendations = recommendByMemoryLevel(userId, List.of(2, 3), limit, "巩固基础");
        } else if (masteredRatio > 0.7) {
            // 掌握比例 > 70%：推荐进阶单词（difficulty_level 高一级）
            recommendations = recommendAdvancedWords(userId, limit);
        } else if (notLearnedRatio > 0.6) {
            // 新学比例 > 60%：推荐基础巩固
            recommendations = recommendBasicWords(limit);
        } else {
            // 均衡推荐
            recommendations = recommendByMemoryLevel(userId, List.of(1, 2, 3), limit, "综合提升");
        }

        return recommendations;
    }

    @Override
    public List<TopicRecommendationDTO> recommendTopics(Long userId, int limit) {
        // 查询用户已学单词的词库分布
        List<UserWordRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<UserWordRecord>()
                        .eq(UserWordRecord::getUserId, userId)
        );

        if (records.isEmpty()) {
            // 新用户，推荐热门词库
            return getPopularTopics(limit);
        }

        // 统计词库分布
        List<Long> wordIds = records.stream().map(UserWordRecord::getWordId).toList();
        List<Word> words = wordMapper.selectBatchIds(wordIds);
        Map<Long, Long> wordbookCount = words.stream()
                .filter(w -> w.getWordbookId() != null)
                .collect(Collectors.groupingBy(Word::getWordbookId, Collectors.counting()));

        // 获取词库信息
        List<Long> wordbookIds = new ArrayList<>(wordbookCount.keySet());
        List<Wordbook> wordbooks = wordbookMapper.selectBatchIds(wordbookIds);

        // 推荐用户较少接触的词库
        return wordbooks.stream()
                .sorted((a, b) -> {
                    long countA = wordbookCount.getOrDefault(a.getId(), 0L);
                    long countB = wordbookCount.getOrDefault(b.getId(), 0L);
                    return Long.compare(countA, countB);
                })
                .limit(limit)
                .map(wb -> new TopicRecommendationDTO(
                        wb.getName(),
                        wb.getCategory(),
                        wb.getDifficulty(),
                        wb.getDescription(),
                        wb.getWordCount()
                ))
                .collect(Collectors.toList());
    }

    private List<WordRecommendationDTO> recommendBasicWords(int limit) {
        List<Word> words = wordMapper.selectList(
                new LambdaQueryWrapper<Word>()
                        .le(Word::getDifficultyLevel, 2)
                        .orderByAsc(Word::getDifficultyLevel)
                        .last("LIMIT " + limit)
        );

        return words.stream()
                .map(w -> new WordRecommendationDTO(
                        w.getId(),
                        w.getWord(),
                        w.getDefinitionCn(),
                        w.getDifficultyLevel(),
                        "基础巩固",
                        getWordbookCategory(w.getWordbookId())
                ))
                .collect(Collectors.toList());
    }

    private List<WordRecommendationDTO> recommendAdvancedWords(Long userId, int limit) {
        // 获取用户当前最高难度
        List<UserWordRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<UserWordRecord>()
                        .eq(UserWordRecord::getUserId, userId)
        );
        List<Long> wordIds = records.stream().map(UserWordRecord::getWordId).toList();
        List<Word> learnedWords = wordMapper.selectBatchIds(wordIds);
        int maxDifficulty = learnedWords.stream()
                .mapToInt(Word::getDifficultyLevel)
                .max()
                .orElse(2);

        // 推荐高一级难度的单词
        List<Word> words = wordMapper.selectList(
                new LambdaQueryWrapper<Word>()
                        .eq(Word::getDifficultyLevel, Math.min(maxDifficulty + 1, 5))
                        .last("LIMIT " + limit)
        );

        return words.stream()
                .map(w -> new WordRecommendationDTO(
                        w.getId(),
                        w.getWord(),
                        w.getDefinitionCn(),
                        w.getDifficultyLevel(),
                        "挑战进阶",
                        getWordbookCategory(w.getWordbookId())
                ))
                .collect(Collectors.toList());
    }

    private List<WordRecommendationDTO> recommendByMemoryLevel(Long userId, List<Integer> levels, int limit, String reason) {
        List<UserWordRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<UserWordRecord>()
                        .eq(UserWordRecord::getUserId, userId)
                        .in(UserWordRecord::getMemoryLevel, levels)
                        .last("LIMIT " + limit)
        );

        List<Long> wordIds = records.stream().map(UserWordRecord::getWordId).toList();
        if (wordIds.isEmpty()) {
            return List.of();
        }

        List<Word> words = wordMapper.selectBatchIds(wordIds);
        return words.stream()
                .map(w -> new WordRecommendationDTO(
                        w.getId(),
                        w.getWord(),
                        w.getDefinitionCn(),
                        w.getDifficultyLevel(),
                        reason,
                        getWordbookCategory(w.getWordbookId())
                ))
                .collect(Collectors.toList());
    }

    private List<TopicRecommendationDTO> getPopularTopics(int limit) {
        List<Wordbook> wordbooks = wordbookMapper.selectList(
                new LambdaQueryWrapper<Wordbook>()
                        .eq(Wordbook::getIsOfficial, 1)
                        .orderByDesc(Wordbook::getWordCount)
                        .last("LIMIT " + limit)
        );

        return wordbooks.stream()
                .map(wb -> new TopicRecommendationDTO(
                        wb.getName(),
                        wb.getCategory(),
                        wb.getDifficulty(),
                        wb.getDescription(),
                        wb.getWordCount()
                ))
                .collect(Collectors.toList());
    }

    private String getWordbookCategory(Long wordbookId) {
        if (wordbookId == null) return "通用";
        Wordbook wordbook = wordbookMapper.selectById(wordbookId);
        return wordbook != null ? wordbook.getCategory() : "通用";
    }
}
