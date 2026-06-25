# AI 面试刷题与简历优化平台

这是一个基于 Spring Boot 的课程设计项目，支持 IDEA 右上角绿色三角形直接运行。

## 1. 运行方式

### 推荐方式：IDEA 右上角三角形运行

1. 用 IDEA 打开本项目中包含 `pom.xml` 的目录。
2. 等右下角 Maven 依赖下载完成。
3. 右上角运行配置选择 `AiInterviewPlatformApplication`。
4. 点击绿色三角形运行。
5. 浏览器访问：`http://localhost:8080`

如果右上角没有出现配置：

1. 打开 `src/main/java/com/hrq/aiinterview/AiInterviewPlatformApplication.java`
2. 点击 `main` 方法左边绿色三角形。
3. 成功运行一次后，右上角就会自动出现运行配置。

## 2. 默认账号

| 角色 | 用户名 | 密码 |
|---|---|---|
| 管理员 | admin | 123456 |
| 普通用户 | user | 123456 |

## 3. Redis 说明

本版本默认启用 Redis 缓存，用于缓存热门面试题。

`application.yml` 中默认配置：

```yml
app:
  cache:
    use-redis: true
```

启动 Redis：

```bash
docker compose up -d
```

测试 Redis：

```bash
docker exec -it ai-interview-redis redis-cli ping
```

返回 `PONG` 说明 Redis 正常。

如果临时不想使用 Redis，可以把 `use-redis` 改成 `false`，系统会使用本地内存缓存热门题目。

## 4. docx 简历上传说明

简历优化模块已经支持上传 `.docx` 格式 Word 简历。

处理流程：

1. 用户在“简历优化管理”中新增简历。
2. 选择 `.docx` 文件上传。
3. 系统保存 Word 文件到 `uploads` 目录。
4. 后端使用 Apache POI 解析 docx 文本。
5. 提取出的文本会自动写入“项目经历”字段，供 AI 模拟分析使用。

注意：当前只允许上传 `.docx`，不支持旧版 `.doc`。

## 5. 项目功能

- 登录认证与权限控制：Spring Security，管理员和普通用户角色。
- 用户管理：管理员可以管理用户信息。
- 面试题库：题目新增、修改、删除、分页查询、热门题展示。
- 简历优化：简历录入、docx Word 文件上传、AI 模拟分析建议。
- 数据看板：Echarts 展示岗位题库分布。
- 缓存设计：热门题目支持 Redis 缓存，也支持本地内存降级。
- 数据库：默认使用 H2 文件数据库，无需手动安装 MySQL。

## 6. 常见问题

### 右上角没有绿色运行按钮

打开 `AiInterviewPlatformApplication.java`，点击 `main` 方法左边绿色三角形运行一次。

### `mvn` 不是内部或外部命令

这是因为电脑没有配置 Maven 环境变量，不影响 IDEA 运行。IDEA 自带 Maven，可以直接用绿色三角形运行。

### Lombok 标红

项目使用 Lombok。IDEA 中开启：

`文件 -> 设置 -> 构建、执行、部署 -> 编译器 -> 注解处理器 -> 启用注解处理`

然后刷新 Maven。

### 上传 docx 后没有文字

请确认上传的是 `.docx`，不是旧版 `.doc`。旧版 `.doc` 不是 Office Open XML 格式，本项目不解析。


## 面试题库扩展说明

本版本新增 `question_bank` 标准题库表，并将题库列表、首页统计、数据看板和高频题缓存切换到新表。

核心能力：

- 支持题库分页查询。
- 支持按 `category` 题目分类筛选。
- 支持按 `job_type` 岗位方向筛选。
- 支持按 `difficulty` 难度筛选。
- 支持对题目标题、答案、标签进行关键词搜索。
- 支持新增、编辑、删除、查看答案。
- 初始化 SQL 内置 90 道面试题，覆盖 Java、Spring Boot、MySQL、Redis、软件测试、前端基础、算法、项目面试、HR 面试。
- 首页增加题库总数和软件测试题目数量展示。

MySQL 初始化脚本：`src/main/resources/db/mysql.sql`

如果已有旧库，建议先备份数据，再执行 `mysql.sql` 重建表结构。
