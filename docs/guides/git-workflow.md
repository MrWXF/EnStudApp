# Git 工作流规范

> 统一的 Git 工作流，确保代码质量和协作效率

## 分支策略

### 主分支

- `main` - 生产环境分支，始终保持稳定状态
- `develop` - 开发环境分支（可选），集成测试用

### 功能分支

| 分支类型 | 命名规范 | 说明 | 示例 |
|---------|---------|------|------|
| 功能分支 | `feature/xxx` | 新功能开发 | `feature/user-login` |
| 修复分支 | `fix/xxx` | Bug 修复 | `fix/word-memory-algorithm` |
| 热修复分支 | `hotfix/xxx` | 生产环境紧急修复 | `hotfix/jwt-expiration` |
| 重构分支 | `refactor/xxx` | 代码重构 | `refactor/user-service` |
| 文档分支 | `docs/xxx` | 文档更新 | `docs/api-guide` |
| 测试分支 | `test/xxx` | 测试相关 | `test/e2e-automation` |

### 分支命名规则

- 使用小写字母和连字符
- 简洁明了，描述性功能
- 示例：
  - ✅ `feature/chat-ai-integration`
  - ✅ `fix/login-token-expiration`
  - ❌ `feature/Feature_Chat_AI`（避免大写和下划线）
  - ❌ `fix/bug`（过于模糊）

## Commit 规范

### 格式

