package com.enstud.word.dto;

import java.time.LocalDateTime;

public record WrongWordDTO(
        Long recordId,
        Long wordId,
        String word,
        String translation,
        Integer wrongCount,
        Integer totalAttempts,
        LocalDateTime lastWrongTime
) {}
