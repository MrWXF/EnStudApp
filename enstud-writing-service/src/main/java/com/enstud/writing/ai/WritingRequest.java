package com.enstud.writing.ai;

public class WritingRequest {
    private String title;
    private String content;
    private String topicType;

    public WritingRequest(String title, String content, String topicType) {
        this.title = title; this.content = content; this.topicType = topicType;
    }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getTopicType() { return topicType; }
}
