package com.enstud.read;

import com.enstud.common.test.DbIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.test.annotation.DirtiesContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("阅读服务集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ReadIntegrationTest extends DbIntegrationTest {

    private static final Long USER_ID = 1L;
    private static final String USERNAME = "reader";

    @BeforeAll
    void setUp() {
        jdbcTemplate.update("INSERT INTO enstud_article (id, title, url, source, summary, content, score) VALUES (1, 'Test Article', 'https://example.com/1', 'HN', 'A test summary.', 'Full article content here.', 100)");
        jdbcTemplate.update("INSERT INTO enstud_article (id, title, url, source, summary, content, score) VALUES (2, 'Another Article', 'https://example.com/2', 'GitHub', 'Another summary.', 'More content.', 80)");
    }

    @AfterAll
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM enstud_article_read");
        jdbcTemplate.update("DELETE FROM enstud_article");
    }

    @Test @Order(1) @DisplayName("无认证 → @AuthRequired 拦截 401")
    void shouldRejectWithoutAuth() throws Exception {
        getWithoutAuth("/read/hot").andExpect(status().isUnauthorized());
    }

    @Test @Order(2) @DisplayName("获取热门文章列表")
    void shouldGetHotArticles() throws Exception {
        getWithAuth("/read/hot", USER_ID, USERNAME)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test @Order(3) @DisplayName("查看文章详情")
    void shouldGetArticleDetail() throws Exception {
        getWithAuth("/read/1", USER_ID, USERNAME)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("Test Article"));
    }

    @Test @Order(4) @DisplayName("文章不存在 → 404")
    void shouldRejectNonExistentArticle() throws Exception {
        getWithAuth("/read/99999", USER_ID, USERNAME)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(404));
    }

    @Test @Order(5) @DisplayName("收藏文章")
    void shouldBookmarkArticle() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/read/1/bookmark")
                        .header("X-User-Id", USER_ID.toString()).header("X-Username", USERNAME))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test @Order(6) @DisplayName("取消收藏（toggle）")
    void shouldUnbookmarkArticle() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/read/1/bookmark")
                        .header("X-User-Id", USER_ID.toString()).header("X-Username", USERNAME))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test @Order(7) @DisplayName("获取收藏列表")
    void shouldGetBookmarks() throws Exception {
        getWithAuth("/read/bookmarks", USER_ID, USERNAME)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
    }

    @Test @Order(8) @DisplayName("获取文章来源列表")
    void shouldGetSources() throws Exception {
        getWithAuth("/read/sources", USER_ID, USERNAME)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
    }
}
