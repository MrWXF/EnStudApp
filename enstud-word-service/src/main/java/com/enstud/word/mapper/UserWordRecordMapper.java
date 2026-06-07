package com.enstud.word.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enstud.word.entity.UserWordRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserWordRecordMapper extends BaseMapper<UserWordRecord> {

    /** 查询用户今日需复习的单词 */
    @Select("SELECT * FROM enstud_user_word_record WHERE user_id = #{userId} AND next_review_time <= NOW() AND status != 'MASTERED' ORDER BY next_review_time ASC LIMIT #{limit}")
    List<UserWordRecord> findDueForReview(@Param("userId") Long userId, @Param("limit") int limit);

    /** 统计用户学习概览 */
    @Select("SELECT COALESCE(SUM(CASE WHEN status = 'MASTERED' THEN 1 ELSE 0 END), 0) FROM enstud_user_word_record WHERE user_id = #{userId}")
    int countMastered(@Param("userId") Long userId);

    @Select("SELECT COALESCE(SUM(CASE WHEN status = 'LEARNING' THEN 1 ELSE 0 END), 0) FROM enstud_user_word_record WHERE user_id = #{userId}")
    int countLearning(@Param("userId") Long userId);

    @Select("SELECT COALESCE(COUNT(*), 0) FROM enstud_user_word_record WHERE user_id = #{userId}")
    int countTotal(@Param("userId") Long userId);
}
