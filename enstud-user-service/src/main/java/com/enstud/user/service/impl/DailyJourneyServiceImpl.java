package com.enstud.user.service.impl;

import com.enstud.common.entity.UserGoal;
import com.enstud.common.entity.UserProfile;
import com.enstud.user.client.StatsClient;
import com.enstud.user.dto.DailyJourneyDTO;
import com.enstud.user.mapper.UserGoalMapper;
import com.enstud.user.mapper.UserProfileMapper;
import com.enstud.user.service.DailyJourneyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyJourneyServiceImpl implements DailyJourneyService {

    private final UserGoalMapper goalMapper;
    private final UserProfileMapper profileMapper;
    private final StatsClient statsClient;

    @Override
    public DailyJourneyDTO getDailyJourney(Long userId) {
        LocalDate today = LocalDate.now();

        UserGoal goal = goalMapper.selectById(userId);
        int dailyWordGoal = goal != null && goal.getDailyWordGoal() != null
                ? goal.getDailyWordGoal() : 20;

        UserProfile profile = profileMapper.selectById(userId);
        int streakDays = profile != null && profile.getCurrentStreak() != null
                ? profile.getCurrentStreak() : 0;
        boolean checkedIn = profile != null && today.equals(profile.getLastStudyDate());

        Map<String, Object> stats = statsClient.getJourneyStats(userId);

        DailyJourneyDTO journey = new DailyJourneyDTO(
                today,
                streakDays,
                checkedIn,
                dailyWordGoal,
                toLong(stats.get("todayLearned")),
                toLong(stats.get("dueReviewCount")),
                toLong(stats.get("todayReadArticles")),
                toLong(stats.get("todayChats"))
        );
        log.debug("今日学习之旅: userId={}, journey={}", userId, journey);
        return journey;
    }

    private static long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }
}
