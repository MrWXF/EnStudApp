package com.enstud.read.ai;

import com.enstud.read.entity.Article;
import com.enstud.read.dto.ArticleDTO;

import java.util.List;

/**
 * 文章抓取器接口
 * 每种数据来源实现一个子类
 */
public interface ArticleFetcher {

    /**
     * 获取来源标识
     */
    String sourceId();

    /**
     * 来源名称
     */
    String sourceName();

    /**
     * 来源图标 URL（可选）
     */
    default String sourceIcon() {
        return null;
    }

    /**
     * 抓取热门文章列表
     */
    List<Article> fetch();
}
