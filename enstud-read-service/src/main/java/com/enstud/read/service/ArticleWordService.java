package com.enstud.read.service;

/**
 * 文章-单词关联服务（学习闭环）
 */
public interface ArticleWordService {

    /**
     * 提取文章内容中的单词并与词库匹配，建立关联
     *
     * @param articleId 文章ID
     * @param content   文章英文内容
     * @return 匹配到的单词数量
     */
    int extractAndLinkWords(Long articleId, String content);

    /**
     * 批量处理未关联单词的文章
     */
    void processPendingArticles();
}
