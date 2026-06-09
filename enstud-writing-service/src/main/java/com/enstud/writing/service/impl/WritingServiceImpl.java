package com.enstud.writing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.common.BusinessException;
import com.enstud.writing.ai.WritingAiClient;
import com.enstud.writing.ai.WritingCorrection;
import com.enstud.writing.ai.WritingRequest;
import com.enstud.writing.dto.*;
import com.enstud.writing.entity.Writing;
import com.enstud.writing.mapper.WritingMapper;
import com.enstud.writing.service.WritingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WritingServiceImpl implements WritingService {

    private final WritingMapper writingMapper;
    private final WritingAiClient aiClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CorrectionDTO submitAndCorrect(Long userId, SubmitWritingRequest request) {
        // 保存作文
        Writing writing = new Writing();
        writing.setUserId(userId);
        writing.setTitle(request.title());
        writing.setContent(request.content());
        writing.setWordCount(request.content().split("\\s+").length);
        writing.setTopicType(request.topicType() != null ? request.topicType() : "ESSAY");
        writingMapper.insert(writing);

        // AI 批改
        WritingRequest aiReq = new WritingRequest(request.title(), request.content(), request.topicType());
        WritingCorrection result = aiClient.correct(aiReq);

        // 保存批改结果
        writing.setScore(result.getScore());
        writing.setCorrection(toJson(result));
        writingMapper.updateById(writing);

        log.info("作文批改完成, writingId={}, userId={}, score={}", writing.getId(), userId, result.getScore());

        return buildCorrectionDTO(writing.getId(), result);
    }

    @Override
    public CorrectionDTO getCorrection(Long writingId) {
        Writing writing = writingMapper.selectById(writingId);
        if (writing == null) throw new BusinessException(4001, "作文不存在");
        if (writing.getCorrection() == null) throw new BusinessException(4002, "批改结果不存在");

        WritingCorrection result = parseCorrection(writing.getCorrection());
        return buildCorrectionDTO(writingId, result);
    }

    @Override
    public List<WritingDTO> getHistory(Long userId) {
        return writingMapper.selectList(
                        new LambdaQueryWrapper<Writing>()
                                .eq(Writing::getUserId, userId)
                                .orderByDesc(Writing::getCreatedAt))
                .stream().map(w -> new WritingDTO(w.getId(), w.getTitle(), w.getContent(),
                        w.getWordCount(), w.getTopicType(), w.getScore(), w.getCreatedAt()))
                .toList();
    }

    @Override
    public List<ModelEssayDTO> getModelEssays(String topicType) {
        // 模拟范文数据，生产环境从数据库读取
        return List.of(
                new ModelEssayDTO("The Importance of Reading",
                        "Reading is one of the most beneficial habits a person can develop. It expands vocabulary, improves writing skills, and broadens our understanding of the world...",
                        "ESSAY",
                        "结构清晰：开头引出主题，中间分段论述，结尾总结。使用了丰富的连接词和学术词汇。"),
                new ModelEssayDTO("My Favorite Season",
                        "Among the four seasons, autumn holds a special place in my heart. The cool breeze and golden leaves create a perfect atmosphere for reflection and creativity...",
                        "ESSAY",
                        "善用感官描写，感情真挚。恰当地使用了比喻和形容词，使文章生动有趣。")
        );
    }

    @Override
    public void deleteWriting(Long userId, Long writingId) {
        Writing writing = writingMapper.selectById(writingId);
        if (writing == null || !writing.getUserId().equals(userId)) {
            throw new BusinessException(4001, "作文不存在");
        }
        writingMapper.deleteById(writingId);
    }

    private CorrectionDTO buildCorrectionDTO(Long writingId, WritingCorrection result) {
        List<CorrectionItemDTO> items = result.getItems().stream()
                .map(i -> new CorrectionItemDTO(i.getType(), i.getOriginal(), i.getSuggestion(), i.getExplanation()))
                .toList();
        return new CorrectionDTO(writingId, result.getScore(), result.getOverallComment(), items);
    }

    @SneakyThrows
    private String toJson(Object obj) { return objectMapper.writeValueAsString(obj); }

    @SneakyThrows
    private WritingCorrection parseCorrection(String json) {
        return objectMapper.readValue(json, WritingCorrection.class);
    }
}
