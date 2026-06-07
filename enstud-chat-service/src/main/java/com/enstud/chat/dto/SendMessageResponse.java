package com.enstud.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class SendMessageResponse {
    private MessageDTO userMessage;
    private MessageDTO aiMessage;
    private List<GrammarIssueDTO> grammarIssues;
}
