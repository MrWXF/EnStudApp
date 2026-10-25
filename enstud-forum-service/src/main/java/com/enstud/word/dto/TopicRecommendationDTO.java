package com.enstud.word.dto;

public record TopicRecommendationDTO(
        String topic,
        String category,
        Integer difficulty,
        String description,
        Integer wordCount
) {}
