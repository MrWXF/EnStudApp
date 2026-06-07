package com.enstud.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户个人信息响应
 */
@Data
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatarUrl;
    private Integer level;
    private Integer points;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
