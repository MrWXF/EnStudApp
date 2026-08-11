package com.enstud.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("enstud_forum_post_tag")
public class ForumPostTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long tagId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
