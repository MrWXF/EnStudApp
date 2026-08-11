package com.enstud.chat.dto;

public record ChatTopicDTO(
        String topic,
        Integer difficulty,
        String description,
        String scenario,
        String source
) {}
