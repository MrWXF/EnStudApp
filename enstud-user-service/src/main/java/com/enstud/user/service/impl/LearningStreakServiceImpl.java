package com.enstud.user.service.impl;

import com.enstud.common.entity.UserProfile;
import com.enstud.user.mapper.UserProfileMapper;
import com.enstud.user.service.LearningStreakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 学习连续天数服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningStreakServiceImpl implements LearningStreakService {

    private final UserProfileMapper userProfileMapper;

    @Override
    public int getStreak(Long userId) {
        UserProfile profile = userProfileMapper.selectById(userId);
        if (profile == null || profile.getLastStudyDate() == null) {
            return 0;
        }
        return profile.getCurrentStreak() != null ? profile.getCurrentStreak() : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStreak(Long userId) {
        // 获取或创建学习档案
        UserProfile profile = userProfileMapper.selectById(userId);
        if (profile == null) {
            // 首次创建学习档案
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setCurrentStreak(0);
            profile.setLongestStreak(0);
            profile.setLastStudyDate(null);
            userProfileMapper.insert(profile);
            log.info("为用户创建学习档案, userId={}", userId);
        }

        LocalDate today = LocalDate.now();
        LocalDate lastStudyDate = profile.getLastStudyDate();
        int currentStreak = profile.getCurrentStreak() != null ? profile.getCurrentStreak() : 0;

        // 如果今天已更新 streak，直接返回当前值
        if (today.equals(lastStudyDate)) {
            log.debug("用户今天已更新连续天数, userId={}, currentStreak={}", userId, currentStreak);
            return currentStreak;
        }

        int newStreak;
        // 如果昨天更新了，正常递增
        if (lastStudyDate != null && today.minusDays(1).equals(lastStudyDate)) {
            newStreak = currentStreak + 1;
        } else {
            // 如果昨天之前更新的或从未学习，重置为 1
            newStreak = 1;
        }

        // 更新 current_streak 和 last_study_date
        userProfileMapper.updateStreak(userId, newStreak, today);

        // 更新 longest_streak（如果当前 streak 超过了历史最长）
        int longestStreak = profile.getLongestStreak() != null ? profile.getLongestStreak() : 0;
        if (newStreak > longestStreak) {
            userProfileMapper.updateLongestStreak(userId, newStreak);
        }

        log.info("用户连续天数更新, userId={}, lastStudyDate={}, today={}, oldStreak={}, newStreak={}",
                userId, lastStudyDate, today, currentStreak, newStreak);

        return newStreak;
    }
}
