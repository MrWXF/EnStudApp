package com.enstud.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("enstud_forum_tag")
public class ForumTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer postCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
