package com.enstud.word.dto;

import java.util.List;

public record ReviewBatchRequest(
        List<Long> wordIds
) {}
