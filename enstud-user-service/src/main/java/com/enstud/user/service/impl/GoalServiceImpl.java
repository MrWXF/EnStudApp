package com.enstud.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.common.BusinessException;
import com.enstud.common.constant.ErrorCode;
import com.enstud.common.entity.DailyStudyRecord;
import com.enstud.common.entity.User;
import com.enstud.common.entity.UserGoal;
import com.enstud.common.entity.UserProfile;
import com.enstud.user.dto.*;
import com.enstud.user.mapper.DailyStudyRecordMapper;
import com.enstud.user.mapper.UserGoalMapper;
import com.enstud.user.mapper.UserMapper;
import com.enstud.user.mapper.UserProfileMapper;
import com.enstud.user.service.GoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final UserGoalMapper goalMapper;
    private final DailyStudyRecordMapper recordMapper;
    private final UserMapper userMapper;
    private final UserProfileMapper profileMapper;

    @Override
    public GoalDTO getGoal(Long userId) {
        UserGoal goal = goalMapper.selectById(userId);
        if (goal == null) {
            // 返回默认目标
            return new GoalDTO(userId, 20, 30);
        }
        return new GoalDTO(goal.getUserId(), goal.getDailyWordGoal(), goal.getDailyMinuteGoal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGoal(Long userId, UpdateGoalRequest request) {
        UserGoal goal = goalMapper.selectById(userId);
        if (goal == null) {
            // 创建新目标
            goal = new UserGoal();
            goal.setUserId(userId);
            goal.setDailyWordGoal(request.dailyWordGoal() != null ? request.dailyWordGoal() : 20);
            goal.setDailyMinuteGoal(request.dailyMinuteGoal() != null ? request.dailyMinuteGoal() : 30);
            goalMapper.insert(goal);
        } else {
            // 更新目标
            if (request.dailyWordGoal() != null) {
                goal.setDailyWordGoal(request.dailyWordGoal());
            }
            if (request.dailyMinuteGoal() != null) {
                goal.setDailyMinuteGoal(request.dailyMinuteGoal());
            }
            goalMapper.updateById(goal);
        }
        log.info("更新用户学习目标: userId={}, wordGoal={}, minuteGoal={}",
                userId, request.dailyWordGoal(), request.dailyMinuteGoal());
    }

    @Override
    public DailyStudyStatusDTO getDailyStatus(Long userId) {
        LocalDate today = LocalDate.now();
        DailyStudyRecord record = recordMapper.selectOne(
                new LambdaQueryWrapper<DailyStudyRecord>()
                        .eq(DailyStudyRecord::getUserId, userId)
                        .eq(DailyStudyRecord::getStudyDate, today)
        );

        GoalDTO goal = getGoal(userId);

        if (record == null) {
            return new DailyStudyStatusDTO(0, goal.dailyWordGoal(), 0, goal.dailyMinuteGoal(), false);
        }

        return new DailyStudyStatusDTO(
                record.getWordsLearned(),
                goal.dailyWordGoal(),
                record.getMinutesStudied(),
                goal.dailyMinuteGoal(),
                record.getGoalCompleted() == 1
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckinResponseDTO checkin(Long userId) {
        LocalDate today = LocalDate.now();

        // 检查今日是否已打卡
        DailyStudyRecord record = recordMapper.selectOne(
                new LambdaQueryWrapper<DailyStudyRecord>()
                        .eq(DailyStudyRecord::getUserId, userId)
                        .eq(DailyStudyRecord::getStudyDate, today)
        );

        if (record != null && record.getGoalCompleted() == 1) {
            // 今日已完成目标，但手动打卡仍可获得积分
            UserProfile profile = profileMapper.selectById(userId);
            if (profile == null) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }

            // 检查是否已经手动打卡过（通过比较 last_study_date）
            if (today.equals(profile.getLastStudyDate())) {
                return new CheckinResponseDTO(
                        profile.getCurrentStreak(),
                        0,
                        "今日已打卡"
                );
            }
        }

        // 执行打卡逻辑
        UserProfile profile = profileMapper.selectById(userId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 更新连续打卡天数
        LocalDate lastStudyDate = profile.getLastStudyDate();
        int newStreak;
        if (lastStudyDate == null) {
            newStreak = 1;
        } else if (lastStudyDate.equals(today.minusDays(1))) {
            newStreak = profile.getCurrentStreak() + 1;
        } else if (lastStudyDate.equals(today)) {
            newStreak = profile.getCurrentStreak(); // 今天已学习过，不增加
        } else {
            newStreak = 1; // 断签，重新开始
        }

        profile.setCurrentStreak(newStreak);
        profile.setLastStudyDate(today);
        profile.setLongestStreak(Math.max(profile.getLongestStreak(), newStreak));
        profileMapper.updateById(profile);

        // 创建或更新每日记录
        if (record == null) {
            record = new DailyStudyRecord();
            record.setUserId(userId);
            record.setStudyDate(today);
            record.setWordsLearned(0);
            record.setMinutesStudied(0);
            record.setGoalCompleted(1);
            recordMapper.insert(record);
        } else {
            record.setGoalCompleted(1);
            recordMapper.updateById(record);
        }

        // 奖励 10 积分
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPoints(user.getPoints() + 10);
            userMapper.updateById(user);
        }

        log.info("用户打卡成功: userId={}, streak={}, bonusPoints=10", userId, newStreak);

        return new CheckinResponseDTO(
                newStreak,
                10,
                "打卡成功！获得 10 积分奖励"
        );
    }
}
