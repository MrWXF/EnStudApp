package com.enstud.read.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 热门英文文章实体
 */
@Data
@TableName("enstud_article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 英文标题 */
    private String title;

    /** 中文翻译标题（缓存） */
    private String titleCn;

    /** 原文链接 */
    private String url;

    /** 文章来源（HN / GitHub / Medium / InfoQ / TechCrunch） */
    private String source;

    /** 英文摘要 */
    private String summary;

    /** 中文翻译摘要（缓存） */
    private String summaryCn;

    /** 完整英文原文（抓取或通过摘要截取） */
    private String content;

    /** 中文翻译完整内容（懒加载缓存） */
    private String contentCn;

    /** 原始热度分 */
    private Integer sourceScore;

    /** 计算后热度分（带时间衰减） */
    private Integer score;

    /** 原文发布时间 */
    private LocalDateTime publishedAt;

    /** 抓取时间 */
    private LocalDateTime fetchedAt;

    /** 封面图 URL */
    private String coverUrl;

    /** 文章来源站点图标 */
    private String sourceIcon;

    /** 作者 */
    private String author;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
