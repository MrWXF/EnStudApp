package com.enstud.forum;

import com.enstud.common.test.DbIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("论坛服务集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ForumIntegrationTest extends DbIntegrationTest {

    private static final Long USER_A = 1L, USER_B = 2L;
    private static final String USERNAME_A = "userA";
    private Long postId;

    @BeforeAll
    void setUp() {
        jdbcTemplate.update("INSERT INTO enstud_forum_category (id, name, description, sort_order) VALUES (1, '学习经验', '方法分享', 1)");
        jdbcTemplate.update("INSERT INTO enstud_forum_category (id, name, description, sort_order) VALUES (2, '问答求助', '问题交流', 3)");
    }

    @Test @Order(1) @DisplayName("获取板块列表（无需认证）")
    void shouldGetCategories() throws Exception {
        getWithoutAuth("/forum/categories")
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test @Order(2) @DisplayName("获取帖子列表（公开）")
    void shouldGetPosts() throws Exception {
        getWithoutAuth("/forum/posts")
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
    }

    @Test @Order(3) @DisplayName("发布帖子（需认证）")
    void shouldCreatePost() throws Exception {
        Map<String, Object> body = Map.of("title", "Test Post", "content", "Hello world test content.", "categoryId", 1);
        postWithAuth("/forum/posts", body, USER_A, USERNAME_A)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("Test Post"));

        postId = jdbcTemplate.queryForObject("SELECT id FROM enstud_forum_post WHERE title='Test Post'", Long.class);
        Assertions.assertNotNull(postId);
    }

    @Test @Order(4) @DisplayName("发布帖子无认证 → code=401")
    void shouldRejectCreatePostWithoutAuth() throws Exception {
        Map<String, Object> body = Map.of("title", "No Auth", "content", "Should fail.", "categoryId", 1);
        postWithoutAuth("/forum/posts", body)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(401));
    }

    @Test @Order(5) @DisplayName("获取帖子详情")
    void shouldGetPostDetail() throws Exception {
        Assertions.assertNotNull(postId, "postId should be set from previous test");
        getWithoutAuth("/forum/posts/" + postId)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("Test Post"));
    }

    @Test @Order(6) @DisplayName("回复帖子")
    void shouldReplyToPost() throws Exception {
        Map<String, String> body = Map.of("content", "Great post!");
        postWithAuth("/forum/posts/" + postId + "/reply", body, USER_B, "userB")
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
    }

    @Test @Order(7) @DisplayName("点赞帖子")
    void shouldLikePost() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/forum/like").param("targetType", "POST").param("targetId", postId.toString())
                        .header("X-User-Id", USER_A.toString()).header("X-Username", USERNAME_A))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
    }

    @Test @Order(8) @DisplayName("取消点赞（toggle）")
    void shouldUnlikePost() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/forum/like").param("targetType", "POST").param("targetId", postId.toString())
                        .header("X-User-Id", USER_A.toString()).header("X-Username", USERNAME_A))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
    }

    @Test @Order(9) @DisplayName("删除他人帖子 → 业务异常被 GlobalExceptionHandler 拦截")
    void shouldRejectDeleteOthersPost() throws Exception {
        deleteWithAuth("/forum/posts/" + postId, USER_B, "userB")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)));
    }

    @Test @Order(10) @DisplayName("按分类过滤帖子")
    void shouldFilterPostsByCategory() throws Exception {
        getWithoutAuth("/forum/posts?categoryId=1")
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test @Order(11) @DisplayName("获取不存在的帖子 → code!=0")
    void shouldRejectNonExistentPost() throws Exception {
        getWithoutAuth("/forum/posts/99999")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)));
    }

    @Test @Order(12) @DisplayName("删除自己的帖子")
    void shouldDeleteOwnPost() throws Exception {
        deleteWithAuth("/forum/posts/" + postId, USER_A, USERNAME_A)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
    }
}
