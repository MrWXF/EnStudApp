package com.enstud.read.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enstud.read.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 按热度排序查询可用的文章（非逻辑删除）
     */
    @Select("SELECT * FROM enstud_article " +
            "WHERE is_deleted = 0 " +
            "ORDER BY score DESC, published_at DESC")
    List<Article> selectHotArticles();

    /**
     * 按来源查询文章
     */
    @Select("SELECT * FROM enstud_article " +
            "WHERE source = #{source} AND is_deleted = 0 " +
            "ORDER BY score DESC, published_at DESC")
    List<Article> selectBySource(@Param("source") String source);

    /**
     * 统计各来源活跃文章数
     */
    @Select("SELECT source, COUNT(*) as cnt FROM enstud_article " +
            "WHERE is_deleted = 0 GROUP BY source ORDER BY cnt DESC")
    List<java.util.Map<String, Object>> countBySource();
}
