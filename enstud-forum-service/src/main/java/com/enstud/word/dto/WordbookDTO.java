package com.enstud.word.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 词库信息
 */
@Data
@AllArgsConstructor
public class WordbookDTO {
    private Long id;
    private String name;
    private String description;
    private String coverUrl;
    private Integer wordCount;
    private Integer difficulty;
    private String category;
    private Boolean isOfficial;
}
