package com.enstud.word.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("enstud_user_word_record")
public class UserWordRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long wordId;

    private Double easeFactor;

    @TableField("`interval`")
    private Integer reviewInterval;

    private Integer repetitions;

    private Integer quality;

    private Integer wrongCount;

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