package com.enstud.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SessionDTO {
    private Long id;
    private String title;
    private String scenario;
    private Integer messageCount;
    private LocalDateTime createdAt;
}
