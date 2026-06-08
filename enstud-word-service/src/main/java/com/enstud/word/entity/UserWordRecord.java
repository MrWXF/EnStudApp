package com.enstud.word.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户单词学习记录（SM-2 算法数据）
 */
@Data
@TableName("enstud_user_word_record")
public class UserWordRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long wordId;
    private Double easeFactor;
    private Integer reviewInterval;
    private Integer repetitions;
    private Integer quality;
    private Integer masteryLevel;
    private String status;
    private Integer memoryLevel;
    private LocalDateTime nextReviewTime;
    private LocalDateTime lastReviewTime;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
