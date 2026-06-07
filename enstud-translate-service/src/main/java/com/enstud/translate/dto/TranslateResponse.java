package com.enstud.translate.dto;

public class TranslateResponse {
    private String sourceText;
    private String translatedText;
    private String from;
    private String to;

    public TranslateResponse() {}
    public TranslateResponse(String sourceText, String translatedText, String from, String to) {
        this.sourceText = sourceText; this.translatedText = translatedText; this.from = from; this.to = to;
    }
    public String getSourceText() { return sourceText; }
    public String getTranslatedText() { return translatedText; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public void setSourceText(String sourceText) { this.sourceText = sourceText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }
    public void setFrom(String from) { this.from = from; }
    public void setTo(String to) { this.to = to; }
}
