package com.enstud.user.dto;

public record GoalDTO(
        Long userId,
        Integer dailyWordGoal,
        Integer dailyMinuteGoal
) {}
