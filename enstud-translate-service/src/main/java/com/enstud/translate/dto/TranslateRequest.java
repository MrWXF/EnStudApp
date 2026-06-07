package com.enstud.translate.dto;

import jakarta.validation.constraints.NotBlank;

public record TranslateRequest(
        @NotBlank String text,
        String from,
        @NotBlank String to
) {}
