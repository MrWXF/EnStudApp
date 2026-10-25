package com.enstud.translate.ai;

import com.enstud.common.SecurityContext;
import com.enstud.common.ai.DeepSeekClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 基于 DeepSeek 的真实翻译客户端
 * <p>
 * 通过 DeepSeek Chat Completion API 实现英汉互译。
 * 仅在 {@code ai.mock=false} 或未配置 {@code ai.mock} 时生效；
 * 若 {@code ai.mock=true} 则 {@link MockTranslateClient} 生效。
 * <p>
 * 翻译计入 translate 每日配额（未登录时跳过配额检查）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.mock", havingValue = "false", matchIfMissing = true)
public class DeepSeekTranslateClient implements TranslateClient {

    private final DeepSeekClient deepSeekClient;

    @Override
    public String translate(String text, String from, String to) {
        String sourceLang = (from == null || from.isBlank() || "auto".equals(from)) ? "自动检测" : from;
        String targetLang = (to == null || to.isBlank()) ? "zh" : to;

        String systemPrompt = """
                You are a professional translator for a language-learning app.
                Translate the user's text from %s into %s.
                Rules:
                1. Output ONLY the translation, with no explanations, quotes, or extra text.
                2. Preserve the original tone, formatting and line breaks.
                3. If the text is a single word, output a concise dictionary-style meaning
                   (part of speech + Chinese glosses), e.g. "v. 放弃；抛弃" or "n. 能力；才能".
                """.formatted(sourceLang, targetLang);

        // 已登录用户计入 translate 每日配额（getCurrentUserId 未登录时返回 null）
        Long userId = SecurityContext.getCurrentUserId();
        String result = deepSeekClient.chat(systemPrompt, text, userId, "translate");
        log.debug("DeepSeek translation ({} -> {}): {}", sourceLang, targetLang, result);
        return result;
    }
}
