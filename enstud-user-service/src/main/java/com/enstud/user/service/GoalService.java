package com.enstud.user.service;

import com.enstud.user.dto.*;

/**
 * 学习目标服务接口
 */
public interface GoalService {

    /**
     * 获取用户学习目标
     */
    GoalDTO getGoal(Long userId);

    /**
     * 更新用户学习目标
     */
    void updateGoal(Long userId, UpdateGoalRequest request);

    /**
     * 获取今日学习进度
     */
    DailyStudyStatusDTO getDailyStatus(Long userId);

    /**
     * 打卡（混合模式：自动检测 + 手动奖励）
     */
    CheckinResponseDTO checkin(Long userId);
}
