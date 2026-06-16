package com.enstud.read.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "划词查词响应")
public class WordLookupResponse {

    @Schema(description = "原文单词", example = "abandon")
    private String originalWord;

    @Schema(description = "单词数量（选中内容拆分为多少个单词）", example = "1")
    private int wordCount;

    @Schema(description = "翻译结果（中文释义）", example = "放弃，抛弃")
    private String translation;

    @Schema(description = "音标（如果有）", example = "/əˈbændən/")
    private String phonetic;

    @Schema(description = "词性", example = "v")
    private String partOfSpeech;

    @Schema(description = "是否已加入生词本", example = "true")
    private boolean addedToWordbook;

    @Schema(description = "生词记录ID（未加入时为null）", example = "123")
    private Long wordRecordId;

    public String getOriginalWord() { return originalWord; }
    public void setOriginalWord(String originalWord) { this.originalWord = originalWord; }
    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }
    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }
    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }
    public String getPartOfSpeech() { return partOfSpeech; }
    public void setPartOfSpeech(String partOfSpeech) { this.partOfSpeech = partOfSpeech; }
    public boolean isAddedToWordbook() { return addedToWordbook; }
    public void setAddedToWordbook(boolean addedToWordbook) { this.addedToWordbook = addedToWordbook; }
    public Long getWordRecordId() { return wordRecordId; }
    public void setWordRecordId(Long wordRecordId) { this.wordRecordId = wordRecordId; }
}
