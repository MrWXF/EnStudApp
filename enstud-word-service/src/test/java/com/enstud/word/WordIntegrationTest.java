package com.enstud.word;

import com.enstud.common.test.DbIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("单词服务 — 词库与单词 CRUD")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class WordIntegrationTest extends DbIntegrationTest {

    private static final Long TEST_USER_ID = 100L;
    private static final String TEST_USERNAME = "worduser";

    @BeforeAll
    void setUpData() {
        jdbcTemplate.update("INSERT INTO enstud_wordbook (id, name, description, category, difficulty, word_count, sort_order, is_official) VALUES (1, 'Test Wordbook', 'Test', 'CET4', 1, 10, 1, 1)");
        jdbcTemplate.update("INSERT INTO enstud_word (id, word, definition_cn, definition_en, example_sentence, part_of_speech, difficulty_level, wordbook_id) VALUES (1, 'abandon', '放弃', 'to leave', 'They abandoned the ship.', 'v', 1, 1)");
        jdbcTemplate.update("INSERT INTO enstud_word (id, word, definition_cn, definition_en, example_sentence, part_of_speech, difficulty_level, wordbook_id) VALUES (2, 'brilliant', '杰出的', 'very bright', 'She is brilliant.', 'adj', 1, 1)");
        jdbcTemplate.update("INSERT INTO enstud_word (id, word, definition_cn, definition_en, example_sentence, part_of_speech, difficulty_level, wordbook_id) VALUES (3, 'curious', '好奇的', 'eager to know', 'The child is curious.', 'adj', 1, 1)");
    }

    @AfterAll
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM enstud_user_word_record");
        jdbcTemplate.update("DELETE FROM enstud_word");
        jdbcTemplate.update("DELETE FROM enstud_wordbook");
    }

    @Test @Order(1) @DisplayName("无 auth header → @AuthRequired 拦截返回 401")
    void shouldRejectWithoutAuth() throws Exception {
        getWithoutAuth("/word/wordbooks").andExpect(status().isUnauthorized());
    }

    @Test @Order(2) @DisplayName("获取词库列表")
    void shouldGetWordbooks() throws Exception {
        getWithAuth("/word/wordbooks", TEST_USER_ID, TEST_USERNAME)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test @Order(3) @DisplayName("获取词库下的单词（分页）")
    void shouldGetWordsByWordbook() throws Exception {
        getWithAuth("/word/wordbooks/1/words?limit=3", TEST_USER_ID, TEST_USERNAME)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", hasSize(3)));
    }

    @Test @Order(4) @DisplayName("词库不存在 → code=2002")
    void shouldRejectNonExistentWordbook() throws Exception {
        getWithAuth("/word/wordbooks/99999/words", TEST_USER_ID, TEST_USERNAME)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2002));
    }

    @Test @Order(5) @DisplayName("开始学习 → 返回待学习单词")
    void shouldStartStudy() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/word/study").param("wordbookId", "1").param("limit", "3")
                        .header("X-User-Id", TEST_USER_ID.toString())
                        .header("X-Username", TEST_USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test @Order(6) @DisplayName("提交复习 quality=5（完美）→ DB 记录更新")
    void shouldSubmitReviewPerfect() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/word/review").param("wordId", "1").param("quality", "5")
                        .header("X-User-Id", TEST_USER_ID.toString())
                        .header("X-Username", TEST_USERNAME))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));

        Integer count = queryInt("SELECT COUNT(*) FROM enstud_user_word_record WHERE user_id=? AND word_id=?", TEST_USER_ID, 1L);
        Assertions.assertEquals(1, count);
        Integer rep = queryInt("SELECT repetitions FROM enstud_user_word_record WHERE user_id=? AND word_id=?", TEST_USER_ID, 1L);
        Assertions.assertEquals(1, rep);
    }

    @Test @Order(7) @DisplayName("提交复习 quality=0 → 重置参数")
    void shouldSubmitReviewWrong() throws Exception {
        submitReview(2L, 5);
        submitReview(2L, 0);
        Integer rep = queryInt("SELECT repetitions FROM enstud_user_word_record WHERE user_id=? AND word_id=?", TEST_USER_ID, 2L);
        Assertions.assertEquals(0, rep);
    }

    @Test @Order(8) @DisplayName("提交复习 quality=6 越界 → 500")
    void shouldRejectInvalidQuality() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/word/review").param("wordId", "3").param("quality", "6")
                        .header("X-User-Id", TEST_USER_ID.toString())
                        .header("X-Username", TEST_USERNAME))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test @Order(9) @DisplayName("复习不存在的单词 → code=2001")
    void shouldRejectReviewNonExistentWord() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/word/review").param("wordId", "99999").param("quality", "3")
                        .header("X-User-Id", TEST_USER_ID.toString())
                        .header("X-Username", TEST_USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2001));
    }

    @Test @Order(10) @DisplayName("记忆等级分布")
    void shouldGetMemoryLevelDistribution() throws Exception {
        getWithAuth("/word/memory-level/distribution", TEST_USER_ID, TEST_USERNAME)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", notNullValue()));
    }

    @Test @Order(11) @DisplayName("从阅读新增单词")
    void shouldAddWordFromReading() throws Exception {
        Map<String, String> body = Map.of("word", "serendipity", "definitionCn", "意外发现的好运",
                "definitionEn", "the occurrence of events by chance", "contextSentence", "Pure serendipity.", "partOfSpeech", "n");
        postWithAuth("/word/reading/add", body, TEST_USER_ID, TEST_USERNAME)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        Integer count = queryInt("SELECT COUNT(*) FROM enstud_word WHERE word=?", "serendipity");
        Assertions.assertEquals(1, count);
    }

    private void submitReview(Long wordId, int quality) throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/word/review").param("wordId", wordId.toString()).param("quality", String.valueOf(quality))
                        .header("X-User-Id", TEST_USER_ID.toString())
                        .header("X-Username", TEST_USERNAME))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
    }

    private Integer queryInt(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, Integer.class, args);
    }
}
