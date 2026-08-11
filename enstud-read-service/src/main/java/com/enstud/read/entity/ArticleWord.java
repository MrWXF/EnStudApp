package com.enstud.read.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章-单词关联实体（学习闭环）
 */
@Data
@TableName("enstud_article_word")
public class ArticleWord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文章ID */
    private Long articleId;

    /** 单词ID */
    private Long wordId;

    /** 单词文本（冗余，便于查询） */
    private String wordText;

    /** 在文章中出现的次数 */
    private Integer occurrenceCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
