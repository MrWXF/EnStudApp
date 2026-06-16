package com.enstud.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enstud.common.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

/**
 * 用户学习档案 Mapper
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {

    /**
     * 更新连续学习天数（每天首次学习时调用）
     *
     * @param userId      用户ID
     * @param newStreak   新的连续天数
     * @param today       今天的日期
     */
    @Update("UPDATE enstud_user_profile SET current_streak = #{newStreak}, last_study_date = #{today}, updated_at = NOW() WHERE user_id = #{userId}")
    void updateStreak(Long userId, Integer newStreak, LocalDate today);

    /**
     * 更新最长连续天数
     */
    @Update("UPDATE enstud_user_profile SET longest_streak = #{longestStreak} WHERE user_id = #{userId} AND longest_streak < #{longestStreak}")
    void updateLongestStreak(Long userId, Integer longestStreak);
}
