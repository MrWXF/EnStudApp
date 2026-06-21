package com.enstud.word;

import com.enstud.common.test.DbIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.test.annotation.DirtiesContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("单词服务 — SM-2 学习流程")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class WordStudyFlowTest extends DbIntegrationTest {

    private static final Long TEST_USER_ID = 200L;
    private static final String TEST_USERNAME = "flowuser";

    @BeforeAll
    void setUpData() {
        jdbcTemplate.update("INSERT INTO enstud_wordbook (id, name, category, difficulty, word_count, sort_order) VALUES (1, 'Flow WB', 'CET4', 1, 5, 1)");
        jdbcTemplate.update("INSERT INTO enstud_word (id, word, definition_cn, difficulty_level, wordbook_id) VALUES (1, 'abandon', '放弃', 1, 1)");
        jdbcTemplate.update("INSERT INTO enstud_word (id, word, definition_cn, difficulty_level, wordbook_id) VALUES (3, 'curious', '好奇的', 1, 1)");
        jdbcTemplate.update("INSERT INTO enstud_word (id, word, definition_cn, difficulty_level, wordbook_id) VALUES (4, 'deliberate', '故意的', 2, 1)");
        jdbcTemplate.update("INSERT INTO enstud_word (id, word, definition_cn, difficulty_level, wordbook_id) VALUES (5, 'eloquent', '雄辩的', 2, 1)");
    }

    @AfterAll
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM enstud_user_word_record");
        jdbcTemplate.update("DELETE FROM enstud_word");
        jdbcTemplate.update("DELETE FROM enstud_wordbook");
    }

    @Test @Order(1) @DisplayName("完整学习周期：新单词 → 多轮复习 → 达到 MASTERED")
    void shouldProgressToMasteredThroughReview() throws Exception {
        for (int i = 0; i < 8; i++) submitReview(1L, 5);
        Integer mastery = queryInt("SELECT mastery_level FROM enstud_user_word_record WHERE user_id=? AND word_id=?", TEST_USER_ID, 1L);
        Assertions.assertTrue(mastery >= 90, "mastery should be >= 90: " + mastery);
    }

    @Test @Order(2) @DisplayName("easeFactor 不低于 1.3")
    void shouldNotDropEaseFactorBelowMinimum() throws Exception {
        for (int i = 0; i < 10; i++) submitReview(3L, 0);
        Double ef = jdbcTemplate.queryForObject("SELECT ease_factor FROM enstud_user_word_record WHERE user_id=? AND word_id=?", Double.class, TEST_USER_ID, 3L);
        Assertions.assertTrue(ef >= 1.3, "ease_factor should be >= 1.3: " + ef);
    }

    @Test @Order(3) @DisplayName("记忆等级分布")
    void shouldHaveCorrectDistribution() throws Exception {
        getWithAuth("/word/memory-level/distribution", TEST_USER_ID, TEST_USERNAME)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
    }

    @Test @Order(4) @DisplayName("手动调整到精通 → MASTERED")
    void shouldAdjustMemoryLevelToMastered() throws Exception {
        submitReview(4L, 3);
        String body = "{\"wordId\":4,\"targetLevel\":5}";
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/word/memory-level/adjust").contentType("application/json")
                        .header("X-User-Id", TEST_USER_ID.toString()).header("X-Username", TEST_USERNAME)
                        .content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        String status = jdbcTemplate.queryForObject("SELECT status FROM enstud_user_word_record WHERE user_id=? AND word_id=?", String.class, TEST_USER_ID, 4L);
        Assertions.assertEquals("MASTERED", status);
    }

    @Test @Order(5) @DisplayName("调整等级越界 → code=2003")
    void shouldRejectInvalidMemoryLevel() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/word/memory-level/adjust").contentType("application/json")
                        .header("X-User-Id", TEST_USER_ID.toString()).header("X-Username", TEST_USERNAME)
                        .content("{\"wordId\":5,\"targetLevel\":6}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(2003));
    }

    @Test @Order(6) @DisplayName("调整未学习单词 → code=2004")
    void shouldRejectAdjustUnlearnedWord() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/word/memory-level/adjust").contentType("application/json")
                        .header("X-User-Id", TEST_USER_ID.toString()).header("X-Username", TEST_USERNAME)
                        .content("{\"wordId\":5,\"targetLevel\":3}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(2004));
    }

    private void submitReview(Long wordId, int quality) throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/word/review").param("wordId", wordId.toString()).param("quality", String.valueOf(quality))
                        .header("X-User-Id", TEST_USER_ID.toString()).header("X-Username", TEST_USERNAME))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
    }

    private Integer queryInt(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, Integer.class, args);
    }
}
