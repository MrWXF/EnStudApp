package com.enstud.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePostRequest(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull Long categoryId,
        String tags
) {}
