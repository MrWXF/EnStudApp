package com.enstud.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private String role;
    private String content;
    private List<GrammarIssueDTO> grammarIssues;
    private LocalDateTime createdAt;
}
