package com.enstud.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReplyDTO {
    private Long id;
    private String content;
    private Long authorId;
    private String authorName;
    private Integer likeCount;
    private LocalDateTime createdAt;
}
