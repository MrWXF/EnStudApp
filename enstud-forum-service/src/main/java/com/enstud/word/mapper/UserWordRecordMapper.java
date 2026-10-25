package com.enstud.word.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enstud.word.entity.UserWordRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserWordRecordMapper extends BaseMapper<UserWordRecord> {

    /** 查询用户今日需复习的单词（排除已精通的） */
    @Select("SELECT * FROM enstud_user_word_record " +
            "WHERE user_id = #{userId} " +
            "AND next_review_time <= NOW() " +
            "AND (memory_level IS NULL OR memory_level != 5) " +
            "ORDER BY next_review_time ASC LIMIT #{limit}")
    List<UserWordRecord> findDueForReview(@Param("userId") Long userId, @Param("limit") int limit);

    /** 统计用户已学单词总数 */
    @Select("SELECT COALESCE(COUNT(*), 0) FROM enstud_user_word_record WHERE user_id = #{userId}")
    int countTotal(@Param("userId") Long userId);

    /** 按记忆等级统计数量 */
    @Select("SELECT COALESCE(COUNT(*), 0) FROM enstud_user_word_record " +
            "WHERE user_id = #{userId} AND (memory_level IS NULL OR memory_level = #{level})")
    int countByMemoryLevel(@Param("userId") Long userId, @Param("level") int level);
}
