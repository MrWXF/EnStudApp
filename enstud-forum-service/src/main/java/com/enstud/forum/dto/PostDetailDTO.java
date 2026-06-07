package com.enstud.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class PostDetailDTO {
    private Long id;
    private String title;
    private String content;
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
    private List<ReplyDTO> replies;
}
