# EnStudApp 编码开发规范

> **所有开发任务必须严格遵守本规范，这是硬性要求，不是建议。**

---

## 一、项目分层架构

### 1.1 三层架构

每个微服务必须遵循 **Controller → Service → Mapper** 三层架构，禁止跨层调用：

```
Controller（API 入口，参数校验，调用 Service）
    ↓ 只能调用
Service（业务逻辑，事务管理，调用 Mapper）
    ↓ 只能调用
Mapper（数据访问，SQL 操作）
```

### 1.2 各层职责

| 层级 | 职责 | 禁止事项 |
|------|------|---------|
| **Controller** | 接收请求、参数校验、调用 Service、返回 Result | 禁止写业务逻辑、禁止直接注入 Mapper |
| **Service** | 业务逻辑、事务管理、数据组装 | 禁止处理 HTTP 协议相关内容 |
| **Mapper** | 数据库 CRUD 操作 | 禁止写业务逻辑 |

### 1.3 包结构约定

每个微服务统一包结构：

```
com.enstud.{service}/
├── controller/       # REST 控制器
├── service/          # Service 接口
│   └── impl/         # Service 实现
├── mapper/           # MyBatis Mapper 接口
├── config/           # 服务配置类
├── dto/              # 服务内部 DTO（请求/响应）
└── algorithm/        # 算法（仅 word-service 等需要的服务）
```

公共模块包结构（`enstud-common`）：

```
com.enstud.common/
├── entity/           # 全部数据库实体（@TableName）
├── dto/              # 跨服务共享 DTO
├── feign/            # OpenFeign 客户端接口
├── constant/         # 常量定义
├── enums/            # 枚举定义
├── exception/        # 异常类
├── util/             # 工具类
└── config/           # 公共配置类
```

---

## 二、后端编码规范

### 2.10 代码注释规范

**所有公共类、接口、公共方法必须添加 Javadoc 注释。**

#### 类 / 接口注释

```java
/**
 * Ai 对话客户端抽象接口
 * <p>
 * 支持切换不同的 AI 后端（OpenAI / 文心一言 / 通义千问 / Mock）
 */
public interface AiChatClient { ... }
```

#### 方法注释

```java
/**
 * 发送消息并获取 AI 回复
 *
 * @param request 对话请求，包含场景、历史消息和当前消息
 * @return AI 响应，包含回复文本和语法问题列表
 */
AiResponse chat(AiRequest request);
```

#### 行内注释

```java
// 构建对话历史（将数据库实体转换为 AI 请求格式）
List<AiRequest.Message> aiHistory = ...

// 调用 AI 前先检查用户配额，避免滥用
if (userQuota <= 0) { ... }
```

#### 禁止事项

| ❌ 禁止 | 说明 |
|---------|------|
| 无意义注释 | `// 设置用户名` 配 `user.setName()`，不言自明的方法不需要注释 |
| 过期注释 | 代码已修改但注释未同步更新 |
| 注释掉的代码提交到 Git | 使用版本管理，删除的代码通过 Git 找回 |
| 只写中文或只写英文 | 统一使用**简体中文**；专有名词保留英文 |
| 在注释里写业务逻辑 | 注释只解释"为什么"，不重复"做什么" |

#### 注释覆盖要求

| 元素 | 要求 |
|------|------|
| 公共类 / 接口 | 必须：功能说明，核心职责 |
| 公共方法 | 必须：功能说明，@param，@return，@throws（如有） |
| 私有方法 | 建议：复杂逻辑才需要 |
| 算法 / 复杂业务逻辑 | 必须：行内注释说明意图 |
| 魔法数字 | 必须：说明含义，或提取为命名常量 |
| 配置类 / 配置属性 | 必须：说明用途和默认值 |

---

### 2.1 依赖注入

**必须使用构造器注入，禁止 @Autowired 字段注入：**

```java
// ✅ 正确：构造器注入
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final RedissonClient redissonClient;
}

// ❌ 错误：字段注入
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
}
```

### 2.2 统一响应格式

所有 API 必须返回 `Result<T>`，成功码为 `0`：

```java
// ✅ 正确
@GetMapping("/{id}")
public Result<UserDTO> getUser(@PathVariable Long id) {
    return Result.success(userService.getById(id));
}

// ❌ 错误：直接返回实体
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) {
    return userService.getById(id);
}
```

Result 类定义（位于 enstud-common）：

```java
@Data
public class Result<T> {
    private int code;    // 0=成功，非0=错误码
    private String msg;  // 提示信息
    private T data;      // 数据

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail(int code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
```

### 2.3 DTO 与实体分离

**禁止直接返回数据库实体类，必须使用 DTO：**

```java
// ✅ 正确：Controller 返回 DTO
@GetMapping("/profile")
public Result<UserProfileDTO> getProfile() {
    Long userId = SecurityContext.getCurrentUserId();
    return Result.success(userService.getProfile(userId));
}

// ❌ 错误：直接返回 Entity
@GetMapping("/profile")
public Result<User> getProfile() {
    return Result.success(userMapper.selectById(userId));
}
```

