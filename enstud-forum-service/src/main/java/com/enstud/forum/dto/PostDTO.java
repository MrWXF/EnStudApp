package com.enstud.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PostDTO {
    private Long id;
    private String title;
    private String summary;
    private Long authorId;
    private String authorName;
    private Long categoryId;
    private String categoryName;
    private String tags;
    private Integer viewCount;
    private Integer likeCount;
    private Integer replyCount;
    private Boolean isPinned;
    private Boolean isEssence;
    private LocalDateTime createdAt;
}
