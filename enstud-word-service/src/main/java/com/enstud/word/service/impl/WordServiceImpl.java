package com.enstud.word.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.common.BusinessException;
import com.enstud.common.entity.Word;
import com.enstud.common.enums.MemoryLevel;
import com.enstud.word.algorithm.Sm2Algorithm;
import com.enstud.word.dto.MemoryLevelDistributionDTO;
import com.enstud.word.dto.WordCardDTO;
import com.enstud.word.dto.WordbookDTO;
import com.enstud.word.entity.UserWordRecord;
import com.enstud.word.entity.Wordbook;
import com.enstud.word.mapper.UserWordRecordMapper;
import com.enstud.word.mapper.WordMapper;
import com.enstud.word.mapper.WordbookMapper;
import com.enstud.word.service.WordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordServiceImpl implements WordService {

    private final WordbookMapper wordbookMapper;
    private final WordMapper wordMapper;
    private final UserWordRecordMapper recordMapper;

    @Override
    public List<WordbookDTO> getWordbooks() {
        List<Wordbook> books = wordbookMapper.selectList(
                new LambdaQueryWrapper<Wordbook>()
                        .orderByAsc(Wordbook::getSortOrder)
                        .orderByDesc(Wordbook::getCreatedAt)
        );
        return books.stream()
                .map(b -> new WordbookDTO(
                        b.getId(), b.getName(), b.getDescription(), b.getCoverUrl(),
                        b.getWordCount(), b.getDifficulty(), b.getCategory(),
                        b.getIsOfficial() == 1))
                .collect(Collectors.toList());
    }

    @Override
    public List<WordCardDTO> getWordsByWordbook(Long wordbookId, Long userId, String cursor, int limit) {
        if (wordbookMapper.selectById(wordbookId) == null) {
            throw new BusinessException(2002, "词库不存在");
        }

        // 获取词库下的单词
        LambdaQueryWrapper<Word> wrapper = new LambdaQueryWrapper<Word>()
                .eq(Word::getWordbookId, wordbookId)
                .orderByAsc(Word::getId);
        if (cursor != null) {
            wrapper.gt(Word::getId, Long.parseLong(cursor));
        }
        wrapper.last("LIMIT " + limit);

        List<Word> words = wordMapper.selectList(wrapper);

        // 批量获取用户学习记录
        List<Long> wordIds = words.stream().map(Word::getId).toList();
        final Map<Long, UserWordRecord> recordMap;
        if (userId != null && !wordIds.isEmpty()) {
            List<UserWordRecord> records = recordMapper.selectList(
                    new LambdaQueryWrapper<UserWordRecord>()
                            .eq(UserWordRecord::getUserId, userId)
                            .in(UserWordRecord::getWordId, wordIds)
            );
            recordMap = records.stream()
                    .collect(Collectors.toMap(UserWordRecord::getWordId, r -> r, (a, b) -> a));
        } else {
            recordMap = Map.of();
        }

        return words.stream()
                .map(w -> {
                    UserWordRecord rec = recordMap.get(w.getId());
                    return new WordCardDTO(
                            w.getId(), w.getWord(), w.getPhoneticUk(), w.getPhoneticUs(),
                            w.getDefinitionCn(), w.getDefinitionEn(), w.getExampleSentence(),
                            w.getPartOfSpeech(),
                            rec != null ? rec.getMasteryLevel() : 0,
                            rec != null ? rec.getStatus() : "NEW"
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<WordCardDTO> getWordsForStudy(Long userId, Long wordbookId, int limit) {
        // 第一步：获取待复习的单词（排除精通的单词）
        List<UserWordRecord> dueRecords = recordMapper.findDueForReview(userId, limit);

        if (dueRecords.size() >= limit) {
            List<Long> wordIds = dueRecords.stream().map(UserWordRecord::getWordId).toList();
            List<Word> words = wordMapper.selectBatchIds(wordIds);
            Map<Long, Word> wordMap = words.stream()
                    .collect(Collectors.toMap(Word::getId, w -> w));

            return dueRecords.stream()
                    .map(r -> {
                        Word w = wordMap.get(r.getWordId());
                        if (w == null) return null;
                        return new WordCardDTO(w.getId(), w.getWord(), w.getPhoneticUk(),
                                w.getPhoneticUs(), w.getDefinitionCn(), w.getDefinitionEn(),
                                w.getExampleSentence(), w.getPartOfSpeech(),
                                r.getMasteryLevel(), r.getStatus());
                    })
                    .filter(d -> d != null)
                    .collect(Collectors.toList());
        }

        // 第二步：复习的单词不够，从词库拿新单词
        int remaining = limit - dueRecords.size();
        List<Long> learnedIds = dueRecords.isEmpty()
                ? recordMapper.selectList(
                        new LambdaQueryWrapper<UserWordRecord>().eq(UserWordRecord::getUserId, userId))
                        .stream().map(UserWordRecord::getWordId).toList()
                : dueRecords.stream().map(UserWordRecord::getWordId).toList();

        LambdaQueryWrapper<Word> wrapper = new LambdaQueryWrapper<Word>()
                .eq(Word::getWordbookId, wordbookId)
                .orderByAsc(Word::getId);
        if (!learnedIds.isEmpty()) {
            wrapper.notIn(Word::getId, learnedIds);
        }
        wrapper.last("LIMIT " + remaining);

        List<Word> newWords = wordMapper.selectList(wrapper);

        // 合并结果
        List<WordCardDTO> result = new ArrayList<>();

        for (UserWordRecord r : dueRecords) {
            Word w = wordMapper.selectById(r.getWordId());
            if (w != null) {
                result.add(new WordCardDTO(w.getId(), w.getWord(), w.getPhoneticUk(),
                        w.getPhoneticUs(), w.getDefinitionCn(), w.getDefinitionEn(),
                        w.getExampleSentence(), w.getPartOfSpeech(),
                        r.getMasteryLevel(), r.getStatus()));
            }
        }

        for (Word w : newWords) {
            result.add(new WordCardDTO(w.getId(), w.getWord(), w.getPhoneticUk(),
                    w.getPhoneticUs(), w.getDefinitionCn(), w.getDefinitionEn(),
                    w.getExampleSentence(), w.getPartOfSpeech(), 0, "NEW"));
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReview(Long userId, Long wordId, int quality) {
        Word word = wordMapper.selectById(wordId);
        if (word == null) {
            throw new BusinessException(2001, "单词不存在");
        }

        // 查找或创建用户学习记录
        UserWordRecord record = recordMapper.selectOne(
                new LambdaQueryWrapper<UserWordRecord>()
                        .eq(UserWordRecord::getUserId, userId)
                        .eq(UserWordRecord::getWordId, wordId)
        );

        if (record == null) {
            record = new UserWordRecord();
            record.setUserId(userId);
            record.setWordId(wordId);
            record.setEaseFactor(2.5);
            record.setReviewInterval(0);
            record.setRepetitions(0);
            record.setMasteryLevel(0);
            record.setMemoryLevel(0);
            record.setStatus("LEARNING");
        }

        // SM-2 算法更新（内部自动更新 memoryLevel）
        Sm2Algorithm.update(record, quality);

        if (record.getId() == null) {
            recordMapper.insert(record);
        } else {
            recordMapper.updateById(record);
        }

        log.info("用户复习完成, userId={}, wordId={}, quality={}, mastery={}, memoryLevel={}",
                userId, wordId, quality, record.getMasteryLevel(), record.getMemoryLevel());
    }

    @Override
    public MemoryLevelDistributionDTO getMemoryLevelDistribution(Long userId) {
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

        return new MemoryLevelDistributionDTO(notLearned, fuzzy, familiar, basic, proficient, mastered);
    }

    @Override
    public List<WordCardDTO> getWordsByMemoryLevel(Long userId, Long wordbookId, Integer memoryLevel, String cursor, int limit) {
        if (memoryLevel < 0 || memoryLevel > 5) {
            throw new BusinessException(2003, "记忆等级必须在 0-5 之间");
        }

        // 筛选用户学习记录中指定记忆等级的单词 ID
        LambdaQueryWrapper<UserWordRecord> recordWrapper = new LambdaQueryWrapper<UserWordRecord>()
                .eq(UserWordRecord::getUserId, userId)
                .eq(UserWordRecord::getMemoryLevel, memoryLevel);
        List<UserWordRecord> records = recordMapper.selectList(recordWrapper);
        List<Long> wordIds = records.stream().map(UserWordRecord::getWordId).toList();
        Map<Long, UserWordRecord> recordMap = records.stream()
                .collect(Collectors.toMap(UserWordRecord::getWordId, r -> r, (a, b) -> a));

        if (wordIds.isEmpty()) {
            return List.of();
        }

        // 按词库和游标分页
        LambdaQueryWrapper<Word> wordWrapper = new LambdaQueryWrapper<Word>()
                .in(Word::getId, wordIds)
                .orderByAsc(Word::getId);
        if (wordbookId != null) {
            wordWrapper.eq(Word::getWordbookId, wordbookId);
        }
        if (cursor != null) {
            wordWrapper.gt(Word::getId, Long.parseLong(cursor));
        }
        wordWrapper.last("LIMIT " + limit);

        List<Word> words = wordMapper.selectList(wordWrapper);

        return words.stream()
                .map(w -> {
                    UserWordRecord rec = recordMap.get(w.getId());
                    return new WordCardDTO(
                            w.getId(), w.getWord(), w.getPhoneticUk(), w.getPhoneticUs(),
                            w.getDefinitionCn(), w.getDefinitionEn(), w.getExampleSentence(),
                            w.getPartOfSpeech(),
                            rec != null ? rec.getMasteryLevel() : 0,
                            rec != null ? rec.getStatus() : "NEW"
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustMemoryLevel(Long userId, Long wordId, Integer targetLevel) {
        if (targetLevel < 0 || targetLevel > 5) {
            throw new BusinessException(2003, "记忆等级必须在 0-5 之间");
        }

        UserWordRecord record = recordMapper.selectOne(
                new LambdaQueryWrapper<UserWordRecord>()
                        .eq(UserWordRecord::getUserId, userId)
                        .eq(UserWordRecord::getWordId, wordId)
        );

        if (record == null) {
            throw new BusinessException(2004, "尚未学习该单词，请先复习后再调整记忆等级");
        }

        int oldLevel = record.getMemoryLevel() != null ? record.getMemoryLevel() : 0;
        record.setMemoryLevel(targetLevel);

        // 同步调整 SM-2 参数，让算法与新等级匹配
        if (targetLevel < oldLevel) {
            // 降级：重置部分记忆参数，增加复习频率
            record.setReviewInterval(Math.max(1, record.getReviewInterval() / 2));
            record.setEaseFactor(Math.max(1.3, record.getEaseFactor() - 0.2));
            // 重新计算 masteryLevel
            int newMastery = Math.max(0, targetLevel * 20 - 5);
            if (targetLevel <= 1) {
                record.setRepetitions(0);
                record.setStatus("LEARNING");
            }
            record.setMasteryLevel(newMastery);
        } else if (targetLevel > oldLevel) {
            // 升级：延长复习间隔
            record.setReviewInterval(Math.max(1, (int) (record.getReviewInterval() * 1.5)));
            record.setEaseFactor(Math.min(3.0, record.getEaseFactor() + 0.15));
            // 设置对应的 masteryLevel
            int newMastery = Math.min(100, Math.max(1, targetLevel * 20 - 5));
            record.setMasteryLevel(newMastery);
            if (targetLevel >= 4) {
                record.setRepetitions(Math.max(record.getRepetitions(), 5));
            }
        }

        // 如果调整到精通，直接标记为 MASTERED
        if (targetLevel >= 5) {
            record.setStatus("MASTERED");
        }

        // 更新下次复习时间
        record.setNextReviewTime(LocalDateTime.now().plusDays(record.getReviewInterval()));
        record.setLastReviewTime(LocalDateTime.now());
        recordMapper.updateById(record);

        log.info("用户调整记忆等级, userId={}, wordId={}, oldLevel={}, newLevel={}",
                userId, wordId, oldLevel, targetLevel);
    }
}
