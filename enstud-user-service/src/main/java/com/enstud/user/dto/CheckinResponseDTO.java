package com.enstud.user.dto;

public record CheckinResponseDTO(
        Integer streakDays,
        Integer bonusPoints,
        String message
) {}
