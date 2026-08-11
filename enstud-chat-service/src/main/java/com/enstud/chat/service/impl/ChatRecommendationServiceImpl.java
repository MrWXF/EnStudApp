package com.enstud.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.chat.constant.PresetChatScenarios;
import com.enstud.chat.dto.ChatTopicDTO;
import com.enstud.chat.entity.ChatSession;
import com.enstud.chat.mapper.ChatSessionMapper;
import com.enstud.chat.service.ChatRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRecommendationServiceImpl implements ChatRecommendationService {

    private final ChatSessionMapper sessionMapper;

    @Override
    public List<ChatTopicDTO> recommendTopics(Long userId, int limit) {
        // 获取用户已聊过的场景
        List<ChatSession> sessions = sessionMapper.selectList(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
        );
        Set<String> usedScenarios = sessions.stream()
                .map(ChatSession::getScenario)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 筛选未聊过的预置场景
        return PresetChatScenarios.SCENARIOS.stream()
                .filter(s -> !usedScenarios.contains(s.nameEn()))
                .limit(limit)
                .map(s -> new ChatTopicDTO(
                        s.nameEn(),
                        s.difficulty(),
                        s.description(),
                        s.nameCn(),
                        "preset"
                ))
                .collect(Collectors.toList());
    }
}
