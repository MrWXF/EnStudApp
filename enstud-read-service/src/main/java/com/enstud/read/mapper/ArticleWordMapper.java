package com.enstud.read.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enstud.read.entity.ArticleWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleWordMapper extends BaseMapper<ArticleWord> {

    /**
     * 查找包含指定单词列表的文章ID（按匹配数量降序）
     */
    @Select("<script>" +
            "SELECT article_id, COUNT(*) as match_count " +
            "FROM enstud_article_word " +
            "WHERE word_text IN " +
            "<foreach collection='words' item='w' open='(' separator=',' close=')'>" +
            "#{w}" +
            "</foreach>" +
            " GROUP BY article_id ORDER BY match_count DESC LIMIT #{limit}" +
            "</script>")
    List<ArticleWord> findArticlesByWords(@Param("words") List<String> words, @Param("limit") int limit);
}
