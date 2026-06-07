package com.enstud.translate.ai;

import org.springframework.stereotype.Component;

@Component
public class MockTranslateClient implements TranslateClient {

    @Override
    public String translate(String text, String from, String to) {
        String sourceLang = (from != null && !from.isEmpty()) ? from : "auto";
        String targetLang = (to != null && !to.isEmpty()) ? to : "en";

        // 简单中英互译模拟
        if (text.contains("你好") || text.contains("hello")) {
            return "zh".equals(sourceLang) || text.contains("你好") ? "Hello, how are you?" : "你好，你好吗？";
        }
        if (text.contains("谢谢") || text.contains("thank")) {
            return "zh".equals(sourceLang) || text.contains("谢谢") ? "Thank you very much!" : "非常感谢！";
        }
        if (text.contains("天气") || text.contains("weather")) {
            return "zh".equals(sourceLang) || text.contains("天气") ? "The weather is very nice today." : "今天天气很好。";
        }
        if (text.contains("爱") || text.contains("love")) {
            return "zh".equals(sourceLang) || text.contains("爱") ? "I love learning English." : "我爱学英语。";
        }

        // 当无法识别时给出简单响应
        if ("zh".equals(targetLang)) {
            return "（翻译结果：" + text + " → 目标语言中文。请在实际使用时接入翻译 API。）";
        }
        return "(Translation: " + text + " → English. Please connect a translation API for actual use.)";
    }
}
