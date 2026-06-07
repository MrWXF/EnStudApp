package com.enstud.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("enstud_forum_post")
public class ForumPost {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String summary;
    private Long authorId;
    private Long categoryId;
    private String tags;
    private Integer viewCount;
    private Integer likeCount;
    private Integer replyCount;
    private Integer collectCount;
    private Integer isPinned;
    private Integer isEssence;
    private String status;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
