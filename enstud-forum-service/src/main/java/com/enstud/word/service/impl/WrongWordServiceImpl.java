package com.enstud.word.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.common.BusinessException;
import com.enstud.common.constant.ErrorCode;
import com.enstud.common.entity.Word;
import com.enstud.word.dto.WrongWordDTO;
import com.enstud.word.entity.UserWordRecord;
import com.enstud.word.mapper.UserWordRecordMapper;
import com.enstud.word.mapper.WordMapper;
import com.enstud.word.service.WrongWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WrongWordServiceImpl implements WrongWordService {

    private final UserWordRecordMapper recordMapper;
    private final WordMapper wordMapper;

    @Override
    public List<WrongWordDTO> getWrongWords(Long userId, String timeFilter, String cursor, int limit) {
        LambdaQueryWrapper<UserWordRecord> wrapper = new LambdaQueryWrapper<UserWordRecord>()
                .eq(UserWordRecord::getUserId, userId)
                .gt(UserWordRecord::getWrongCount, 0)
                .lt(UserWordRecord::getMasteryLevel, 80)
                .orderByDesc(UserWordRecord::getUpdatedAt);

        // 时间筛选
        if ("week".equals(timeFilter)) {
            LocalDateTime weekAgo = LocalDate.now().minusWeeks(1).atStartOfDay();
            wrapper.ge(UserWordRecord::getUpdatedAt, weekAgo);
        } else if ("month".equals(timeFilter)) {
            LocalDateTime monthAgo = LocalDate.now().minusMonths(1).atStartOfDay();
            wrapper.ge(UserWordRecord::getUpdatedAt, monthAgo);
        }

        // 游标分页
        if (cursor != null) {
            wrapper.lt(UserWordRecord::getId, Long.parseLong(cursor));
        }
        wrapper.last("LIMIT " + limit);

        List<UserWordRecord> records = recordMapper.selectList(wrapper);
        if (records.isEmpty()) {
            return List.of();
        }

        // 批量查询单词信息
        List<Long> wordIds = records.stream().map(UserWordRecord::getWordId).toList();
        List<Word> words = wordMapper.selectBatchIds(wordIds);
        Map<Long, Word> wordMap = words.stream()
                .collect(Collectors.toMap(Word::getId, w -> w, (a, b) -> a));

        return records.stream()
                .map(r -> {
                    Word word = wordMap.get(r.getWordId());
                    if (word == null) return null;
                    return new WrongWordDTO(
                            r.getId(),
                            r.getWordId(),
                            word.getWord(),
                            word.getDefinitionCn(),
                            r.getWrongCount(),
                            r.getRepetitions(),
                            r.getUpdatedAt()
                    );
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewBatch(Long userId, List<Long> wordIds) {
        if (wordIds == null || wordIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "单词ID列表不能为空");
        }

        log.info("批量复习错词: userId={}, count={}", userId, wordIds.size());
        // 这里只是标记开始复习，实际复习逻辑在 /word/review 接口
        // 前端会逐个调用 /word/review 提交复习结果
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetWrongWord(Long userId, Long recordId) {
        UserWordRecord record = recordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "记录不存在");
        }

        // 重置错词状态
        record.setWrongCount(0);
        record.setMasteryLevel(80);
        recordMapper.updateById(record);

        log.info("重置错词状态: userId={}, recordId={}", userId, recordId);
    }
}
