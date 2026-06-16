# EnStudApp 功能增强方案 v2.0

> 基于现有架构（Spring Boot 3.2.4 + Spring Cloud Alibaba + React + Ant Design Mobile）提出的三大功能增强 + 两个系统优化 + 测试策略

---

## 目录

- [一、阅读与单词联动（选中查词）](#一阅读与单词联动选中查词)
- [二、首页改造成学习仪表盘](#二首页改造成学习仪表盘)
- [三、学习热力图 / 目标管理 / 错词本](#三学习热力图--目标管理--错词本)
- [四、论坛敏感词过滤方案](#四论坛敏感词过滤方案)
- [五、健康检查脚本 + CI 强化](#五健康检查脚本--ci-强化)
- [六、实施路线图](#六实施路线图)

---

## 一、阅读与单词联动（选中查词）

### 1.1 背景

现有 `read-service` 提供英文技术文章聚合展示，用户阅读时遇到生词需跳转到 `word-service` 手动查询，体验割裂。需要一个 **无缝的阅读查词体验**。

### 1.2 整体流程

```
用户选中英文文本
        │
        ▼
前端弹出浮动气泡（Popover）
        │
        ▼
调用 translate-service 的 /translate/word 接口（单词翻译）
        │
        ▼
显示：音标 + 释义 + 例句 + 【加入生词本】按钮
        │
        ▼
点击【加入生词本】→ 调 word-service 添加学习记录
```

### 1.3 后端改动

#### read-service 新增接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/read/articles/{id}/translations` | 预缓存整篇文章的全部单词翻译（批量优化） |
| `POST` | `/read/articles/{id}/vocab/{wordId}` | 将文章中的单词加入用户的生词本（关联文章上下文） |

#### translate-service 新增接口

```java
@Operation(summary = "查单个单词（轻量翻译）")
@GetMapping("/translate/word")
public Result<WordTranslateDTO> translateWord(
    @RequestParam @NotBlank String word,
    @RequestParam(defaultValue = "en") String from,
    @RequestParam(defaultValue = "zh") String to
)
```

**WordTranslateDTO：**
```java
public record WordTranslateDTO(
    String word,              // 原文
    String phonetic,          // 音标（如 /ˈpɜːrsɪst/）
    String translation,       // 中文释义
    List<String> examples,    // 例句列表
    Long wordId               // 本地词库ID（若已存在，用于快速加入生词本）
) {}
```

#### 优化：全文单词预缓存

当用户打开文章详情页时，前端可调用预缓存接口，后端用 NLP 或简单正则提取文章中的所有英文单词，批量调用翻译 API 并缓存到 Redis（TTL=24h），避免逐个查询。

```java
// read-service 伪代码
@PostConstruct
public void initWordCache(Long articleId, String content) {
    Set<String> words = extractEnglishWords(content);  // 正则提取
    // 批量查词库是否存在
    List<Word> existing = wordClient.batchQuery(words);
    // 不存在的才调翻译
    List<WordTranslateDTO> missing = translateClient.batchTranslate(existing);
    // 写入 Redis 缓存
    redisTemplate.opsForHash().putAll("article:words:" + articleId, wordMap);
}
```

### 1.4 前端改动

#### 阅读页面组件改造

```tsx
// ReadingPage.tsx — 新增浮动查词功能

// 1. 监听选中事件
const handleTextSelect = useCallback(() => {
  const selection = window.getSelection();
  const text = selection?.toString().trim();
  if (!text || !isEnglishWord(text)) return; // 只处理英文单词

  // 2. 获取选中区域位置
  const range = selection!.getRangeAt(0);
  const rect = range.getBoundingClientRect();

  // 3. 显示 Popover
  setSelectedWord({ text, position: { x: rect.left, y: rect.bottom } });
}, []);

// 2. 查词气泡 Popover
<Popover
  visible={!!selectedWord}
  content={
    <WordPopover
      word={selectedWord.text}
      onAddToVocab={(wordId) => addToVocab(wordId, articleId)}
    />
  }
  placement="top"
>
```

#### 添加词库中转页面

加入生词本后，显示一个 **"已加入 → 查看生词本"** 的 Toast，点击可跳转至 word-service 的复习页面。

#### 样式要点

- 选中文本加高亮底色（如浅黄色 `#fff3bf`）
- Popover 圆角卡片，深色或白色主题与阅读页一致
- 浮动图标建议：`📖` 或自定义 SVG 查词图标

### 1.5 数据流图

```
用户选中 "persist"
    │
    ▼
Frontend: window.getSelection()
    │  GET /translate/word?word=persist
    ▼
translate-service → 调用翻译 API / 本地词库
    │
    ▼
返回: { word: "persist", phonetic: "/pərˈsɪst/",
        translation: "坚持；持续", examples: [...], wordId: 123 }
    │
    ▼
Frontend: Popover 展示
    │
    ├─ 点击【加入生词本】→ POST /word/records → 创建 UserWordRecord
    │
    └─ 关闭 Popover → 继续阅读
```

---

## 二、首页改造成学习仪表盘

### 2.1 背景

当前首页（Home）只是简单导航入口，缺少数据驱动感。改造后应让用户一登录就看到学习进度总览，提升学习动力。

### 2.2 仪表盘布局

```
┌─────────────────────────────────────────────────┐
│  👋 早上好，小明！  🔥 已连续学习 7 天           │
│  ┌──────┬──────┬──────┬──────┐                  │
│  │ 今日  │ 单词  │ 累计  │ 掌握  │                  │
│  │ 学习  │ 复习  │ 单词  │ 率    │                  │
│  │ 15min │ 12个  │ 356个 │ 78%   │                  │
│  └──────┴──────┴──────┴──────┘                  │
│                                                  │
│  📊 本周学习趋势                                  │
│  [柱状图：周一 ██ 周二 ███ 周三 █ 周四 ████ ...] │
│                                                  │
│  📚 待复习单词（5个）   🎯 今日目标：20个单词      │
│  ┌────────────────────  ┌───────────────────────  │
│  │ persistence  - 70%   │  🔘 已完成 12/20       │
│  │ algorithm    - 45%   │  [████████░░░░░░░]     │
│  │ architecture - 60%   │                        │
│  └────────────────────  └───────────────────────  │
│                                                  │
│  🔥 快速入口卡片                                  │
│  [背单词] [AI对话] [阅读文章] [写作练习]          │
└─────────────────────────────────────────────────┘
```

### 2.3 后端改动

#### 新增 DashboardController（放在 gateway 或 user-service）

```java
@Tag(name = "学习仪表盘")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final WordService wordService;
    private final UserService userService;
    private final ChatService chatService;

    @Operation(summary = "获取仪表盘数据")
    @GetMapping("/stats")
    public Result<DashboardDTO> getDashboard() {
        Long userId = SecurityContext.getCurrentUserId();
        return Result.success(dashboardService.getStats(userId));
    }
}
```

**DashboardDTO：**
```java
public record DashboardDTO(
    // 问候与基础统计
    String greeting,              // "早上好" / "下午好"
    String streakDays,            // 连续学习天数
    // 四格统计
    int todayMinutes,             // 今日学习时长（分钟）
    int todayReviewCount,         // 今日复习单词数
    int totalWords,               // 累计学习单词
    int masteryRate,              // 掌握率百分比
    // 趋势数据
    List<DailyTrend> weeklyTrend, // 本周每日学习数据
    // 待复习
    List<DueWordDTO> dueWords,    // 待复习单词列表（上限5）
    // 目标进度
    int dailyGoal,                // 每日目标数
    int dailyProgress,            // 今日已学
    // 快速入口（各模块最新动态）
    int unreadMessages,           // AI对话未读
    List<HotArticleDTO> hotArticles // 热门阅读
) {}
```

#### 现有接口复用（无需大规模改造）

| 数据 | 来源 | 备注 |
|------|------|------|
| 今日学习时长 | `word-service` 学习记录 | 按日期聚合 `learning_duration` |
| 连续天数 | `user-service` 签到/学习记录 | 新增 `learning_streak` 逻辑 |
| 掌握率 | `word-service` | `count(MASTERED) / count(total)` |
| 待复习 | `word-service` | `next_review_time <= now()` |
| 每日目标 | `user-service` 用户配置 | `user_config` 表新增字段 |

#### 新增表：学习连续记录

```sql
CREATE TABLE IF NOT EXISTS `enstud_learning_streak` (
    `id`            BIGINT    NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT    NOT NULL,
    `study_date`    DATE      NOT NULL COMMENT '学习日期',
    `duration_min`  INT       NOT NULL DEFAULT 0 COMMENT '当日学习时长',
    `word_count`    INT       NOT NULL DEFAULT 0 COMMENT '当日学习单词数',
    `created_at`    DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `study_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习连续记录';
```

学习时长统计逻辑：每次 `word-service` 创建/更新学习记录时，记录时间戳差，归入 `enstud_learning_streak` 表。

### 2.4 前端改动

#### 仪表盘页面组件结构

```tsx
// pages/dashboard/DashboardPage.tsx
// 子组件拆分：
// - GreetingCard      — 问候语 + 连续天数
// - StatsGrid         — 四格统计卡片
// - WeeklyChart       — 本周学习趋势柱状图
// - DueWordsCard      — 待复习单词列表
// - DailyGoalCard     — 今日目标进度条
// - QuickActions      — 快速入口卡片
```

#### 状态管理

```tsx
// stores/useDashboardStore.ts
interface DashboardState {
  stats: DashboardDTO | null;
  loading: boolean;
  fetchStats: () => Promise<void>;
}
```

#### 技术要点

- **柱状图**：使用轻量 `recharts` 或纯 CSS 柱状图（避免大依赖）
- **目标进度条**：Ant Design Mobile `Progress` 组件
- **问候语**：`new Date().getHours()` 判断时段
- **刷新策略**：每次进入首页时自动调 `/dashboard/stats`，配合 `useFocusEffect`

---

## 三、学习热力图 / 目标管理 / 错词本

### 3.1 整体概览

三个独立但互补的功能，共同构成"学习数据闭环"：

```
热力图（回顾过去）→ 目标管理（驱动现在）→ 错词本（攻克薄弱）
         │                       │                    │
    可视化学习轨迹          设定每日目标          集中复习弱点
```

### 3.2 学习热力图

#### 设计（GitHub-style Contribution Graph）

```
         第 8 周  第 9 周  第 10周  第 11周
  周一     ░░      ░░       ██       ██
  周二     ░░      ██       ████     ██
  周三     ██      ██       ██       ░░
  周四     ░░      ████     ████     ████
  周五     ██      ██       ░░       ██
  周六     ████    ████     ████     ████
  周日     ████    ████     ██       ████
                颜色：浅→深 = 0→20+ 单词
```

#### 后端接口

```java
@Operation(summary = "获取热力图数据（最近365天）")
@GetMapping("/dashboard/heatmap")
public Result<List<HeatmapCellDTO>> getHeatmap(
    @RequestParam(defaultValue = "365") int days) {
    // 返回 { date: "2026-01-15", count: 12 } 数组
}
```

#### 前端实现

- 7 行 × 53 列网格（一年）
- 每个格子颜色根据学习单词数 5 级：`#ebedf0` → `#9be9a8` → `#40c463` → `#30a14e` → `#216e39`
- 支持点击格子查看当天详情
- `tooltip` 悬浮显示："2026-01-15：学习了 12 个单词"

### 3.3 目标管理

#### 设计

- **每日单词目标**：默认 20 个/天，用户可自定义（5-100）
- **每日学习时长目标**：默认 30 分钟
- **目标更新周期**：每天 00:00 重置
- **连续达标奖励**：连续 7/14/30 天达标，前端展示成就徽章

#### 后端表结构（复用或新增）

```sql
-- 用户配置表（新增）
CREATE TABLE IF NOT EXISTS `enstud_user_config` (
    `id`                  BIGINT    NOT NULL AUTO_INCREMENT,
    `user_id`             BIGINT    NOT NULL,
    `daily_word_goal`     INT       NOT NULL DEFAULT 20,
    `daily_minute_goal`   INT       NOT NULL DEFAULT 30,
    `enable_streak_alert` TINYINT   NOT NULL DEFAULT 1 COMMENT '连续学习提醒',
    `created_at`          DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`          DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户配置表';
```

#### 后端接口

```java
// 获取/更新目标
@GetMapping("/user/goal")
public Result<UserGoalDTO> getGoal();
@PutMapping("/user/goal")
public Result<Void> updateGoal(@Valid @RequestBody UpdateGoalRequest request);
```

### 3.4 错词本

#### 设计

错词本本质上是 **特定筛选条件下的 UserWordRecord 列表**，但提供更好的 UX：

```
┌─────────────────────────────────────────────┐
│  错词本  (23 个单词需要重新学习)             │
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │ 筛选：全部 | 本周 | 本月              │  │
│  ├────────────────────────────────────────┤  │
│  │ 📝 persistence      ❌错了 3 次  ▶ 复习│  │
│  │ 📝 algorithm         ❌错了 5 次  ▶ 复习│  │
│  │ 📝 simultaneously   ❌错了 2 次  ▶ 复习│  │
│  │ ...                                    │  │
│  └────────────────────────────────────────┘  │
│                                              │
│  [一键复习所有错词]                           │
└─────────────────────────────────────────────┘
```

#### 后端接口

```java
@Operation(summary = "获取错词列表")
@GetMapping("/word/wrong-words")
public Result<PageResult<WrongWordDTO>> getWrongWords(
    @RequestParam(required = false) String timeFilter, // all/week/month
    @RequestParam(required = false) String cursor,
    @RequestParam(defaultValue = "20") int limit) {
    // 筛选条件：quality < 3 且 repetitions == 0（重置状态）
    // 或按时间范围
}
```

**WrongWordDTO：**
```java
public record WrongWordDTO(
    Long recordId,
    Long wordId,
    String word,           // 英文
    String translation,    // 中文
    int wrongCount,        // 累计答错次数
    int totalAttempts,     // 总尝试次数
    LocalDateTime lastWrongTime
) {}
```

#### 实现要点

- `word-service` 新增 `wrong_count` 字段累加答错次数（`quality < 3` 时递增）
- 答对时不清零 `wrong_count`，仅在 mastery >= 80 时重置
- 一键复习 = 批量生成复习任务（`POST /word/review-batch`）
- 错词本接口需要 JOIN `word` 表获取中文释义

### 3.5 数据流整合

```
day_start/end 定时任务
    │
    ▼
check user_learning_streak → 更新连续天数
    │
    ▼
dashboard/stats 聚合:
  ├─ 从 learning_streak 取本周数据
  ├─ 从 word_record 取掌握率
  ├─ 从 user_config 取目标
  └─ 从 word_record 取待复习
```

---

## 四、论坛敏感词过滤方案

### 4.1 现状

`ResultCode.CONTENT_SENSITIVE(6003)` 已定义，但实际发布帖子/回复时**未做敏感词校验**，需要补上。

### 4.2 两种方案对比

| 方案 | 复杂度 | 热更新 | 适用场景 |
|------|--------|--------|----------|
| **A: Nacos 配置中心** | 中 | ✅ `@RefreshScope` | 已有 Nacos，推荐 |
| **B: 数据库 `system_config` 表** | 低 | ✅ 可轮询 | 简单，无需 Nacos 依赖 |

### 4.3 推荐方案 A：Nacos 配置中心

#### 步骤 1：创建 Nacos 配置文件

在 Nacos 控制台（或 `nacos-config/forum-sensitive-words.yaml`）创建：

```yaml
# Data ID: forum-sensitive-words.yaml
# Group: ENSTUD
# 格式: yaml

forum:
  sensitive:
    enabled: true
    words:
      - "赌博"
      - "色情"
      - "暴力"
      - "毒品"
      - "赌博网站"
      - "代考"
      - "作弊"
      - "枪支"
      - "诈骗"
      - "反动"
    replacement: "***"
```

#### 步骤 2：forum-service 配置加载

```yaml
# application.yml
spring:
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_HOST:172.20.0.4}:${NACOS_PORT:8848}
        namespace: ''
        group: ENSTUD
        extension-configs:
          - data-id: forum-sensitive-words.yaml
            refresh: true   # 支持热更新
```

#### 步骤 3：敏感词检测组件

```java
@Component
@RefreshScope
@ConfigurationProperties(prefix = "forum.sensitive")
public class SensitiveWordFilter {

    private boolean enabled = true;

    private List<String> words = List.of();

    private String replacement = "***";

    // DFA 前缀树（编译时构建，热更新时重建）
    private final TrieNode root = new TrieNode();

    @PostConstruct
    public void init() {
        buildTrie();
    }

    /**
     * 检测是否包含敏感词
     * @return true = 安全（无敏感词）
     */
    public boolean check(String text) {
        if (!enabled || text == null || text.isBlank()) return true;
        return !containsSensitive(text);
    }

    /**
     * 替换敏感词
     */
    public String filter(String text) {
        if (!enabled || text == null || text.isBlank()) return text;
        return replaceSensitive(text);
    }

    /**
     * 构建 DFA 前缀树
     */
    private void buildTrie() { /* DFA 实现 */ }

    private boolean containsSensitive(String text) { /* 遍历匹配 */ }
    private String replaceSensitive(String text) { /* 替换 */ }

    // DFA 节点
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd = false;
    }
}
```

> **DFA 算法优势：** 时间复杂度 O(n)，与敏感词数量无关，适合论坛实时检测场景。

#### 步骤 4：在 Service 层集成

```java
// ForumServiceImpl.createPost() 修改
@Transactional(rollbackFor = Exception.class)
public PostDTO createPost(Long userId, CreatePostRequest request) {
    // 敏感词检测
    if (!sensitiveWordFilter.check(request.title())) {
        throw new BusinessException(ErrorCode.CONTENT_SENSITIVE);
    }
    if (!sensitiveWordFilter.check(request.content())) {
        throw new BusinessException(ErrorCode.CONTENT_SENSITIVE);
    }

    ForumCategory cat = categoryMapper.selectById(request.categoryId());
    if (cat == null) throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
    // ... 原创建逻辑
}

// createReply() 同理
```

### 4.4 降级方案 B：数据库

如果不想引入 Nacos Config 依赖：

```sql
CREATE TABLE IF NOT EXISTS `enstud_system_config` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `config_key`  VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT        NOT NULL COMMENT '配置值（JSON）',
    `description` VARCHAR(255) DEFAULT NULL,
    `updated_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';
```

```sql
INSERT INTO `enstud_system_config` (`config_key`, `config_value`, `description`) VALUES
('sensitive_words', '["赌博","色情","暴力","毒品","代考","作弊","诈骗","反动"]', '论坛敏感词列表'),
('sensitive_enabled', 'true', '是否开启敏感词过滤');
```

配合定时刷新：
```java
@Scheduled(fixedRate = 60_000)  // 每分钟重载
public void reloadSensitiveWords() {
    // 从 DB 读取
}
```

---

## 五、健康检查脚本 + CI 强化

### 5.1 健康检查脚本

```bash
#!/bin/bash
# scripts/health-check.sh
# 依次检查 8 个微服务的健康状态

SERVICES=(
    "网关:http://localhost:8080/actuator/health"
    "用户服务:http://localhost:8081/actuator/health"
    "单词服务:http://localhost:8082/actuator/health"
    "AI对话服务:http://localhost:8083/actuator/health"
    "写作服务:http://localhost:8084/actuator/health"
    "翻译服务:http://localhost:8085/actuator/health"
    "论坛服务:http://localhost:8086/actuator/health"
    "阅读服务:http://localhost:8087/actuator/health"
)

ALL_UP=true
for entry in "${SERVICES[@]}"; do
    name="${entry%%:*}"
    url="${entry#*:}"
    if curl -sf "$url" > /dev/null 2>&1; then
        echo "✅ $name ($url) — UP"
    else
        echo "❌ $name ($url) — DOWN"
        ALL_UP=false
    fi
done

echo ""
if [ "$ALL_UP" = true ]; then
    echo "🎉 所有服务运行正常"
    exit 0
else
    echo "⚠️  部分服务异常，请检查日志"
    exit 1
fi
```

### 5.2 各 Service 开启 Actuator

每个微服务的 `pom.xml` 确保有：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

`application.yml` 统一配置：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

### 5.3 GitHub Actions 集成

```yaml
# .github/workflows/health-check.yml
name: Health Check

on:
  schedule:
    - cron: '0 */6 * * *'  # 每6小时
  workflow_dispatch:        # 手动触发

jobs:
  health-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run health check
        run: chmod +x scripts/health-check.sh && ./scripts/health-check.sh
      - name: Slack notification on failure
        if: failure()
        uses: slackapi/slack-github-action@v1
        with:
          payload: '{"text":"⚠️ EnStudApp 部分服务异常！"}'
```

### 5.4 SM-2 算法单元测试

现有 `Sm2AlgorithmTest.java` 已覆盖以下场景：

| 测试用例 | 验证内容 | 状态 |
|----------|----------|------|
| `testFirstReview_PerfectQuality` | 首次完美回答 → interval=1, rep=1 | ✅ |
| `testFirstReview_Wrong` | 首次答错 → 重置 | ✅ |
| `testQualityOutOfRange` | 非法 quality 参数 → 抛异常 | ✅ |
| `testMultipleCorrectReviews_ReachesMastered` | 多次正确 → MASTERED 状态 | ✅ |
| `testReviewAfterWrong_ResetsMemoryLevel` | 答错后重置记忆等级 | ✅ |
| `testEaseFactorNeverBelowMinimum` | EF 不低于 1.3 | ✅ |
| `testMemoryLevelProgressesCorrectly` | 记忆等级逐步提升 | ✅ |

**建议补充：**

```java
@Test
void testReviewIntervalIncreasesGeometrically() {
    // 验证间隔递增：1 → 6 → 15+ 天
    UserWordRecord r = createRecord();
    int[] expected = {1, 6, 15, 38, 95};
    for (int exp : expected) {
        Sm2Algorithm.update(r, 5);
        assertEquals(exp, r.getReviewInterval());
    }
}
```

#### Maven 执行测试

```bash
mvn test -pl enstud-word-service -Dtest=Sm2AlgorithmTest
```

CI 中的 `mvn test` 会默认跑所有单元测试。

---

## 六、实施路线图

### 优先级 & 工作量估算

| 功能 | 优先级 | 预估工时 | 依赖 |
|------|--------|----------|------|
| **论坛敏感词过滤** | 🔴 P0 | 0.5 天 | Nacos / 数据库 |
| **SM-2 测试增强** | 🔴 P0 | 0.5 天 | 无 |
| **健康检查脚本 + CI** | 🟡 P1 | 0.5 天 | 无 |
| **阅读选中查词** | 🟠 P1 | 2-3 天 | translate-service 扩展 |
| **首页仪表盘** | 🟢 P2 | 2-3 天 | `learning_streak` 表 |
| **学习热力图** | 🟢 P2 | 1 天 | 仪表盘完成后 |
| **目标管理** | 🟢 P2 | 1 天 | 仪表盘完成后 |
| **错词本** | 🟢 P2 | 1-2 天 | word-service 扩展 |

### 建议执行顺序

```
Phase 1（基础设施加固）:
  ├── 论坛敏感词过滤（防止违规内容）
  ├── 健康检查脚本（便于排查宕机）
  └── SM-2 测试补充（保障核心算法）

Phase 2（核心体验提升）:
  ├── 阅读选中查词（提升阅读体验）
  └── 首页仪表盘（提升首次进入体验）

Phase 3（学习闭环完善）:
  ├── 学习热力图
  ├── 目标管理
  └── 错词本
```
