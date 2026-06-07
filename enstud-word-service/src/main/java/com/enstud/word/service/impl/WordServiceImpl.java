package com.enstud.word.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.common.BusinessException;
import com.enstud.common.entity.Word;
import com.enstud.word.algorithm.Sm2Algorithm;
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
        // 第一步：获取待复习的单词
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
            record.setStatus("LEARNING");
        }

        // SM-2 算法更新
        Sm2Algorithm.update(record, quality);

        if (record.getId() == null) {
            recordMapper.insert(record);
        } else {
            recordMapper.updateById(record);
        }

        log.info("用户复习完成, userId={}, wordId={}, quality={}, mastery={}",
                userId, wordId, quality, record.getMasteryLevel());
    }
}
