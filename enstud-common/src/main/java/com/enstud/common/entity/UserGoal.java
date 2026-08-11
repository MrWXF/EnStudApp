package com.enstud.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户学习目标实体
 */
@Data
@TableName("enstud_user_goal")
public class UserGoal {

    /** 用户ID（主键） */
    @TableId
    private Long userId;

    /** 每日单词目标 */
    private Integer dailyWordGoal;

    /** 每日学习时长目标（分钟） */
    private Integer dailyMinuteGoal;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
