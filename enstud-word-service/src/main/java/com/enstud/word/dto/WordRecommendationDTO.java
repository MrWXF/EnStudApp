package com.enstud.word.dto;

public record WordRecommendationDTO(
        Long wordId,
        String word,
        String translation,
        Integer difficulty,
        String reason,
        String category
) {}