### 2.4 参数校验

**入参使用 Java Record + Jakarta Validation：**

```java
// ✅ 正确
public record LoginRequest(
    @NotBlank(message = "用户名不能为空") String username,
    @NotBlank(message = "密码不能为空") String password
) {}

// ✅ 正确：Controller 中使用 @Valid
@PostMapping("/login")
public Result<LoginDTO> login(@Valid @RequestBody LoginRequest request) { ... }

// ❌ 错误：手动 if-else 校验
@PostMapping("/login")
public Result<LoginDTO> login(@RequestBody LoginRequest request) {
    if (request.username() == null || request.username().isEmpty()) {
        return Result.fail(400, "用户名不能为空");
    }
}
```

### 2.5 异常处理

**业务异常使用 BusinessException，全局统一拦截：**

```java
// ✅ 正确：Service 层抛出 BusinessException
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    public UserProfileDTO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // ...
    }
}

// ❌ 错误：Controller 中直接 try-catch
@PostMapping("/login")
public Result<LoginDTO> login(@RequestBody LoginRequest request) {
    try {
        return Result.success(userService.login(request));
    } catch (Exception e) {
        return Result.fail(500, e.getMessage());
    }
}
```

### 2.6 事务管理

**事务声明在 Service 层，必须指定 rollbackFor：**

```java
// ✅ 正确
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        // 多步数据库操作
    }
}
```

### 2.7 API 文档注解

**Controller 必须添加 Swagger 注解：**

```java
@Tag(name = "用户管理", description = "用户注册、登录、个人信息")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginDTO> login(@Valid @RequestBody LoginRequest request) { ... }
}
```

### 2.8 日志规范

**使用 @Slf4j + 参数化日志，禁止字符串拼接：**

```java
// ✅ 正确
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    public UserProfileDTO getProfile(Long userId) {
        log.info("获取用户信息, userId={}", userId);
        // ...
    }
}

// ❌ 错误
log.info("获取用户信息, userId=" + userId);
```

日志级别约定：

| 级别 | 使用场景 |
|------|---------|
| **ERROR** | 系统异常、需要告警的问题 |
| **WARN** | 业务异常、需要注意的情况 |
| **INFO** | 关键业务操作（登录、注册、支付等） |
| **DEBUG** | 调试信息（生产环境不输出） |

### 2.9 数据库命名规范

**表名和字段名使用 snake_case：**

```sql
-- 表名：小写下划线，带业务前缀
CREATE TABLE enstud_user (...);
CREATE TABLE enstud_word (...);
CREATE TABLE enstud_user_word_record (...);

-- 字段名：小写下划线
user_id, created_at, updated_at, is_deleted, mastery_level
```

**通用字段：**
- `id` — BIGINT 主键，自增
- `created_at` — 创建时间，DATETIME
- `updated_at` — 更新时间，DATETIME ON UPDATE
- `is_deleted` — 逻辑删除标记，TINYINT DEFAULT 0

---

## 三、前端编码规范

### 3.1 TypeScript 严格模式

**禁止使用 `any` 类型，必须定义明确的类型：**

```typescript
// ✅ 正确
interface LoginParams {
  username: string;
  password: string;
}

// ❌ 错误
function login(params: any) { ... }
```

### 3.2 目录约定

```
client/src/
├── apis/              # API 请求（按模块拆分文件）
│   ├── user.ts        # 用户相关 API
│   ├── word.ts        # 单词相关 API
│   └── ...
├── pages/             # 页面组件（按业务模块拆分目录）
│   ├── home/
│   │   └── index.tsx
│   ├── word/
│   │   ├── index.tsx
│   │   └── components/
│   └── ...
├── components/        # 全局通用组件
├── hooks/             # 自定义 Hooks
├── stores/            # Zustand 状态管理
├── types/             # 全局 TypeScript 类型定义
├── utils/             # 工具函数
├── config/            # 配置常量
├── styles/            # 全局样式/变量
└── assets/            # 静态资源
```

### 3.3 组件规范

```typescript
// ✅ 正确：函数式组件 + Props 接口
interface WordCardProps {
  word: string;
  phonetic: string;
  definition: string;
  onFlip: () => void;
}

const WordCard: React.FC<WordCardProps> = ({ word, phonetic, definition, onFlip }) => {
  return (
    <div className="word-card">
      <h3>{word}</h3>
      <p className="phonetic">[{phonetic}]</p>
      <p className="definition">{definition}</p>
    </div>
  );
};
```

### 3.4 API 请求规范

```typescript
// ✅ 正确：统一封装 API 请求
// apis/user.ts
import request from '@/utils/request';

export function login(params: LoginParams) {
  return request.post<LoginResult>('/user/login', params);
}

// ✅ 正确：在页面中使用
import { login } from '@/apis/user';

const handleLogin = async () => {
  const res = await login({ username, password });
  if (res.code === 0) {
    // 处理登录成功
  }
};
```

### 3.5 状态管理

