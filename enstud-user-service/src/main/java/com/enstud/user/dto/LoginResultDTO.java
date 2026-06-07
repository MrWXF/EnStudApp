package com.enstud.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录结果
 */
@Data
@AllArgsConstructor
public class LoginResultDTO {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private String nickname;
}
