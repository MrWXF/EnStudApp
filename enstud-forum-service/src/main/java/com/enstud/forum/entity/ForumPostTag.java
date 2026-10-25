package com.enstud.forum.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子-标签关联（联合主键 post_id + tag_id，无自增 id）
 */
@Data
@TableName("enstud_forum_post_tag")
public class ForumPostTag {
    private Long postId;
    private Long tagId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
