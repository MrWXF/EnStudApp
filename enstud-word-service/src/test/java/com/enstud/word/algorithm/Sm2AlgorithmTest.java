package com.enstud.word.algorithm;

import com.enstud.word.entity.UserWordRecord;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Sm2AlgorithmTest {

    private UserWordRecord createRecord() {
        UserWordRecord r = new UserWordRecord();
        r.setUserId(1L);
        r.setWordId(1L);
        r.setEaseFactor(2.5);
        r.setReviewInterval(0);
        r.setRepetitions(0);
        r.setMasteryLevel(0);
        r.setMemoryLevel(0);
        r.setStatus("LEARNING");
        return r;
    }

    @Test
    void testFirstReview_PerfectQuality() {
        UserWordRecord r = createRecord();
        Sm2Algorithm.update(r, 5);

        assertEquals(1, r.getReviewInterval());
        assertEquals(1, r.getRepetitions());
        assertTrue(r.getMasteryLevel() > 0);
        assertTrue(r.getMemoryLevel() >= 1); // 至少 FUZZY
        assertNotNull(r.getNextReviewTime());
        assertNotNull(r.getLastReviewTime());
    }

    @Test
    void testFirstReview_Wrong() {
        UserWordRecord r = createRecord();
        Sm2Algorithm.update(r, 1);

        assertEquals(1, r.getReviewInterval()); // reset to 1
        assertEquals(0, r.getRepetitions());   // reset
        assertEquals(1, r.getMemoryLevel());   // FUZZY — quality=1<3 且 rep=0
    }

    @Test
    void testQualityOutOfRange() {
        UserWordRecord r = createRecord();
        assertThrows(IllegalArgumentException.class, () -> Sm2Algorithm.update(r, 6));
        assertThrows(IllegalArgumentException.class, () -> Sm2Algorithm.update(r, -1));
    }

    @Test
    void testMultipleCorrectReviews_ReachesMastered() {
        UserWordRecord r = createRecord();

        // 第一次
        Sm2Algorithm.update(r, 5);
        assertEquals(1, r.getReviewInterval());

        // 第二次（6天后）
        r.setNextReviewTime(null);
        Sm2Algorithm.update(r, 4);
        assertEquals(6, r.getReviewInterval());

        // 重复几次，最终应该达到精通
        for (int i = 0; i < 10; i++) {
            Sm2Algorithm.update(r, 5);
        }
        assertTrue(r.getMasteryLevel() >= 90);
        assertEquals("MASTERED", r.getStatus());
        assertEquals(5, r.getMemoryLevel()); // MASTERED
    }

    @Test
    void testReviewAfterWrong_ResetsMemoryLevel() {
        UserWordRecord r = createRecord();
        // 先正确回答几次
        Sm2Algorithm.update(r, 5);
        Sm2Algorithm.update(r, 4);
        assertTrue(r.getMemoryLevel() >= 1);

        // 然后回答错误
        Sm2Algorithm.update(r, 1);
        assertEquals(0, r.getRepetitions());
        assertEquals(1, r.getReviewInterval()); // reset
    }

    @Test
    void testEaseFactorNeverBelowMinimum() {
        UserWordRecord r = createRecord();
        // 反复答错，easeFactor 不应该低于 1.3
        for (int i = 0; i < 20; i++) {
            Sm2Algorithm.update(r, 0);
        }
        assertEquals(1.3, r.getEaseFactor(), 0.001);
    }

    @Test
    void testMemoryLevelProgressesCorrectly() {
        UserWordRecord r = createRecord();

        // 连续正确回答，验证记忆等级逐步提升
        for (int i = 0; i < 3; i++) {
            Sm2Algorithm.update(r, 5);
        }

        // 高 mastery 应该对应较高记忆等级
        int ml = r.getMemoryLevel();
        assertTrue(ml >= 1, "经过3次正确复习，记忆等级应 >= 1");
    }
}
