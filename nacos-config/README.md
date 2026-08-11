# Nacos 配置目录

此目录用于存放 Nacos 配置导出文件，便于本地开发时批量导入。

当前项目配置通过以下方式管理：
- `.env` — 环境变量（数据库密码、API Key 等敏感信息）
- `application.yml` — 各服务配置
- `application-common.yml` — 公共配置（所有服务共享）

## 何时使用此目录

当你需要：
1. 备份 Nacos 中的配置
2. 在多台机器间同步 Nacos 配置
3. 离线导入配置到新的 Nacos 实例

可以将 Nacos 配置导出为 YAML 文件放在此目录。
