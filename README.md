# AI 智能学习平台

一个以 **AI 驱动为核心** 的现代化智能学习平台,前后端分离架构,融合三大核心学习场景,为学习者提供个性化、智能化的学习体验。项目从需求分析、数据库设计到前后端实现均为独立完成。

## ✨ 项目亮点

- **AI 直连**:不依赖 Spring AI / LangChain4j 等现成框架,用 Spring `RestClient` 手写调用 DeepSeek 的 OpenAI 兼容接口,轻量可控。
- **混合判分**:客观题(单选/多选/判断)本地规则判分、零成本;主观题(填空/简答)交给 DeepSeek 判分并生成评语。
- **个性化推荐**:基于错题本统计薄弱知识点,智能推荐针对性题目。
- **工程规范**:统一返回体、全局异常处理、参数校验、JWT 无状态认证、BCrypt 密码加密、逻辑删除、雪花 ID。

## 三大核心场景

| 场景 | 说明 |
|---|---|
| 🎯 **智能生成题目** | 输入知识点/题型/难度/数量,调用 DeepSeek 自动出题并入库 |
| ✍️ **智能刷题** | 按知识点/难度抽题、错题本、AI 智能解析、基于薄弱点的个性化推荐 |
| 📝 **智能考试** | AI 组卷、在线作答/倒计时/自动交卷、客观题自动判分 + 主观题 AI 判分 |

## 技术栈

- **后端**:Spring Boot 4.1.1 (Java 21) + MyBatis-Plus 3.5.16 + MySQL 8 + JWT
- **前端**:Vue 3 + Vite + Element Plus + Pinia + Vue Router + Axios
- **AI**:DeepSeek(OpenAI 兼容接口,直连 `https://api.deepseek.com`)

## 系统架构

```
┌─────────────┐      HTTP/JSON       ┌──────────────────────────────┐
│   前端 Vue3  │  ───────────────▶   │        后端 Spring Boot 4      │
│  Element Plus│      /api/*         │                              │
└─────────────┘                      │  Controller(接口层)           │
      │  Axios                       │      │                       │
      │  (代理到 :8081)              │  Service(业务层)              │
      │                              │      │                       │
      │                              │  Mapper(MyBatis-Plus 持久层) │
      │                              └──────┬───────────────┬───────┘
      │                                     │               │
      │                              ┌──────▼──────┐   ┌────▼─────────┐
      │                              │   MySQL 8   │   │   DeepSeek    │
      │                              │ (8 张表)     │   │ (出题/解析/判分)│
      │                              └─────────────┘   └──────────────┘
```

请求链路:前端 Axios 发请求 → `AuthInterceptor` 校验 JWT → Controller → Service → 需要 AI 时调 `DeepSeekClient`(强制 JSON 输出)→ `QuestionParser` 解析 → 入库/返回。

## 数据库设计(8 张表)

| 表 | 说明 | 关键字段 |
|---|---|---|
| `user` | 用户 | username, password(BCrypt), nickname |
| `question` | 题库 | type, content, options(JSON), answer, analysis, knowledge_point, difficulty, source(AI/manual) |
| `wrong_question` | 错题本 | user_id, question_id, wrong_count, mastered |
| `practice_record` | 刷题记录 | user_id, question_id, user_answer, correct, cost_seconds |
| `exam` | 试卷 | name, total_score, duration_minutes, create_type |
| `exam_question` | 试卷-题目关联(多对多) | exam_id, question_id, score, sort |
| `exam_record` | 考试记录 | user_id, exam_id, status, objective_score, subjective_score, total_score |
| `exam_answer` | 作答明细 | record_id, question_id, user_answer, correct, ai_score, ai_comment |

通用字段:`create_time`、`update_time`、`deleted`(逻辑删除)。

## 目录结构

```
demo/
├── backend/                 # 后端(Spring Boot)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/demo/
│       │   ├── ai/          # DeepSeekClient / PromptTemplate / QuestionParser
│       │   ├── security/    # JwtUtil / AuthInterceptor / UserContext
│       │   ├── config/      # MyBatis-Plus / CORS / 拦截器
│       │   ├── common/      # Result / 异常处理 / 分页
│       │   ├── entity/ mapper/ dto/ service/ controller/
│       └── resources/
│           ├── application.yml
│           └── db/schema.sql
└── frontend/                # 前端(Vue 3)
    └── src/
        ├── api/ stores/ router/ layout/ views/ utils/
```

