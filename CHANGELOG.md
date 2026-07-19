# Changelog

All notable changes to EnStudApp will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- JaCoCo 代码覆盖率检查（行覆盖率 ≥ 50%）
- OWASP Dependency Check 依赖漏洞扫描
- Checkstyle Java 静态代码分析
- Pre-commit hooks（Husky + lint-staged + Prettier）
- CD 投产发布流水线（GitHub Actions + GHCR + SSH 部署）
- 容器健康检查（Docker HEALTHCHECK）
- 生产环境 docker-compose 覆盖配置
- 部署/回滚脚本

### Changed
- 前端 Lint CI 从静默失败改为硬阻断

## [1.0.0] — 2026-05-xx

### Added
- 用户服务：注册、登录、JWT 认证、个人信息管理、积分等级
- 单词服务：词库管理、SM-2 记忆算法、学习记录、复习计划
- AI 对话服务：英语日常对话、语法纠正、对话历史
- 写作服务：作文提交、AI 智能批改、范文推荐
- 翻译服务：文本翻译、语音翻译、图片翻译
- 论坛服务：帖子发布、回复、点赞、搜索
- 阅读服务：热门英文文章聚合
- API 网关：Spring Cloud Gateway + JWT 统一鉴权
- 前端：React + TypeScript + Ant Design Mobile
- CI 流水线：后端编译测试 + 前端构建 Lint + 集成冒烟测试
