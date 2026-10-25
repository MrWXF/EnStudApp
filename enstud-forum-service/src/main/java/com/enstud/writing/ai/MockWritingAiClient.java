package com.enstud.writing.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟写作批改客户端（用于开发测试）
 * <p>
 * 当 {@code application.yml} 中配置 {@code ai.mock=true} 时生效；
 * 若未配置或 {@code ai.mock=false}，则 {@link DeepSeekWritingAiClient} 优先生效。
 * <p>
 * 基于简单的关键词匹配和规则计算评分，
 * 仅用于前后端联调，<b>不可用于生产环境</b>。
 *
 * @author EnStudApp
 * @since 1.0.0
 * @see DeepSeekWritingAiClient
 * @see WritingAiClient
 */
@Component
@ConditionalOnProperty(name = "ai.mock", havingValue = "true")
public class MockWritingAiClient implements WritingAiClient {

    /**
     * 批改英语作文（模拟实现）
     * <p>
     * 处理流程：
     * <ol>
     *   <li>检测常见拼写/语法错误（基于关键词匹配）</li>
     *   <li>根据错误数量计算评分（满分 90，每项错误扣 10 分，最低 40 分）</li>
     *   <li>根据评分生成总评</li>
     * </ol>
     *
     * @param request 批改请求，包含作文标题、内容、话题类型
     * @return 批改结果，包含评分、总评和批改意见列表
     */
    @Override
    public WritingCorrection correct(WritingRequest request) {
        List<CorrectionItem> items = new ArrayList<>();
        String content = request.getContent();

        // 检测：第一人称代词未大写
        if (content.contains("i ")) {
            items.add(new CorrectionItem("Grammar", "\"i\"", "\"I\"", "第一人称代词 'I' 必须大写。"));
        }
        // 检测：常见缩写错误
        if (content.contains("dont")) {
            items.add(new CorrectionItem("Spelling", "\"dont\"", "\"don't\"", "缩写需要加撇号 ' 。"));
        }
        // 检测：their/there 混用
        if (content.contains("their") && content.contains("is")) {
            items.add(new CorrectionItem("Grammar", "their ... is", "there ... is / their ... are", "区分 there 和 their 的用法。"));
        }

        // 检测：文章长度过短
        String[] words = content.split("\\s+");
        if (words.length < 50) {
            items.add(new CorrectionItem("Structure", "短篇文章", "尝试扩展到 100 词以上", "适当扩展内容可以使论述更充实。"));
        }

        // 根据错误数量计算评分
        int score = Math.max(40, 90 - items.size() * 10);
        // 根据评分生成总评
        String comment = score >= 80 ? "写得不错！继续加油。" :
                         score >= 60 ? "基本合格，还有一些地方可以改进。" :
                         "需要多加练习，注意语法和拼写。";

        return new WritingCorrection(score, comment, items);
    }
}
