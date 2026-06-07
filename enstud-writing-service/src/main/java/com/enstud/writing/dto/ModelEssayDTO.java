package com.enstud.writing.dto;

public class ModelEssayDTO {
    private String title;
    private String content;
    private String topicType;
    private String analysis;

    public ModelEssayDTO() {}
    public ModelEssayDTO(String title, String content, String topicType, String analysis) {
        this.title = title; this.content = content; this.topicType = topicType; this.analysis = analysis;
    }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getTopicType() { return topicType; }
    public String getAnalysis() { return analysis; }
}
