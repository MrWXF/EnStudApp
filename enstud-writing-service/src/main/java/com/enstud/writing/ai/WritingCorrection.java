package com.enstud.writing.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * 写作批改结果实体
 * <p>
 * 对应 AI 批改返回的 JSON 结构，通过 Jackson 反序列化填充。
 * <p>
 * 包含评分、总评和详细的批改意见列表，
 * 由 {@link WritingAiClient} 的实现类构造并返回。
 *
 * @author EnStudApp
 * @since 1.0.0
 * @see WritingAiClient
 * @see DeepSeekWritingAiClient
 * @see MockWritingAiClient
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WritingCorrection {

    /** 作文评分（0–100 分，越高越好） */
    private int score;

    /** 总评文字（简体中文，2–3 句话） */
    private String overallComment;

    /** 逐条批改意见列表，可为空但不可为 {@code null} */
    private List<CorrectionItem> items;

    /**
     * 无参构造函数
     * <p>
     * 供 Jackson 反序列化使用，必须通过反射创建实例。
     */
    public WritingCorrection() {}

    /**
     * 全参构造函数
     *
     * @param score          评分（0–100）
     * @param overallComment 总评文字
     * @param items          批改意见列表
     */
    public WritingCorrection(int score, String overallComment, List<CorrectionItem> items) {
        this.score = score;
        this.overallComment = overallComment;
        this.items = items;
    }

    /** @return 作文评分（0–100） */
    public int getScore() { return score; }

    /** @return 总评文字（中文） */
    public String getOverallComment() { return overallComment; }

    /** @return 批改意见列表，不会为 {@code null} */
    public List<CorrectionItem> getItems() { return items; }

    /** @param score 评分（0–100） */
    public void setScore(int score) { this.score = score; }

    /** @param overallComment 总评文字 */
    public void setOverallComment(String overallComment) { this.overallComment = overallComment; }

    /** @param items 批改意见列表 */
    public void setItems(List<CorrectionItem> items) { this.items = items; }
}
