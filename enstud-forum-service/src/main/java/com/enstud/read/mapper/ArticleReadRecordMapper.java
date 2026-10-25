package com.enstud.read.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enstud.read.entity.ArticleReadRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ArticleReadRecordMapper extends BaseMapper<ArticleReadRecord> {

    /**
     * 查询用户的阅读记录（含文章信息）
     */
    @Select("SELECT ar.* FROM enstud_article_read ar " +
            "INNER JOIN enstud_article a ON ar.article_id = a.id " +
            "WHERE ar.user_id = #{userId} AND a.is_deleted = 0 " +
            "ORDER BY ar.updated_at DESC")
    List<ArticleReadRecord> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的收藏列表
     */
    @Select("SELECT ar.* FROM enstud_article_read ar " +
            "INNER JOIN enstud_article a ON ar.article_id = a.id " +
            "WHERE ar.user_id = #{userId} AND ar.is_bookmarked = 1 AND a.is_deleted = 0 " +
            "ORDER BY ar.updated_at DESC")
    List<ArticleReadRecord> selectBookmarksByUserId(@Param("userId") Long userId);

    /**
     * 查某篇文章的阅读记录
     */
    @Select("SELECT * FROM enstud_article_read " +
            "WHERE user_id = #{userId} AND article_id = #{articleId} LIMIT 1")
    ArticleReadRecord selectByUserAndArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);

    /**
     * 增加阅读计数
     */
    @Update("UPDATE enstud_article_read SET read_count = read_count + 1, updated_at = NOW() " +
            "WHERE id = #{id}")
    void incrementReadCount(@Param("id") Long id);
}
