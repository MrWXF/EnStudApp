package com.enstud.forum.service.impl;

import com.enstud.common.util.SensitiveWordFilter;
import com.enstud.forum.service.ForumSensitiveService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 论坛敏感词过滤服务实现
 * <p>
 * 使用 DFA 算法，启动时从 sensitive-words.txt 加载敏感词列表。
 * 提供 {@link #refreshWords(List)} 方法支持运行时热更新。
 */
@Slf4j
@Service
public class ForumSensitiveServiceImpl implements ForumSensitiveService {

    private final SensitiveWordFilter filter = new SensitiveWordFilter();

    @Value("classpath:sensitive-words.txt")
    private Resource sensitiveWordsResource;

    /** 当前加载的敏感词列表（仅用于调试和管理） */
    private volatile List<String> currentWords = Collections.emptyList();

    @PostConstruct
    public void init() {
        reloadFromFile();
    }

    private void reloadFromFile() {
        try {
            List<String> words = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(sensitiveWordsResource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                        words.add(trimmed.toLowerCase());
                    }
                }
            }
            filter.build(words);
            this.currentWords = List.copyOf(words);
            log.info("敏感词过滤已初始化，共加载 {} 个敏感词", words.size());
        } catch (Exception e) {
            log.warn("加载敏感词文件失败，使用空列表", e);
            filter.build(List.of());
            this.currentWords = Collections.emptyList();
        }
    }

    @Override
    public boolean containsSensitive(String text) {
        return filter.contains(text);
    }

    @Override
    public String filterSensitive(String text) {
        return filter.replace(text);
    }

    @Override
    public String findFirstSensitive(String text) {
        return filter.findFirst(text);
    }

    @Override
    public void refreshWords(List<String> words) {
        List<String> lowerWords = words.stream()
                .map(w -> w.trim().toLowerCase())
                .filter(w -> !w.isEmpty())
                .toList();
        filter.build(lowerWords);
        this.currentWords = List.copyOf(lowerWords);
        log.info("敏感词列表已热更新，共 {} 个敏感词", lowerWords.size());
    }

    @Override
    public SensitiveWordFilter getFilter() {
        return filter;
    }
}
