package com.enstud.chat.service;

import com.enstud.chat.dto.MessageDTO;
import com.enstud.chat.dto.SendMessageResponse;
import com.enstud.chat.dto.SessionDTO;

import java.util.List;

public interface ChatService {
    /** 开始新会话 */
    SessionDTO startSession(Long userId, String scenario);

    /** 发送消息 */
    SendMessageResponse sendMessage(Long userId, Long sessionId, String content);

    /** 获取会话列表 */
    List<SessionDTO> getSessions(Long userId);

    /** 获取会话消息历史 */
    List<MessageDTO> getMessages(Long sessionId);

    /** 删除会话 */
    void deleteSession(Long userId, Long sessionId);
}
