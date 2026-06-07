package com.enstud.writing.dto;

import java.util.List;

public class CorrectionDTO {
    private Long writingId;
    private int score;
    private String overallComment;
    private List<CorrectionItemDTO> items;

    public CorrectionDTO() {}
    public CorrectionDTO(Long writingId, int score, String overallComment, List<CorrectionItemDTO> items) {
        this.writingId = writingId; this.score = score; this.overallComment = overallComment; this.items = items;
    }
    public Long getWritingId() { return writingId; }
    public int getScore() { return score; }
    public String getOverallComment() { return overallComment; }
    public List<CorrectionItemDTO> getItems() { return items; }
}
