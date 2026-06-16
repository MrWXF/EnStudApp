package com.enstud.user.service;

/**
 * 学习连续天数服务接口
 */
public interface LearningStreakService {

    /**
     * 获取当前连续学习天数
     *
     * @param userId 用户ID
     * @return 连续学习天数
     */
    int getStreak(Long userId);

    /**
     * 更新连续学习天数（每日首次学习/登录时调用）
     * 逻辑：
     * - 如果今天已更新，直接返回当前 streak
     * - 如果昨天更新了，正常递增（+1）
     * - 如果昨天之前更新的，重置为 1
     *
     * @param userId 用户ID
     * @return 更新后的连续学习天数
     */
    int updateStreak(Long userId);
}
