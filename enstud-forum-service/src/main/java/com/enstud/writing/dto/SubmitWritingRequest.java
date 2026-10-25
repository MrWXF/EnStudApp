package com.enstud.writing.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmitWritingRequest(
        @NotBlank String title,
        @NotBlank String content,
        String topicType
) {}