## 快速开始

### 环境要求

- **JDK 21**(Spring Boot 4 硬性要求)
- **Node.js 18+**(推荐 20+)
- **MySQL 8**

### 1. 启动 MySQL

本项目 MySQL 未注册为 Windows 服务,需手动启动(单独开一个终端窗口,别关):

```bash
D:\mysql-8.0.26-winx64\mysql-8.0.26-winx64\bin\mysqld.exe --console
```

### 2. 初始化数据库

```bash
mysql -u root -p < backend/src/main/resources/db/schema.sql
```

脚本会自动创建 `learning_platform` 库和 8 张表。

### 3. 配置本地开发凭据(必填)

数据库密码、JWT 密钥、DeepSeek Key 这三项**只通过环境变量或本地配置文件提供,不写进代码**,避免泄露到仓库。二选一:

**方式 A —— application-local.yml(推荐,一次配置长期有效):**

项目已提供 `backend/src/main/resources/application-local.yml`(已被 `.gitignore` 忽略,**不会提交到仓库**),里面已预填好本地 MySQL 密码和 JWT 密钥。如要用 AI 功能,取消文件底部注释并填入 DeepSeek Key。

**方式 B —— IDEA 环境变量:**

`Run → Edit Configurations → Environment variables` 添加:

```
MYSQL_PASSWORD=123456
JWT_SECRET=一串至少48字符的随机密钥
DEEPSEEK_API_KEY=sk-你的key
```

> 数据库密码 / JWT 密钥缺失会导致后端无法启动;DeepSeek Key 不配时 AI 功能返回友好提示,基础功能不受影响。

### 4. 启动后端

**IDEA**:打开 `backend` 目录,运行 `DemoApplication`(JDK 选 21)。若用方式 A,记得把 **Active profiles** 设为 `local`。

**命令行**:

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

后端运行在 **http://localhost:8081**。

### 5. 启动前端

```bash
cd frontend
npm install      # 首次
npm run dev
```

前端运行在 **http://localhost:5173**,已配置 `/api` 代理到后端 8081。

## 使用流程

1. 打开 `http://localhost:5173`,注册并登录
2. **智能出题**:输入知识点 → AI 生成题目 → 自动入库
3. **智能刷题**:抽题作答 → 即时判分 → AI 解析 → 错题自动进错题本
4. **智能考试**:AI 组卷 → 开始考试(倒计时)→ 交卷 → 客观自动判分 + 主观 AI 判分 → 成绩报告

## 主要 API

- `POST /api/auth/register` / `POST /api/auth/login` — 注册/登录
- `POST /api/question/ai-generate` — AI 出题
- `GET /api/question/page` — 题库分页
- `GET /api/practice/questions` / `POST /api/practice/submit` — 刷题
- `GET /api/practice/wrong` / `POST /api/practice/ai-explain` / `GET /api/practice/recommend` — 错题/AI 解析/推荐
- `POST /api/exam/generate` — AI 组卷
- `POST /api/exam/{id}/start` / `POST /api/exam/{id}/submit` — 考试
- `GET /api/exam/record/{id}` — 成绩报告

## 截图

> 运行后把以下页面截图放到这里,便于展示:登录页、AI 出题结果、刷题作答、错题本、考试作答、成绩报告。

## 注意事项

- 后端默认端口为 **8081**(避免与本机其他项目冲突),前端代理已同步配置。
- DeepSeek 的 `deepseek-chat` 模型已于 2026-07-24 弃用,本项目默认 `deepseek-v4-pro`,可用 `DEEPSEEK_MODEL` 切换。
- MyBatis-Plus 在 Spring Boot 4 下必须使用 `mybatis-plus-spring-boot4-starter`(已在 pom 中配置)。
- 生产部署请务必通过环境变量设置 `DEEPSEEK_API_KEY` 与 `JWT_SECRET`,并修改数据库密码。