采用 [Conventional Commits](https://www.conventionalcommits.org/) 规范，使用中文描述：

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

### Type 类型

| Type | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat(user): 添加用户注册功能` |
| `fix` | Bug 修复 | `fix(word): 修复记忆算法错误` |
| `docs` | 文档更新 | `docs(readme): 更新安装说明` |
| `style` | 代码格式（不影响功能） | `style: 统一代码缩进` |
| `refactor` | 重构（非新功能、非修复） | `refactor(user): 优化用户查询逻辑` |
| `perf` | 性能优化 | `perf(word): 优化单词查询性能` |
| `test` | 测试相关 | `test(user): 添加登录接口单元测试` |
| `chore` | 构建/工具/依赖变动 | `chore: 升级 Spring Boot 版本` |
| `ci` | CI/CD 配置变动 | `ci: 更新 GitHub Actions 配置` |
| `revert` | 回滚提交 | `revert: 回滚 feat(user): 添加用户注册功能` |

### Scope 范围（可选）

指定受影响的功能模块：

- `user` - 用户服务
- `word` - 单词服务
- `chat` - 聊天服务
- `writing` - 写作服务
- `translate` - 翻译服务
- `forum` - 论坛服务
- `read` - 阅读服务
- `gateway` - API 网关
- `frontend` - 前端
- `docker` - Docker 配置
- `db` - 数据库相关

### Subject 主题

- 使用中文描述
- 简洁明了，不超过 50 个字符
- 不以句号结尾
- 使用祈使语气（如"添加"而非"添加了"）

### 示例

```bash
# 好 ✅
feat(user): 添加用户注册功能
fix(word): 修复记忆算法导致的重复推荐
docs(api): 更新 API 文档
refactor(chat): 重构聊天消息处理逻辑
perf(forum): 优化帖子列表查询性能
test(user): 添加登录接口单元测试
chore(docker): 升级 MySQL 到 8.0

# 不好 ❌
update code                    # 过于模糊
fix bug                        # 没有说明具体修复什么
feat(user): 添加了一个新的用户注册功能，用户可以通过邮箱注册账号  # 太长
```

## 开发流程

### 1. 创建功能分支

```bash
# 确保在主分支
git checkout main

# 拉取最新代码
git pull origin main

# 创建功能分支
git checkout -b feature/user-login
```

### 2. 开发并提交

```bash
# 查看修改
git status

# 添加修改
git add .

# 提交（遵循 commit 规范）
git commit -m "feat(user): 添加用户登录功能"
```

### 3. 保持分支同步

```bash
# 定期合并主分支最新代码
git checkout main
git pull origin main
git checkout feature/user-login
git merge main
```

### 4. 推送分支

```bash
# 首次推送
git push -u origin feature/user-login

# 后续推送
git push
```

### 5. 创建 Pull Request

1. 访问 GitHub 仓库
2. 点击 "Compare & pull request"
3. 填写 PR 描述（见下方模板）
4. 指定 Reviewer
5. 等待 CI 检查通过
6. 等待 Code Review 通过
7. 合并 PR

## Pull Request 模板

```markdown
## 变更说明

简要描述本次 PR 的功能或修复的问题

## 变更类型

- [ ] 新功能（feat）
- [ ] Bug 修复（fix）
- [ ] 文档更新（docs）
- [ ] 代码重构（refactor）
- [ ] 性能优化（perf）
- [ ] 测试相关（test）
- [ ] 其他（chore）

## 影响范围

列出受影响的功能模块

## 测试情况

- [ ] 本地测试通过
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 手动验证通过

## 检查清单

- [ ] 代码符合编码规范
- [ ] 注释完整清晰
- [ ] API 文档已更新（如有）
- [ ] 数据库变更已提交（如有）
- [ ] 无硬编码的密钥/密码
- [ ] 日志输出合理
- [ ] 异常处理完善

## 截图（如有 UI 变更）

![截图描述](截图链接)

## 相关 Issue

Closes #123
```

## Code Review 规范

### Reviewer 职责

1. **及时响应** - 收到 Review 请求后 24 小时内完成审查
2. **全面审查** - 检查代码质量、功能正确性、安全性
3. **建设性反馈** - 提供具体的改进建议，而非仅指出问题
4. **友好沟通** - 保持专业、尊重的态度

### 审查重点

- ✅ 功能是否符合需求
- ✅ 代码是否符合编码规范
- ✅ 逻辑是否正确，边界情况是否处理
- ✅ 性能是否合理
- ✅ 安全性（SQL 注入、XSS 等）
- ✅ 测试覆盖是否充分
- ✅ 文档是否完整

### 审查意见分级

- 🔴 **Must Fix** - 必须修复，阻塞合并
- 🟡 **Should Fix** - 建议修复，可后续处理
- 🟢 **Nice to Have** - 可选优化
- 💬 **Question** - 需要讨论

## CI/CD 检查

所有 PR 必须通过以下 CI 检查：

### 后端检查

```bash
# 编译检查
mvn clean compile

# 单元测试
mvn test

# 代码格式检查（如有配置）
mvn spotless:check
```

### 前端检查

```bash
# 安装依赖
npm install

# Lint 检查
npm run lint

# 类型检查
npm run build  # 包含 tsc 检查

# 单元测试
npm run test
```

### 自动化流程

PR 提交后，GitHub Actions 会自动执行：

1. 代码编译
2. 单元测试
3. 代码格式检查
4. 安全扫描
5. 构建 Docker 镜像（可选）

**所有检查必须通过才能合并。**

## 合并策略

### 合并方式

推荐使用 **Squash and Merge**：

- 将多个提交压缩为一个
- 保持主分支历史清晰
- 便于回滚

### 合并后

```bash
# 删除远程分支
git push origin --delete feature/user-login

# 删除本地分支
git branch -d feature/user-login

# 更新主分支
git checkout main
git pull origin main
```

## 标签管理

发布版本时使用语义化版本标签：

```bash
# 创建标签
git tag -a v1.0.0 -m "Release version 1.0.0"

# 推送标签
git push origin v1.0.0
```

### 版本号规范

- `MAJOR.MINOR.PATCH`（主版本.次版本.补丁版本）
- `MAJOR` - 不兼容的 API 变更
- `MINOR` - 向下兼容的功能新增
- `PATCH` - 向下兼容的 Bug 修复

## 常见问题

### Q: 如何处理冲突？

```bash
# 1. 拉取最新主分支
git checkout main
git pull origin main

# 2. 合并到功能分支
git checkout feature/user-login
git merge main

# 3. 解决冲突
# 编辑冲突文件，手动解决冲突

# 4. 提交解决
git add .
git commit -m "merge: 解决合并冲突"

# 5. 推送
git push
```

### Q: 如何回滚提交？

```bash
# 回滚最近的提交（保留修改）
git reset --soft HEAD~1

# 回滚到某个提交（保留修改）
git reset --soft <commit-hash>

# 回滚并丢弃修改
git reset --hard HEAD~1
```

### Q: 如何修改最近的 commit message？

```bash
git commit --amend -m "新的 commit message"
```

## 相关文档

- [编码规范](./coding-standards.md)
- [CI/CD 流程](../deployment/cicd.md)
- [GitHub Actions 文档](https://docs.github.com/en/actions)
