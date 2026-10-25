package com.enstud.writing.dto;

public class WritingDTO {
    private Long id;
    private String title;
    private String content;
    private Integer wordCount;
    private String topicType;
    private Integer score;
    private java.time.LocalDateTime createdAt;

    public WritingDTO() {}
    public WritingDTO(Long id, String title, String content, Integer wordCount, String topicType, Integer score, java.time.LocalDateTime createdAt) {
        this.id = id; this.title = title; this.content = content; this.wordCount = wordCount;
        this.topicType = topicType; this.score = score; this.createdAt = createdAt;
    }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Integer getWordCount() { return wordCount; }
    public String getTopicType() { return topicType; }
    public Integer getScore() { return score; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
}
