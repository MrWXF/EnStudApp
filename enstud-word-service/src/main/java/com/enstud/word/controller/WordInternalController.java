package com.enstud.word.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.common.Result;
import com.enstud.word.dto.MemoryLevelDistributionDTO;
import com.enstud.word.entity.UserWordRecord;
import com.enstud.word.mapper.UserWordRecordMapper;
import com.enstud.word.mapper.WordMapper;
import com.enstud.common.entity.Word;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 内部接口：单词学习统计（供 user-service 聚合调用，无需鉴权）
 */
@Tag(name = "单词服务内部接口")
@RestController
@RequestMapping("/word/internal")
@RequiredArgsConstructor
public class WordInternalController {

    private final UserWordRecordMapper recordMapper;
    private final WordMapper wordMapper;

    @Operation(summary = "内部接口：获取用户单词学习统计")
    @GetMapping("/stats/{userId}")
    public Result<WordInternalStatsResponse> getStats(
            @PathVariable Long userId,
            @Parameter(description = "统计日期，默认当天")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate today) {

        if (today == null) {
            today = LocalDate.now();
        }
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        // 今日学习单词数：createdAt >= todayStart
        long todayLearned = recordMapper.selectCount(
                new LambdaQueryWrapper<UserWordRecord>()
                        .eq(UserWordRecord::getUserId, userId)
                        .ge(UserWordRecord::getCreatedAt, todayStart)
                        .le(UserWordRecord::getCreatedAt, todayEnd)
        );

        // 累计学习单词数：status = 'LEARNING' or 'MASTERED'
        long totalLearned = recordMapper.selectCount(
                new LambdaQueryWrapper<UserWordRecord>()
                        .eq(UserWordRecord::getUserId, userId)
                        .in(UserWordRecord::getStatus, "LEARNING", "MASTERED")
        );

        // 记忆等级分布
        List<UserWordRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<UserWordRecord>()
                        .eq(UserWordRecord::getUserId, userId)
        );
        long notLearned = 0, fuzzy = 0, familiar = 0, basic = 0, proficient = 0, mastered = 0;
        for (UserWordRecord r : records) {
            int ml = r.getMemoryLevel() != null ? r.getMemoryLevel() : 0;
            switch (ml) {
                case 0 -> notLearned++;
                case 1 -> fuzzy++;
                case 2 -> familiar++;
                case 3 -> basic++;
                case 4 -> proficient++;
                case 5 -> mastered++;
                default -> notLearned++;
            }
        }
        MemoryLevelDistributionDTO memoryDistribution = new MemoryLevelDistributionDTO(
                notLearned, fuzzy, familiar, basic, proficient, mastered
        );

        return Result.success(new WordInternalStatsResponse(todayLearned, totalLearned, memoryDistribution));
    }

    /**
     * 单词学习统计响应体
     */
    public record WordInternalStatsResponse(
            long todayLearned,
            long totalLearned,
            MemoryLevelDistributionDTO memoryDistribution
    ) {}

    // ==================== 学习闭环：供其他服务获取用户学习数据 ====================

    @Operation(summary = "内部接口：获取用户正在学习的单词")
    @GetMapping("/learning-words/{userId}")
    public Result<List<Map<String, Object>>> getLearningWords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "20") int limit) {

        // 查询用户正在学习（未掌握）的单词记录
        List<UserWordRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<UserWordRecord>()
                        .eq(UserWordRecord::getUserId, userId)
                        .in(UserWordRecord::getStatus, "LEARNING", "REVIEWING")
                        .orderByDesc(UserWordRecord::getUpdatedAt)
                        .last("LIMIT " + limit)
        );

        if (records.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        // 批量获取单词详情
        List<Long> wordIds = records.stream().map(UserWordRecord::getWordId).toList();
        List<Word> words = wordMapper.selectBatchIds(wordIds);

        // 组装结果
        Map<Long, Word> wordMap = new HashMap<>();
        words.forEach(w -> wordMap.put(w.getId(), w));

        List<Map<String, Object>> result = new ArrayList<>();
        for (UserWordRecord r : records) {
            Word w = wordMap.get(r.getWordId());
            if (w != null) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("wordId", w.getId());
                item.put("word", w.getWord());
                item.put("definitionCn", w.getDefinitionCn());
                item.put("phoneticUs", w.getPhoneticUs());
                item.put("phoneticUk", w.getPhoneticUk());
                item.put("masteryLevel", r.getMasteryLevel());
                item.put("status", r.getStatus());
                result.add(item);
            }
        }
        return Result.success(result);
    }

    @Operation(summary = "内部接口：获取用户今日待复习单词")
    @GetMapping("/review-words/{userId}")
    public Result<List<Map<String, Object>>> getReviewWords(@PathVariable Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<UserWordRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<UserWordRecord>()
                        .eq(UserWordRecord::getUserId, userId)
                        .le(UserWordRecord::getNextReviewTime, now)
                        .in(UserWordRecord::getStatus, "LEARNING", "REVIEWING")
                        .orderByAsc(UserWordRecord::getNextReviewTime)
                        .last("LIMIT 20")
        );

        if (records.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        List<Long> wordIds = records.stream().map(UserWordRecord::getWordId).toList();
        List<Word> words = wordMapper.selectBatchIds(wordIds);
        Map<Long, Word> wordMap = new HashMap<>();
        words.forEach(w -> wordMap.put(w.getId(), w));

        List<Map<String, Object>> result = new ArrayList<>();
        for (UserWordRecord r : records) {
            Word w = wordMap.get(r.getWordId());
            if (w != null) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("wordId", w.getId());
                item.put("word", w.getWord());
                item.put("definitionCn", w.getDefinitionCn());
                item.put("nextReviewTime", r.getNextReviewTime());
                result.add(item);
            }
        }
        return Result.success(result);
    }
}
