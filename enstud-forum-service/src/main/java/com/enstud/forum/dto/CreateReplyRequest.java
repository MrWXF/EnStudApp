package com.enstud.forum.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateReplyRequest(
        @NotBlank String content
) {}
