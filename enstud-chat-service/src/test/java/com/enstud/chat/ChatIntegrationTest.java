package com.enstud.chat;

import com.enstud.common.test.DbIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.test.annotation.DirtiesContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("AI对话服务集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ChatIntegrationTest extends DbIntegrationTest {

    private static final Long USER_ID = 1L;
    private static final String USERNAME = "chatuser";
    private Long sessionId;

    @Test @Order(1) @DisplayName("开始新会话")
    void shouldStartSession() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/chat/sessions").param("scenario", "FREE")
                        .header("X-User-Id", USER_ID.toString()).header("X-Username", USERNAME))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id", notNullValue()));

        sessionId = jdbcTemplate.queryForObject("SELECT id FROM enstud_chat_session WHERE user_id=?", Long.class, USER_ID);
        Assertions.assertNotNull(sessionId);
    }

    @Test @Order(2) @DisplayName("开始会话无认证 → 401")
    void shouldRejectStartSessionWithoutAuth() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/chat/sessions").param("scenario", "FREE"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(401));
    }

    @Test @Order(3) @DisplayName("发送消息")
    void shouldSendMessage() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/chat/sessions/" + sessionId + "/messages").param("content", "Hello, how are you?")
                        .header("X-User-Id", USER_ID.toString()).header("X-Username", USERNAME))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
    }

    @Test @Order(4) @DisplayName("获取会话列表")
    void shouldGetSessions() throws Exception {
        getWithAuth("/chat/sessions", USER_ID, USERNAME)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test @Order(5) @DisplayName("获取会话消息")
    void shouldGetMessages() throws Exception {
        getWithAuth("/chat/sessions/" + sessionId + "/messages", USER_ID, USERNAME)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test @Order(6) @DisplayName("删除会话")
    void shouldDeleteSession() throws Exception {
        deleteWithAuth("/chat/sessions/" + sessionId, USER_ID, USERNAME)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
    }

    @Test @Order(7) @DisplayName("访问不存在的会话消息 → 空列表")
    void shouldGetEmptyMessagesForNonExistentSession() throws Exception {
        getWithAuth("/chat/sessions/99999/messages", USER_ID, USERNAME)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test @Order(8) @DisplayName("删除不存在的会话 → 3001")
    void shouldRejectDeleteNonExistentSession() throws Exception {
        deleteWithAuth("/chat/sessions/99999", USER_ID, USERNAME)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(3001));
    }
}
