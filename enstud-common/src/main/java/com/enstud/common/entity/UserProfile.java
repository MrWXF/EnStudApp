package com.enstud.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户学习档案实体
 */
@Data
@TableName("enstud_user_profile")
public class UserProfile {

    /** 用户ID（与 enstud_user 表一对一） */
    @TableId
    private Long userId;

    /** 累计学习单词数 */
    private Integer totalWordsLearned;

    /** 词汇量估算 */
    private Integer vocabularySize;

    /** 写作次数 */
    private Integer writingCount;

    /** 对话次数 */
    private Integer chatCount;

    /** 当前连续打卡天数 */
    private Integer currentStreak;

    /** 最长连续打卡天数 */
    private Integer longestStreak;

    /** 最后学习日期 */
    private LocalDate lastStudyDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
