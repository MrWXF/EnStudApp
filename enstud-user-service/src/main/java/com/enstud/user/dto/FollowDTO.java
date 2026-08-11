package com.enstud.user.dto;

public record FollowDTO(
        Long userId,
        String username,
        String nickname,
        String avatarUrl
) {}
