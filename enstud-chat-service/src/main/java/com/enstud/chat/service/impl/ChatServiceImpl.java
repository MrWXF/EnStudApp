package com.enstud.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.chat.ai.AiChatClient;
import com.enstud.chat.ai.AiRequest;
import com.enstud.chat.ai.AiResponse;
import com.enstud.chat.dto.*;
import com.enstud.chat.entity.ChatMessage;
import com.enstud.chat.entity.ChatSession;
import com.enstud.chat.mapper.ChatMessageMapper;
import com.enstud.chat.mapper.ChatSessionMapper;
import com.enstud.chat.service.ChatService;
import com.enstud.common.BusinessException;
import com.enstud.common.constant.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final AiChatClient aiChatClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SessionDTO startSession(Long userId, String scenario) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setScenario(scenario != null ? scenario : "FREE");
        session.setTitle("New Chat " + System.currentTimeMillis() % 10000);
        sessionMapper.insert(session);
        log.info("会话创建, sessionId={}, userId={}", session.getId(), userId);
        return toSessionDTO(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SendMessageResponse sendMessage(Long userId, Long sessionId, String content) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND);
        }

        // 构建对话历史
        List<ChatMessage> history = messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getCreatedAt));

        List<AiRequest.Message> aiHistory = history.stream()
                .map(m -> new AiRequest.Message(m.getRole(), m.getContent()))
                .toList();

        // 存储用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("USER");
        userMsg.setContent(content);
        messageMapper.insert(userMsg);

        // 调用 AI
        AiRequest aiRequest = new AiRequest(session.getScenario(), aiHistory, content);
        AiResponse aiResponse = aiChatClient.chat(aiRequest);

        // 存储 AI 回复
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole("AI");
        aiMsg.setContent(aiResponse.getReply());
        if (aiResponse.getGrammarIssues() != null && !aiResponse.getGrammarIssues().isEmpty()) {
            aiMsg.setGrammarIssues(toJson(aiResponse.getGrammarIssues()));
        }
        messageMapper.insert(aiMsg);

        // 更新会话统计和标题
        session.setMessageCount(session.getMessageCount() + 2);
        if (session.getMessageCount() <= 2) {
            session.setTitle(content.length() > 30 ? content.substring(0, 30) : content);
        }
        sessionMapper.updateById(session);

        // 组装返回
        MessageDTO userDTO = new MessageDTO(userMsg.getId(), "USER", content,
                null, userMsg.getCreatedAt());
        List<GrammarIssueDTO> issueDTOs = convertIssues(aiResponse.getGrammarIssues());
        MessageDTO aiDTO = new MessageDTO(aiMsg.getId(), "AI", aiResponse.getReply(),
                issueDTOs, aiMsg.getCreatedAt());

        return new SendMessageResponse(userDTO, aiDTO, issueDTOs);
    }

    @Override
    public List<SessionDTO> getSessions(Long userId) {
        return sessionMapper.selectList(
                        new LambdaQueryWrapper<ChatSession>()
                                .eq(ChatSession::getUserId, userId)
                                .orderByDesc(ChatSession::getUpdatedAt))
                .stream().map(this::toSessionDTO).toList();
    }

    @Override
    public List<MessageDTO> getMessages(Long sessionId) {
        return messageMapper.selectList(
                        new LambdaQueryWrapper<ChatMessage>()
                                .eq(ChatMessage::getSessionId, sessionId)
                                .orderByAsc(ChatMessage::getCreatedAt))
                .stream().map(m -> new MessageDTO(m.getId(), m.getRole(), m.getContent(),
                        parseIssues(m.getGrammarIssues()), m.getCreatedAt()))
                .toList();
    }

    @Override
    public void deleteSession(Long userId, Long sessionId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND);
        }
        sessionMapper.deleteById(sessionId);
        log.info("会话已删除, sessionId={}, userId={}", sessionId, userId);
    }

    private SessionDTO toSessionDTO(ChatSession s) {
        return new SessionDTO(s.getId(), s.getTitle(), s.getScenario(),
                s.getMessageCount(), s.getCreatedAt());
    }

    @SneakyThrows
    private String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private List<GrammarIssueDTO> parseIssues(String json) {
        if (json == null) return null;
        List<AiResponse.GrammarIssue> raw = objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, AiResponse.GrammarIssue.class));
        return convertIssues(raw);
    }

    private List<GrammarIssueDTO> convertIssues(List<AiResponse.GrammarIssue> raw) {
        if (raw == null) return null;
        return raw.stream()
                .map(i -> new GrammarIssueDTO(i.getError(), i.getCorrection(), i.getExplanation()))
                .toList();
    }
}
