package com.enstud.common.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MemoryLevelTest {

    @Test
    void testFromLevel() {
        assertEquals(MemoryLevel.NOT_LEARNED, MemoryLevel.fromLevel(0));
        assertEquals(MemoryLevel.FUZZY, MemoryLevel.fromLevel(1));
        assertEquals(MemoryLevel.FAMILIAR, MemoryLevel.fromLevel(2));
        assertEquals(MemoryLevel.BASIC, MemoryLevel.fromLevel(3));
        assertEquals(MemoryLevel.PROFICIENT, MemoryLevel.fromLevel(4));
        assertEquals(MemoryLevel.MASTERED, MemoryLevel.fromLevel(5));
        assertEquals(MemoryLevel.NOT_LEARNED, MemoryLevel.fromLevel(99));
    }

    @Test
    void testCalcFromMastery_NotLearned() {
        assertEquals(MemoryLevel.NOT_LEARNED,
                MemoryLevel.calcFromMastery(0, 0, "LEARNING"));
    }

    @Test
    void testCalcFromMastery_Fuzzy() {
        assertEquals(MemoryLevel.FUZZY,
                MemoryLevel.calcFromMastery(5, 0, "LEARNING"));
        assertEquals(MemoryLevel.FUZZY,
                MemoryLevel.calcFromMastery(20, 1, "REVIEWING"));
    }

    @Test
    void testCalcFromMastery_Familiar() {
        assertEquals(MemoryLevel.FAMILIAR,
                MemoryLevel.calcFromMastery(25, 2, "REVIEWING"));
        assertEquals(MemoryLevel.FAMILIAR,
                MemoryLevel.calcFromMastery(40, 3, "REVIEWING"));
    }

    @Test
    void testCalcFromMastery_Basic() {
        assertEquals(MemoryLevel.BASIC,
                MemoryLevel.calcFromMastery(45, 4, "REVIEWING"));
        assertEquals(MemoryLevel.BASIC,
                MemoryLevel.calcFromMastery(60, 5, "REVIEWING"));
    }

    @Test
    void testCalcFromMastery_Proficient() {
        assertEquals(MemoryLevel.PROFICIENT,
                MemoryLevel.calcFromMastery(65, 6, "REVIEWING"));
        assertEquals(MemoryLevel.PROFICIENT,
                MemoryLevel.calcFromMastery(85, 7, "REVIEWING"));
    }

    @Test
    void testCalcFromMastery_Mastered() {
        assertEquals(MemoryLevel.MASTERED,
                MemoryLevel.calcFromMastery(90, 8, "MASTERED"));
        assertEquals(MemoryLevel.MASTERED,
                MemoryLevel.calcFromMastery(100, 10, "MASTERED"));
    }

    @Test
    void testCalcFromMastery_WithQuality() {
        // quality < 3 且 repetitions == 0 时应为 FUZZY
        assertEquals(MemoryLevel.FUZZY,
                MemoryLevel.calcFromMastery(33, 0, "LEARNING", 1));
        // quality == 0 时 NOT_LEARNED
        assertEquals(MemoryLevel.NOT_LEARNED,
                MemoryLevel.calcFromMastery(60, 0, "LEARNING", 0));
        // quality >= 3 正常计算
        assertEquals(MemoryLevel.PROFICIENT,
                MemoryLevel.calcFromMastery(70, 1, "REVIEWING", 4));
    }

    @Test
    void testLabels() {
        assertEquals("未学习", MemoryLevel.NOT_LEARNED.label);
        assertEquals("模糊", MemoryLevel.FUZZY.label);
        assertEquals("有印象", MemoryLevel.FAMILIAR.label);
        assertEquals("基本掌握", MemoryLevel.BASIC.label);
        assertEquals("熟练", MemoryLevel.PROFICIENT.label);
        assertEquals("精通", MemoryLevel.MASTERED.label);
    }

    @Test
    void testEmojis() {
        assertEquals("🆕", MemoryLevel.NOT_LEARNED.emoji);
        assertEquals("😵‍💫", MemoryLevel.FUZZY.emoji);
        assertEquals("🤔", MemoryLevel.FAMILIAR.emoji);
        assertEquals("👍", MemoryLevel.BASIC.emoji);
        assertEquals("💪", MemoryLevel.PROFICIENT.emoji);
        assertEquals("🏆", MemoryLevel.MASTERED.emoji);
    }
}
