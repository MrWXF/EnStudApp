package com.enstud.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日学习记录实体
 */
@Data
@TableName("enstud_daily_study_record")
public class DailyStudyRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 学习日期 */
    private LocalDate studyDate;

    /** 当日学习单词数 */
    private Integer wordsLearned;

    /** 当日学习时长（分钟） */
    private Integer minutesStudied;

    /** 是否完成目标 0-否 1-是 */
    private Integer goalCompleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
