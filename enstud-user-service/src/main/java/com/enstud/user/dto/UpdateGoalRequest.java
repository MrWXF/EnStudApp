package com.enstud.user.dto;

public record UpdateGoalRequest(
        Integer dailyWordGoal,
        Integer dailyMinuteGoal
) {}
