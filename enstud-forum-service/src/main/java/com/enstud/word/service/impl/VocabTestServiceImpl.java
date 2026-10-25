package com.enstud.word.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.common.BusinessException;
import com.enstud.common.constant.ErrorCode;
import com.enstud.word.dto.VocabTestAnswerRequest;
import com.enstud.word.dto.VocabTestResultDTO;
import com.enstud.word.dto.VocabTestWordDTO;
import com.enstud.word.entity.VocabTestRecord;
import com.enstud.word.mapper.VocabTestRecordMapper;
import com.enstud.word.service.VocabTestService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 词汇量测试：按频段采样估算法
 * 词池按词汇频率分为 6 个频段（初中 → 进阶），每段随机抽 5 词，
 * 以各频段认知比例 × 频段容量求和估算总词汇量；
 * 混入 2 个伪词，答"认识"会被扣分，抑制乱猜。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VocabTestServiceImpl implements VocabTestService {

    /** 每个频段抽取的单词数 */
    private static final int WORDS_PER_BAND = 5;
    /** 混入的伪词数 */
    private static final int FAKE_WORDS_PER_TEST = 2;
    /** 有效作答下限（少于则视为无效提交） */
    private static final int MIN_VALID_ANSWERS = 20;
    /** 每个伪词"认识"的扣分（模糊折半） */
    private static final double FAKE_WORD_PENALTY = 800;

    private final VocabTestRecordMapper recordMapper;
    private final ObjectMapper objectMapper;

    /** 有序频段定义（band 1 → 6，容量递增） */
    private final List<BandInfo> bands = new ArrayList<>();
    /** 单词 → 所属频段 */
    private final Map<String, BandInfo> wordToBand = new HashMap<>();
    /** 伪词集合 */
    private final Set<String> fakeWords = new HashSet<>();

    record BandInfo(int band, String label, int cumulativeSize, List<String> words) {}

    @PostConstruct
    void loadWordPool() {
        try (InputStream in = new ClassPathResource("vocab-test-words.json").getInputStream()) {
            Map<String, Object> root = objectMapper.readValue(in, new TypeReference<>() {});
            List<Map<String, Object>> bandList = (List<Map<String, Object>>) root.get("bands");
            for (Map<String, Object> b : bandList) {
                BandInfo info = new BandInfo(
                        (int) b.get("band"),
                        (String) b.get("label"),
                        (int) b.get("cumulativeSize"),
                        (List<String>) b.get("words"));
                bands.add(info);
                info.words().forEach(w -> wordToBand.put(w, info));
            }
            ((List<String>) root.get("fakeWords")).forEach(fakeWords::add);
            log.info("词汇量测试词池加载完成: {} 个频段, {} 个单词, {} 个伪词",
                    bands.size(), wordToBand.size(), fakeWords.size());
        } catch (Exception e) {
            throw new IllegalStateException("加载词汇量测试词池失败", e);
        }
    }

    @Override
    public List<VocabTestWordDTO> getTestWords() {
        List<String> picked = new ArrayList<>();
        for (BandInfo band : bands) {
            List<String> pool = new ArrayList<>(band.words());
            Collections.shuffle(pool);
            picked.addAll(pool.subList(0, Math.min(WORDS_PER_BAND, pool.size())));
        }
        List<String> fakePool = new ArrayList<>(fakeWords);
        Collections.shuffle(fakePool);
        picked.addAll(fakePool.subList(0, Math.min(FAKE_WORDS_PER_TEST, fakePool.size())));

        Collections.shuffle(picked);
        return picked.stream().map(VocabTestWordDTO::new).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VocabTestResultDTO submit(Long userId, VocabTestAnswerRequest request) {
        // 按频段统计认知情况
        Map<Integer, int[]> bandTally = new HashMap<>(); // band -> [known, vague, unknown]
        double fakeCredits = 0;

        for (VocabTestAnswerRequest.Answer a : request.answers()) {
            int knowledge = Math.max(0, Math.min(2, a.knowledge()));
            BandInfo band = wordToBand.get(a.word());
            if (band != null) {
                int[] tally = bandTally.computeIfAbsent(band.band(), k -> new int[3]);
                tally[knowledge]++;
            } else if (fakeWords.contains(a.word())) {
                // 伪词答"认识"计 1 分，"模糊"计 0.5 分
                fakeCredits += knowledge == 2 ? 1 : knowledge == 1 ? 0.5 : 0;
            }
            // 词池外的作答直接忽略（防伪造）
        }

        int validAnswers = bandTally.values().stream()
                .mapToInt(t -> t[0] + t[1] + t[2]).sum();
        if (validAnswers < MIN_VALID_ANSWERS) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // 估算：各频段认知比例 × 频段容量增量，求和
        // tally 按 knowledge 值索引：tally[0]=不认识, tally[1]=模糊, tally[2]=认识
        List<VocabTestResultDTO.BandResult> bandResults = new ArrayList<>();
        double estimate = 0;
        int prevSize = 0;
        for (BandInfo band : bands) {
            int[] tally = bandTally.getOrDefault(band.band(), new int[3]);
            int total = tally[0] + tally[1] + tally[2];
            double ratio = total == 0 ? 0 : (tally[2] + 0.5 * tally[1]) / total;
            estimate += ratio * (band.cumulativeSize() - prevSize);
            prevSize = band.cumulativeSize();
            bandResults.add(new VocabTestResultDTO.BandResult(
                    band.band(), band.label(), tally[2], tally[1], tally[0], ratio));
        }

        // 伪词惩罚后取整
        long estimatedSize = Math.max(0, Math.round(estimate - fakeCredits * FAKE_WORD_PENALTY));
        String[] level = resolveLevel(estimatedSize);

        // 持久化
        VocabTestRecord record = new VocabTestRecord();
        record.setUserId(userId);
        record.setEstimatedSize((int) estimatedSize);
        record.setLevelLabel(level[0]);
        record.setCreatedAt(LocalDateTime.now());
        try {
            record.setBandStats(objectMapper.writeValueAsString(bandResults));
        } catch (Exception e) {
            log.warn("频段明细序列化失败: userId={}", userId, e);
        }
        recordMapper.insert(record);

        log.info("词汇量测试完成: userId={}, estimatedSize={}, level={}, fakeCredits={}",
                userId, estimatedSize, level[0], fakeCredits);

        return new VocabTestResultDTO(estimatedSize, level[0], level[1], bandResults, record.getCreatedAt());
    }

    @Override
    public VocabTestResultDTO getLatest(Long userId) {
        VocabTestRecord record = recordMapper.selectOne(
                new LambdaQueryWrapper<VocabTestRecord>()
                        .eq(VocabTestRecord::getUserId, userId)
                        .orderByDesc(VocabTestRecord::getId)
                        .last("LIMIT 1"));
        if (record == null) {
            return null;
        }

        List<VocabTestResultDTO.BandResult> bandResults = List.of();
        if (record.getBandStats() != null) {
            try {
                bandResults = objectMapper.readValue(record.getBandStats(),
                        new TypeReference<List<VocabTestResultDTO.BandResult>>() {});
            } catch (Exception e) {
                log.warn("频段明细反序列化失败: userId={}", userId, e);
            }
        }

        String[] level = resolveLevel(record.getEstimatedSize());
        return new VocabTestResultDTO(record.getEstimatedSize(), record.getLevelLabel(),
                level[1], bandResults, record.getCreatedAt());
    }

    /** 按估算值解析水平标签与描述 */
    private String[] resolveLevel(long size) {
        if (size >= 10000) {
            return new String[]{"词汇大神", "你的词汇量已超过绝大多数学习者，原版书阅读基本无压力"};
        }
        if (size >= 8000) {
            return new String[]{"考研/雅思水平", "可以应对考研英语和雅思阅读的核心词汇"};
        }
        if (size >= 6000) {
            return new String[]{"六级水平", "达到大学英语六级的词汇要求，向更高阶进发"};
        }
        if (size >= 4500) {
            return new String[]{"四级水平", "达到大学英语四级的词汇要求，继续巩固冲刺六级"};
        }
        if (size >= 3000) {
            return new String[]{"高中水平", "达到高中英语的词汇要求，大学词汇等你挑战"};
        }
        if (size >= 1500) {
            return new String[]{"初中水平", "掌握了初中核心词汇，从词库选一本词书开始进阶吧"};
        }
        return new String[]{"词汇萌新", "千里之行始于足下，从今天的第一组单词开始吧"};
    }
}
