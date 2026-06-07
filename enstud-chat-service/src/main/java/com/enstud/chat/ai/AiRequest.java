package com.enstud.chat.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class AiRequest {
    private String scenario;
    private List<Message> history;
    private String currentMessage;

    @Data
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
