package com.enstud.writing;

import com.enstud.common.test.DbIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("写作服务集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class WritingIntegrationTest extends DbIntegrationTest {

    private static final Long USER_ID = 1L;
    private static final String USERNAME = "writer";
    private Long writingId;

    @Test @Order(1) @DisplayName("提交作文并获取批改")
    void shouldSubmitAndCorrect() throws Exception {
        Map<String, String> body = Map.of("title", "My Day", "content",
                "I go to school yesterday. It was a good day. I learn many things.",
                "topicType", "ESSAY");

        postWithAuth("/writing/submit", body, USER_ID, USERNAME)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.score", notNullValue()));

        writingId = jdbcTemplate.queryForObject("SELECT id FROM enstud_writing WHERE user_id=?", Long.class, USER_ID);
        Assertions.assertNotNull(writingId);
    }

    @Test @Order(2) @DisplayName("提交作文无认证 → 401")
    void shouldRejectWithoutAuth() throws Exception {
        Map<String, String> body = Map.of("title", "Bad", "content", "Test.", "topicType", "ESSAY");
        postWithoutAuth("/writing/submit", body)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(401));
    }

    @Test @Order(3) @DisplayName("获取批改结果")
    void shouldGetCorrection() throws Exception {
        getWithoutAuth("/writing/" + writingId + "/correction")
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
    }

    @Test @Order(4) @DisplayName("获取写作历史")
    void shouldGetHistory() throws Exception {
        getWithAuth("/writing/history", USER_ID, USERNAME)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test @Order(5) @DisplayName("获取范文")
    void shouldGetModelEssays() throws Exception {
        getWithoutAuth("/writing/models?topicType=ESSAY")
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
    }

    @Test @Order(6) @DisplayName("删除作文")
    void shouldDeleteWriting() throws Exception {
        deleteWithAuth("/writing/" + writingId, USER_ID, USERNAME)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
    }

    @Test @Order(7) @DisplayName("获取范文（不同话题）")
    void shouldGetModelEssaysByTopic() throws Exception {
        getWithoutAuth("/writing/models?topicType=LETTER")
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
    }

    @Test @Order(8) @DisplayName("提交作文缺少标题 → 400")
    void shouldRejectMissingTitle() throws Exception {
        Map<String, String> body = Map.of("content", "Test content here.");
        postWithoutAuth("/writing/submit", body)
                .andExpect(status().isBadRequest());
    }

    @Test @Order(9) @DisplayName("得分应为有效数字")
    void shouldHaveValidScore() throws Exception {
        Map<String, String> body = Map.of("title", "Score Test", "content",
                "This is a valid English essay for testing scoring.", "topicType", "ESSAY");
        postWithAuth("/writing/submit", body, USER_ID, "scorer")
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.score").isNumber());
    }
}
