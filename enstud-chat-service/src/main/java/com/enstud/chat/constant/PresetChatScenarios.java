package com.enstud.chat.constant;

import java.util.List;

/**
 * 预置对话场景库
 */
public class PresetChatScenarios {

    public static final List<ChatScenario> SCENARIOS = List.of(
            // 日常场景
            new ChatScenario("餐厅点餐", "Restaurant Ordering", 1, "学习在餐厅点餐的常用表达", "daily"),
            new ChatScenario("超市购物", "Supermarket Shopping", 1, "学习购物时的英语对话", "daily"),
            new ChatScenario("问路", "Asking for Directions", 1, "学习如何询问和指引方向", "daily"),
            new ChatScenario("打车", "Taking a Taxi", 1, "学习打车时的常用表达", "daily"),
            new ChatScenario("酒店入住", "Hotel Check-in", 2, "学习酒店入住和求助的表达", "daily"),

            // 旅行场景
            new ChatScenario("机场出行", "Airport Travel", 2, "学习机场值机、安检、登机的表达", "travel"),
            new ChatScenario("景点游览", "Sightseeing", 2, "学习参观景点时的对话", "travel"),
            new ChatScenario("租车自驾", "Car Rental", 3, "学习租车和自驾时的英语", "travel"),
            new ChatScenario("紧急情况", "Emergency Situations", 3, "学习求助和紧急情况的表达", "travel"),

            // 商务场景
            new ChatScenario("会议开场", "Meeting Introduction", 3, "学习商务会议的开场白", "business"),
            new ChatScenario("项目汇报", "Project Presentation", 4, "学习项目汇报和演示的表达", "business"),
            new ChatScenario("商务谈判", "Business Negotiation", 4, "学习商务谈判的技巧和表达", "business"),
            new ChatScenario("电话会议", "Conference Call", 3, "学习电话会议的常用表达", "business"),

            // 社交场景
            new ChatScenario("自我介绍", "Self Introduction", 1, "学习自我介绍的表达方式", "social"),
            new ChatScenario("聚会闲聊", "Party Chat", 2, "学习聚会时的轻松对话", "social"),
            new ChatScenario("兴趣讨论", "Hobby Discussion", 2, "学习讨论兴趣爱好的表达", "social"),

            // 学习场景
            new ChatScenario("校园生活", "Campus Life", 2, "学习校园日常对话", "study"),
            new ChatScenario("课堂讨论", "Classroom Discussion", 3, "学习课堂讨论的表达", "study"),
            new ChatScenario("学术报告", "Academic Presentation", 4, "学习学术报告的表达技巧", "study")
    );

    public record ChatScenario(
            String nameCn,
            String nameEn,
            Integer difficulty,
            String description,
            String category
    ) {}
}
