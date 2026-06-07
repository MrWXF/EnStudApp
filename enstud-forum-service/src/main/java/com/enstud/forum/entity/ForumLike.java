package com.enstud.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("enstud_forum_like")
public class ForumLike {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String targetType;
    private Long targetId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
