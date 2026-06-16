package com.enstud.common.util;

import java.util.*;

/**
 * DFA 敏感词过滤器
 * <p>
 * 基于确定有限状态自动机（Deterministic Finite Automaton）算法实现，
 * 将敏感词列表构建为 Trie 树，实现 O(L) 时间复杂度的敏感词检测与替换
 * （L 为待检测文本长度）。
 * <p>
 * 线程安全 —— build() 完成后 filter/replace 均为纯读取操作。
 */
public class SensitiveWordFilter {

    /** 替换字符 */
    private static final char REPLACE_CHAR = '*';

    /** DFA 字典树根节点 */
    private final Map<Character, Object> root = new HashMap<>();

    /**
     * 构建敏感词字典树
     * <p>
     * 可在运行时多次调用以热更新敏感词列表。
     *
     * @param words 敏感词列表（小写，非 null）
     */
    @SuppressWarnings("unchecked")
    public void build(Collection<String> words) {
        root.clear();
        for (String word : words) {
            if (word == null || word.isBlank()) continue;
            String lower = word.trim().toLowerCase();
            Map<Character, Object> node = root;
            for (int i = 0; i < lower.length(); i++) {
                char c = lower.charAt(i);
                Object child = node.get(c);
                if (child == null) {
                    Map<Character, Object> next = new HashMap<>();
                    node.put(c, next);
                    node = next;
                } else {
                    node = (Map<Character, Object>) child;
                }
            }
            // 用空 Map 标记词尾（性能优于专用 boolean 字段）
            node.put('\0', Map.of());
        }
    }

    /**
     * 检查文本是否包含敏感词
     */
    @SuppressWarnings("unchecked")
    public boolean contains(String text) {
        if (text == null || text.isBlank()) return false;
        String lower = text.toLowerCase();
        for (int i = 0; i < lower.length(); i++) {
            Map<Character, Object> node = root;
            for (int j = i; j < lower.length(); j++) {
                char c = lower.charAt(j);
                Object child = node.get(c);
                if (child == null) break;
                node = (Map<Character, Object>) child;
                if (node.containsKey('\0')) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 替换文本中的敏感词为 *
     */
    @SuppressWarnings("unchecked")
    public String replace(String text) {
        if (text == null || text.isBlank()) return text;
        String lower = text.toLowerCase();
        char[] result = text.toCharArray();
        for (int i = 0; i < lower.length(); i++) {
            Map<Character, Object> node = root;
            int hitEnd = -1;
            for (int j = i; j < lower.length(); j++) {
                char c = lower.charAt(j);
                Object child = node.get(c);
                if (child == null) break;
                node = (Map<Character, Object>) child;
                if (node.containsKey('\0')) {
                    hitEnd = j;
                }
            }
            if (hitEnd >= 0) {
                for (int k = i; k <= hitEnd; k++) {
                    result[k] = REPLACE_CHAR;
                }
            }
        }
        return new String(result);
    }

    /**
     * 获取文本中命中的第一个敏感词，无则返回 null
     */
    @SuppressWarnings("unchecked")
    public String findFirst(String text) {
        if (text == null || text.isBlank()) return null;
        String lower = text.toLowerCase();
        for (int i = 0; i < lower.length(); i++) {
            Map<Character, Object> node = root;
            for (int j = i; j < lower.length(); j++) {
                char c = lower.charAt(j);
                Object child = node.get(c);
                if (child == null) break;
                node = (Map<Character, Object>) child;
                if (node.containsKey('\0')) {
                    return text.substring(i, j + 1);
                }
            }
        }
        return null;
    }
}
