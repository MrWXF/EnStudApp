package com.enstud.word.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 词汇量测试记录
 */
@Data
@TableName("enstud_vocab_test_record")
public class VocabTestRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 估算词汇量 */
    private Integer estimatedSize;

    /** 水平标签，如"四级水平" */
    private String levelLabel;

    /** 各频段得分明细 JSON */
    private String bandStats;

    private LocalDateTime createdAt;
}
