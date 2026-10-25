package com.enstud.word.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 单词卡片（学习用）
 */
@Data
@AllArgsConstructor
public class WordCardDTO {
    private Long id;
    private String word;
    private String phoneticUk;
    private String phoneticUs;
    private String definitionCn;
    private String definitionEn;
    private String exampleSentence;
    private String partOfSpeech;
    private Integer masteryLevel;   // 当前掌握程度 0-100，未学习为 null
    private String status;           // 学习状态 LEARNING/REVIEWING/MASTERED
}
