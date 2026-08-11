package com.enstud.user.dto;

public record DailyStudyStatusDTO(
        Integer wordsLearned,
        Integer wordGoal,
        Integer minutesStudied,
        Integer minuteGoal,
        Boolean goalCompleted
) {}
