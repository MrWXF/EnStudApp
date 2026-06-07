package com.enstud.word.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("enstud_wordbook")
public class Wordbook {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String coverUrl;
    private Integer wordCount;
    private Integer difficulty;
    private String category;
    private Integer sortOrder;
    private Integer isOfficial;
    private Long creatorId;
    @TableLogic
    private Integer isDeleted;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
