package com.enstud.forum.service;

import com.enstud.common.util.SensitiveWordFilter;

import java.util.List;

/**
 * 论坛敏感词过滤服务接口
 */
public interface ForumSensitiveService {

    /**
     * 检测文本是否包含敏感词
     */
    boolean containsSensitive(String text);

    /**
     * 替换文本中的敏感词为 *
     */
    String filterSensitive(String text);

    /**
     * 获取最先命中的敏感词
     */
    String findFirstSensitive(String text);

    /**
     * 热更新敏感词列表
     */
    void refreshWords(List<String> words);

    /**
     * 获取当前加载的 DFA 过滤器（供单元测试校验）
     */
    SensitiveWordFilter getFilter();
}
