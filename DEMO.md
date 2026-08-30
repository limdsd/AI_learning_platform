# 演示脚本(录屏 / 现场演示用)

> 目标:5~8 分钟内,把「登录 → AI 出题 → 智能刷题 → 错题本 → AI 组卷 → 考试 → 成绩报告」完整走一遍。
> 提前 10 分钟把环境起好,录屏时只演示,不停顿调环境。

---

## 一、录屏前准备(一次性)

```bash
# 1. 启动 MySQL(单独一个终端,别关)
D:\mysql-8.0.26-winx64\mysql-8.0.26-winx64\bin\mysqld.exe --console

# 2. 启动后端(IDEA 跑 DemoApplication,Active profiles 选 local;或命令行)
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# 3. 启动前端(另开一个终端)
cd frontend
npm run dev
```

确认三件事:
- [ ] 后端 http://localhost:8081 已启动(日志无报错)
- [ ] 前端 http://localhost:5173 能打开
- [ ] 已配置 DeepSeek Key(AI 功能需要)

---

## 二、演示流程(按顺序,边点边讲)

### 第 1 步:注册 / 登录(30 秒)
1. 打开 http://localhost:5173,进入登录页
2. 点「注册」,填用户名/昵称/密码,注册成功
3. 登录进入主页

**讲解词:**「平台有用户体系,用 JWT 做无状态认证,密码 BCrypt 加密存储。」

---

### 第 2 步:AI 智能出题(1 分钟)⭐核心
1. 进入「智能出题」页
2. 填:知识点=`Java 基础`、题型=`单选题`、难度=`简单`、数量=`3`
3. 点「生成」,展示 AI 生成的 3 道题(题干 + 选项 + 答案 + 解析)

**讲解词:**「这里我不用任何 AI 框架,直接用 RestClient 调 DeepSeek 的 OpenAI 兼容接口,强制 JSON 输出,解析后批量入库,标记来源是 AI。」

---

### 第 3 步:智能刷题(1.5 分钟)⭐核心
1. 进入「刷题练习」页
2. 抽题 → 选一个答案 → 点「提交」
3. 界面立刻显示对错(客观题本地判分,零延迟)
4. 点「AI 解析」,弹出 DeepSeek 给出的详细解析
5. 故意答错一题,看到它自动进了「错题本」

**讲解词:**「客观题用本地规则判分,不用调 AI,省钱又快;答错的题自动进错题本;点 AI 解析才调大模型生成逐题讲解。」

---

### 第 4 步:错题本 + 个性化推荐(1 分钟)
1. 进「错题本」,看到刚才答错的题
2. 点「智能推荐」,系统根据错题归纳出薄弱知识点,推荐同类题

**讲解词:**「我把错题按知识点分组统计,找出薄弱点,再从题库抽对应题目推荐,形成『错题 → 知识点 → 推荐』的闭环。」

---

### 第 5 步:AI 组卷 + 智能考试(2 分钟)⭐核心
1. 进「考试列表」,点「AI 组卷」
2. 填:名称=`Java 期末考试`、时长=`30 分钟`、知识点=`Java`、题量=`3`
3. 进入考试作答页(有倒计时)
4. 依次作答,提交
5. 跳转到「成绩报告」:总分、逐题对错、正确答案、解析一目了然

**讲解词:**「考试场景支持 AI 组卷、倒计时作答、交卷后客观题自动判分;如果卷子里有主观题,会交给 DeepSeek 判分并生成评语。」

---

### 第 6 步:收尾(20 秒)
回到主页,点一下「题库管理」,展示 AI 生成的题目已经落库、可以分页筛选。

**讲解词:**「所有 AI 生成的题目都会落库,可以复用、检索、手动编辑,不是一次性生成。」

---

## 三、备选:纯接口演示(不用前端,直接 curl)

适合录「后端 API」部分的视频,或面试官想快速看效果。用 Git Bash 运行:

```bash
BASE=http://localhost:8081
TOKEN=$(curl -s -X POST $BASE/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser1","password":"123456"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')

# 1. AI 出题
curl -s -X POST $BASE/api/question/ai-generate \
  -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"knowledgePoint":"Java基础","type":"single","difficulty":"easy","count":3}'

# 2. 抽题刷题
curl -s "$BASE/api/practice/questions?count=3" -H "Authorization: Bearer $TOKEN"

# 3. 提交一题(替换 questionId 和 userAnswer)
curl -s -X POST $BASE/api/practice/submit \
  -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"questionId":0,"userAnswer":"A","costSeconds":12}'

# 4. 错题本 / 推荐
curl -s "$BASE/api/practice/wrong" -H "Authorization: Bearer $TOKEN"
curl -s "$BASE/api/practice/recommend" -H "Authorization: Bearer $TOKEN"

# 5. AI 组卷
curl -s -X POST $BASE/api/exam/generate \
  -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Java期末","durationMinutes":30,"knowledgePoint":"Java","type":"single","difficulty":"easy","count":3}'
```

> 说明:接口返回里 `data.id` 是试卷 id,交卷流程见下方完整示例。

---

## 四、完整考试链路(组卷 → 开考 → 交卷 → 报告)

```bash
BASE=http://localhost:8081
TOKEN=$(curl -s -X POST $BASE/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser1","password":"123456"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')

# 组卷(记下返回里的 data.id = 试卷ID)
curl -s -X POST $BASE/api/exam/generate \
  -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Java Exam","durationMinutes":30,"knowledgePoint":"Java","type":"single","difficulty":"easy","count":3}'
# 假设返回 examId=2094025625711734786,题目 id 见返回的 questions[].id

# 开考(记下返回里的 data.id = 记录ID)
curl -s -X POST $BASE/api/exam/2094025625711734786/start \
  -H "Authorization: Bearer $TOKEN"
# 假设返回 recordId=2094025717206282242

# 交卷(answers 里填 questionId 和你的答案)
curl -s -X POST $BASE/api/exam/2094025625711734786/submit \
  -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"recordId":2094025717206282242,"answers":[{"questionId":2094025625522991105,"userAnswer":"A"},{"questionId":2094025625585905665,"userAnswer":"B"},{"questionId":2094025625648820225,"userAnswer":"A"}]}'

# 成绩报告
curl -s $BASE/api/exam/record/2094025717206282242 -H "Authorization: Bearer $TOKEN"
```

> ⚠️ 上面的 examId / recordId / questionId 是我本次验证时生成的示例值,**你重新生成时要用自己返回里的 id**,别照抄。

---

## 五、演示注意点(避坑)

1. **别现场配环境**:所有服务提前起好,录屏只演示。
2. **AI 接口有延迟**:出题/解析/判分可能 2~5 秒,讲解词正好覆盖这个空档。
3. **提前准备好测试账号**:避免录到一半去注册。
4. **DeepSeek Key 别露出来**:录屏时别切到有 key 的配置文件/环境变量界面。
5. **若 AI 偶发失败**:从容说一句「AI 输出做了容错,失败会返回友好提示,不会崩」,重试一次即可,反而显得你考虑了边界情况。
