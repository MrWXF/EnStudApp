package com.enstud.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 单词实体
 */
@Data
@TableName("enstud_word")
public class Word {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 单词 */
    private String word;

    /** 英式音标 */
    private String phoneticUk;

    /** 美式音标 */
    private String phoneticUs;

    /** 中文释义 */
    private String definitionCn;

    /** 英文释义 */
    private String definitionEn;

    /** 发音音频URL */
    private String pronunciationUrl;

    /** 例句 */
    private String exampleSentence;

    /** 例句中文翻译 */
    private String exampleCn;

    /** 词性 n/v/adj/adv 等 */
    private String partOfSpeech;

    /** 难度等级 1-5 */
    private Integer difficultyLevel;

    /** 所属词库 ID */
    private Long wordbookId;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
