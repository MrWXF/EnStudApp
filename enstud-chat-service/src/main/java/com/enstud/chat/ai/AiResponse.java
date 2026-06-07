package com.enstud.chat.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class AiResponse {
    private String reply;
    private List<GrammarIssue> grammarIssues;

    @Data
    @AllArgsConstructor
    public static class GrammarIssue {
        private String error;
        private String correction;
        private String explanation;
    }
}
