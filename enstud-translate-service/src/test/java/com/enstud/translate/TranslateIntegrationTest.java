package com.enstud.translate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * translate-service 集成测试
 * <p>
 * 验证：@SpringBootTest + test profile + AI mock 基础设施正常
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("翻译服务集成测试")
class TranslateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== 正常翻译场景 ====================

    @Test
    @DisplayName("英译中：hello → 中文问候语")
    void shouldTranslateEnglishToChinese() throws Exception {
        Map<String, String> body = Map.of("text", "hello", "from", "en", "to", "zh");

        mockMvc.perform(post("/translate/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.translatedText", containsString("你好")))
                .andExpect(jsonPath("$.data.sourceText").value("hello"))
                .andExpect(jsonPath("$.data.from").value("en"))
                .andExpect(jsonPath("$.data.to").value("zh"));
    }

    @Test
    @DisplayName("中译英：你好 → English greeting")
    void shouldTranslateChineseToEnglish() throws Exception {
        Map<String, String> body = Map.of("text", "你好", "from", "zh", "to", "en");

        mockMvc.perform(post("/translate/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.translatedText", containsString("Hello")))
                .andExpect(jsonPath("$.data.sourceText").value("你好"))
                .andExpect(jsonPath("$.data.to").value("en"));
    }

    @Test
    @DisplayName("自动检测源语言：from 为空时自动识别")
    void shouldAutoDetectSourceLanguage() throws Exception {
        Map<String, String> body = Map.of("text", "天气", "to", "en");

        mockMvc.perform(post("/translate/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.from").value("auto"))
                .andExpect(jsonPath("$.data.translatedText", not(emptyString())));
    }

    @Test
    @DisplayName("未知文本：返回通用 fallback 翻译")
    void shouldReturnFallbackForUnknownText() throws Exception {
        Map<String, String> body = Map.of("text", "zxyqrs_unknown", "from", "en", "to", "zh");

        mockMvc.perform(post("/translate/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.translatedText", not(emptyString())));
    }

    // ==================== 参数校验场景 ====================

    @Test
    @DisplayName("空文本校验：text 为空 → 400 参数错误")
    void shouldRejectEmptyText() throws Exception {
        Map<String, String> body = Map.of("text", "", "from", "en", "to", "zh");

        mockMvc.perform(post("/translate/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("缺失必填字段 text → 400")
    void shouldRejectMissingTextField() throws Exception {
        Map<String, String> body = Map.of("from", "en", "to", "zh");

        mockMvc.perform(post("/translate/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("缺失必填字段 to → 400")
    void shouldRejectMissingToField() throws Exception {
        Map<String, String> body = Map.of("text", "hello", "from", "en");

        mockMvc.perform(post("/translate/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("超长文本：不应崩溃，正常返回")
    void shouldHandleVeryLongText() throws Exception {
        String longText = "hello ".repeat(1000); // ~6KB
        Map<String, String> body = Map.of("text", longText, "from", "en", "to", "zh");

        mockMvc.perform(post("/translate/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
