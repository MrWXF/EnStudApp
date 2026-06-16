package com.enstud.read.service;

import com.enstud.read.dto.ArticleDTO;
import com.enstud.read.dto.ArticleDetailDTO;
import com.enstud.read.dto.SourceDTO;
import com.enstud.read.dto.*;

import java.util.List;

public interface ReadService {

    /**
     * 获取热门文章列表（按热度排序）
     */
    List<ArticleDTO> getHotArticles(Long userId, String source);

    /**
     * 查看文章详情
     */
    ArticleDetailDTO getArticleDetail(Long userId, Long articleId);

    /**
     * 获取文章中文翻译（缓存，首次触发时翻译并保存）
     */
    ArticleDetailDTO getArticleTranslation(Long articleId);

    /**
     * 收藏/取消收藏
     */
    boolean toggleBookmark(Long userId, Long articleId);

    /**
     * 获取用户收藏列表
     */
    List<ArticleDTO> getBookmarks(Long userId);

    /**
     * 获取来源列表
     */
    List<SourceDTO> getSources(Long userId);

    /**
     * 手动触发文章同步（从各来源抓取）
     */
    int syncArticles();

    /**
     * 划词查词：翻译选中文本并自动加入生词本
     */
    WordLookupResponse wordLookup(Long userId, WordLookupRequest request);
}
