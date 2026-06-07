package com.enstud.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GrammarIssueDTO {
    private String error;
    private String correction;
    private String explanation;
}