**使用 Zustand 进行状态管理：**

```typescript
// ✅ 正确
import { create } from 'zustand';

interface UserStore {
  userInfo: UserInfo | null;
  token: string | null;
  setUserInfo: (info: UserInfo) => void;
  logout: () => void;
}

export const useUserStore = create<UserStore>((set) => ({
  userInfo: null,
  token: localStorage.getItem('token'),
  setUserInfo: (info) => set({ userInfo: info }),
  logout: () => {
    set({ userInfo: null, token: null });
    localStorage.removeItem('token');
  },
}));
```

### 3.6 命名规范

| 元素 | 规范 | 示例 |
|------|------|------|
| 组件文件 | PascalCase | `WordCard.tsx` |
| 工具文件 | camelCase | `formatDate.ts` |
| 样式文件 | kebab-case | `word-card.module.css` |
| 常量 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 接口 | PascalCase + 后缀 | `UserInfo`, `LoginParams` |
| 函数 | camelCase | `handleLogin`, `fetchWordList` |
| 组件 | PascalCase | `WordCard`, `ChatWindow` |
| CSS 类名 | kebab-case / BEM | `word-card`, `word-card__title` |

---

## 四、Git 工作流规范

### 4.1 分支策略

| 分支 | 用途 | 保护规则 |
|------|------|---------|
| `main` | 生产环境代码 | 禁止直接推送，必须通过 PR |
| `develop` | 开发集成分支 | 禁止 force push |
| `feature/*` | 功能开发分支 | — |
| `hotfix/*` | 紧急修复分支 | — |

### 4.2 Commit 规范

使用 Conventional Commits 格式：

```
<type>(<scope>): <subject>

类型：
- feat:     新功能
- fix:      Bug 修复
- docs:     文档变更
- style:    代码格式（不影响逻辑）
- refactor: 重构（非新功能、非 Bug 修复）
- perf:     性能优化
- test:     测试相关
- chore:    构建/工具链变更

示例：
feat(word): 实现艾宾浩斯记忆曲线复习算法
fix(chat): 修复 AI 对话流式响应中断问题
docs(api):  更新用户服务 API 文档
```

### 4.3 Code Review

- 所有合并到 `develop` 的代码必须通过 Code Review
- 至少 1 人 Review 通过
- CI 检查（编译、Lint、测试）全部通过

---

## 五、数据库规范

### 5.1 迁移脚本管理

所有数据库变更必须通过 SQL 脚本，存放在 `sql/` 目录：

```
sql/
├── init/                          # 初始化脚本（建表、初始数据）
│   ├── 01_create_user_tables.sql
│   ├── 02_create_word_tables.sql
│   └── 03_create_forum_tables.sql
├── migration/                     # 增量迁移脚本（按日期编号）
│   ├── 20260524_add_user_avatar.sql
│   └── 20260525_create_wordbook.sql
```

### 5.2 SQL 编码规范

```sql
-- ✅ 正确
SELECT id, username, email
FROM enstud_user
WHERE is_deleted = 0
  AND status = 1
ORDER BY created_at DESC
LIMIT 20;

-- ❌ 错误：SELECT *
SELECT * FROM enstud_user;

-- ❌ 错误：字符串拼接
SELECT * FROM enstud_user WHERE username = '${username}';
```

---

## 六、安全规范

### 6.1 禁止事项

- ❌ 硬编码密码、密钥、Token（必须使用环境变量或 Nacos 配置）
- ❌ SQL 拼接（必须使用参数绑定）
- ❌ 在日志中打印用户密码、Token 等敏感信息
- ❌ 前端存储敏感信息在 localStorage（Token 除外，需设置过期时间）

### 6.2 认证与授权

- JWT Token 存储在 Redis 中，支持主动失效
- Token 有效期：Access Token 2 小时，Refresh Token 7 天
- API 鉴权通过 Gateway 统一处理

---

## 七、检查清单

每次提交代码前，对照以下清单自检：

### 后端

- [ ] 编译通过：`mvn clean compile`
- [ ] 单元测试通过：`mvn test`
- [ ] Controller 使用 `@RequiredArgsConstructor` 注入
- [ ] API 返回 `Result<T>`，不直接返回 Entity
- [ ] 入参使用 `@Valid` 校验
- [ ] 业务异常使用 `BusinessException`
- [ ] 日志使用 `@Slf4j` 参数化格式
- [ ] 添加了 `@Tag` / `@Operation` 注解
- [ ] 事务声明了 `rollbackFor = Exception.class`
- [ ] 没有硬编码密钥/密码

### 前端

- [ ] Lint 通过：`npm run lint`
- [ ] 没有 `any` 类型
- [ ] 组件有 Props 类型定义
- [ ] API 请求有错误处理
- [ ] 组件和文件命名符合规范
- [ ] 新页面已添加到路由配置

### 通用

- [ ] Commit 信息符合 Conventional Commits 规范
- [ ] 数据库变更脚本已添加到 `sql/` 目录
- [ ] 更新了相关的 API 文档
