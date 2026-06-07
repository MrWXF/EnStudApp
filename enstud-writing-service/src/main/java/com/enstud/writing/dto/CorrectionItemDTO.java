package com.enstud.writing.dto;

public class CorrectionItemDTO {
    private String type;
    private String original;
    private String suggestion;
    private String explanation;

    public CorrectionItemDTO() {}
    public CorrectionItemDTO(String type, String original, String suggestion, String explanation) {
        this.type = type; this.original = original; this.suggestion = suggestion; this.explanation = explanation;
    }
    public String getType() { return type; }
    public String getOriginal() { return original; }
    public String getSuggestion() { return suggestion; }
    public String getExplanation() { return explanation; }
}
