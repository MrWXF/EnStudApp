package com.enstud.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DFA 敏感词过滤器单元测试
 */
class SensitiveWordFilterTest {

    private final SensitiveWordFilter filter = new SensitiveWordFilter();

    private static final List<String> SENSITIVE_WORDS = List.of(
            "fuck", "shit", "damn", "asshole",
            "作弊", "代考", "代写",
            "色情", "赌博"
    );

    @BeforeEach
    void setUp() {
        filter.build(SENSITIVE_WORDS);
    }

    @Test
    void contains_shouldReturnTrue_whenTextContainsSensitiveWord() {
        assertTrue(filter.contains("this is fucking awesome"));
        assertTrue(filter.contains("shit happens"));
        assertTrue(filter.contains("damn it"));
        assertTrue(filter.contains("考试作弊是不对的"));
        assertTrue(filter.contains("代考请联系我"));
        assertTrue(filter.contains("色情内容"));
    }

    @Test
    void contains_shouldReturnFalse_whenTextIsClean() {
        assertFalse(filter.contains("hello world"));
        assertFalse(filter.contains("good job"));
        assertFalse(filter.contains("今天天气真好"));
        assertFalse(filter.contains("学习英语很重要"));
    }

    @Test
    void contains_shouldHandleNullAndBlank() {
        assertFalse(filter.contains(null));
        assertFalse(filter.contains(""));
        assertFalse(filter.contains("   "));
    }

    @Test
    void contains_shouldBeCaseInsensitive() {
        assertTrue(filter.contains("FUCK"));
        assertTrue(filter.contains("Shit"));
        assertTrue(filter.contains("DAMN"));
    }

    @Test
    void replace_shouldMaskSensitiveWords() {
        assertEquals("this is ****ing awesome", filter.replace("this is fucking awesome"));
        assertEquals("**** happens", filter.replace("shit happens"));
        assertEquals("考试**是不对的", filter.replace("考试作弊是不对的"));
    }

    @Test
    void replace_shouldHandleMultipleWordsInText() {
        assertEquals("**** this ****", filter.replace("shit this fuck"));
        assertEquals("考试**和****都不对", filter.replace("考试作弊和代考都不对"));
    }

    @Test
    void replace_shouldNotAlterCleanText() {
        assertEquals("hello world", filter.replace("hello world"));
        assertEquals("学习进步", filter.replace("学习进步"));
    }

    @Test
    void replace_shouldHandleNullAndBlank() {
        assertNull(filter.replace(null));
        assertEquals("", filter.replace(""));
        assertEquals("   ", filter.replace("   "));
    }

    @Test
    void findFirst_shouldReturnFirstMatch() {
        assertEquals("fuck", filter.findFirst("this is fucking awesome"));
        assertEquals("shit", filter.findFirst("shit happens"));
        assertNull(filter.findFirst("clean text"));
    }

    @Test
    void rebuild_shouldUpdateWordList() {
        assertTrue(filter.contains("fuck"));
        assertFalse(filter.contains("newbadword"));

        filter.build(List.of("newbadword"));
        assertFalse(filter.contains("fuck"));
        assertTrue(filter.contains("newbadword"));
    }
}
