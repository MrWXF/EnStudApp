package com.enstud.read.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户阅读记录
 */
@Data
@TableName("enstud_article_read")
public class ArticleReadRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 文章 ID */
    private Long articleId;

    /** 阅读次数 */
    private Integer readCount;

    /** 是否收藏 */
    private Boolean isBookmarked;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
