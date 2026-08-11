package com.enstud.chat.service;

import com.enstud.chat.ai.AiRequest;
import com.enstud.chat.ai.AiResponse;
import com.enstud.chat.dto.MessageDTO;
import com.enstud.chat.dto.SendMessageResponse;
import com.enstud.chat.entity.ChatMessage;
import com.enstud.chat.entity.ChatSession;
import com.enstud.chat.mapper.ChatMessageMapper;
import com.enstud.chat.mapper.ChatSessionMapper;
import com.enstud.common.Result;
import com.enstud.common.SecurityContext;
import com.enstud.common.feign.WordServiceClient;
import com.enstud.chat.ai.AiChatClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 复习对话服务 — 学习闭环核心功能
 * <p>
 * 提供基于用户学习单词的 AI 复习对话和单词练习对话。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewSessionService {

    private final WordServiceClient wordServiceClient;
    private final AiChatClient aiChatClient;
    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;

    /**
     * 创建复习对话会话 — AI 围绕用户待复习的单词生成对话场景
     *
     * @return 包含初始 AI 消息的会话信息
     */
    @Transactional(rollbackFor = Exception.class)
    public SendMessageResponse startReviewSession() {
        Long userId = SecurityContext.getCurrentUserId();

        // 获取待复习单词
        List<String> reviewWords = fetchReviewWords(userId);
        if (reviewWords.isEmpty()) {
            reviewWords = fetchLearningWords(userId);
        }

        if (reviewWords.isEmpty()) {
            // 没有待复习/学习单词，创建普通对话
            return createReviewSessionWithWords(userId, List.of(), "让我们继续英语对话练习吧！");
        }

        // 构建复习场景提示
        String wordList = String.join(", ", reviewWords);
        String systemHint = "请围绕以下单词设计一段日常对话场景，引导用户自然地使用这些单词。"
                + "每次只引导1-2个单词，用提问或情景描述的方式。单词列表：" + wordList;

        return createReviewSessionWithWords(userId, reviewWords, systemHint);
    }

    /**
     * 单词练习对话 — AI 围绕指定单词生成练习对话
     *
     * @param wordId 单词ID
     * @return 包含初始 AI 消息的会话信息
     */
    @Transactional(rollbackFor = Exception.class)
    public SendMessageResponse startWordPractice(Long wordId) {
        Long userId = SecurityContext.getCurrentUserId();

        // 获取单词信息
        String wordInfo = fetchWordInfo(wordId);
        if (wordInfo == null) {
            wordInfo = "未知单词";
        }

        String practiceHint = "请围绕英语单词 \"" + wordInfo + "\" 设计一段练习对话：\n"
                + "1. 先用一个简单的例句展示这个单词的用法\n"
                + "2. 然后引导用户用这个单词造句或回答问题\n"
                + "3. 如果用户回答正确，给予鼓励并解释更多用法\n"
                + "4. 保持对话自然流畅，像朋友聊天一样";

        return createReviewSessionWithWords(userId, List.of(wordInfo), practiceHint);
    }

    private SendMessageResponse createReviewSessionWithWords(Long userId, List<String> words, String initialHint) {
        // 创建会话
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle("📖 复习对话" + (words.isEmpty() ? "" : " — " + String.join(", ", words.subList(0, Math.min(3, words.size())))));
        session.setScenario("REVIEW");
        session.setMessageCount(0);
        sessionMapper.insert(session);

        // 调用 AI 生成初始消息
        AiRequest aiRequest = new AiRequest("REVIEW", List.of(), initialHint);
        AiResponse aiResponse = aiChatClient.chat(aiRequest);

        // 存储 AI 消息
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(session.getId());
        aiMsg.setRole("AI");
        aiMsg.setContent(aiResponse.getReply());
        messageMapper.insert(aiMsg);

        session.setMessageCount(1);
        sessionMapper.updateById(session);

        MessageDTO aiDTO = new MessageDTO(aiMsg.getId(), "AI", aiResponse.getReply(), null, aiMsg.getCreatedAt());

        return new SendMessageResponse(null, aiDTO, null);
    }

    private List<String> fetchReviewWords(Long userId) {
        try {
            Result<List<Map<String, Object>>> result = wordServiceClient.getReviewWords(userId);
            if (result != null && result.getData() != null) {
                return result.getData().stream()
                        .map(m -> String.valueOf(m.get("word")))
                        .filter(w -> w != null && !w.isEmpty() && !"null".equals(w))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch review words", e);
        }
        return List.of();
    }

    private List<String> fetchLearningWords(Long userId) {
        try {
            Result<List<Map<String, Object>>> result = wordServiceClient.getLearningWords(userId, 5);
            if (result != null && result.getData() != null) {
                return result.getData().stream()
                        .map(m -> String.valueOf(m.get("word")))
                        .filter(w -> w != null && !w.isEmpty() && !"null".equals(w))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch learning words", e);
        }
        return List.of();
    }

    private String fetchWordInfo(Long wordId) {
        try {
            Result<List<Map<String, Object>>> result = wordServiceClient.getLearningWords(
                    SecurityContext.getCurrentUserId(), 100);
            if (result != null && result.getData() != null) {
                return result.getData().stream()
                        .filter(m -> wordId.equals(((Number) m.get("wordId")).longValue()))
                        .map(m -> m.get("word") + " (" + m.get("definitionCn") + ")")
                        .findFirst().orElse(null);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch word info for wordId={}", wordId, e);
        }
        return null;
    }
}
