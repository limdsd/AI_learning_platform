package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.ai.DeepSeekClient;
import com.example.demo.ai.PromptTemplate;
import com.example.demo.ai.QuestionParser;
import com.example.demo.common.BizException;
import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.mapper.*;
import com.example.demo.security.UserContext;
import com.example.demo.util.JudgeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamMapper examMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final ExamRecordMapper examRecordMapper;
    private final ExamAnswerMapper examAnswerMapper;
    private final QuestionMapper questionMapper;
    private final QuestionService questionService;
    private final WrongQuestionService wrongQuestionService;
    private final DeepSeekClient deepSeekClient;
    private final PromptTemplate promptTemplate;
    private final QuestionParser questionParser;

    /**
     * AI 组卷
     */
    public ExamDetailVO generate(ExamGenerateRequest req) {
        String type = StringUtils.hasText(req.getType()) ? req.getType() : "single";
        String difficulty = StringUtils.hasText(req.getDifficulty()) ? req.getDifficulty() : "medium";
        String kp = StringUtils.hasText(req.getKnowledgePoint()) ? req.getKnowledgePoint() : "综合知识";

        // 1. AI 生成题目
        PromptTemplate.Prompt prompt = promptTemplate.questionGeneration(kp, type, difficulty, req.getCount());
        String result = deepSeekClient.chat(prompt.system(), prompt.user(), true);
        List<Question> questions = questionParser.parseGeneratedQuestions(result);
        for (Question q : questions) {
            questionMapper.insert(q);
        }

        // 2. 创建试卷
        int score = 100 / questions.size();
        Exam exam = new Exam();
        exam.setName(req.getName());
        exam.setTotalScore(score * questions.size());
        exam.setDurationMinutes(req.getDurationMinutes());
        exam.setCreateType("AI");
        examMapper.insert(exam);

        // 3. 关联题目
        int sort = 1;
        for (Question q : questions) {
            ExamQuestion eq = new ExamQuestion();
            eq.setExamId(exam.getId());
            eq.setQuestionId(q.getId());
            eq.setScore(score);
            eq.setSort(sort++);
            examQuestionMapper.insert(eq);
        }

        return buildDetail(exam);
    }

    /**
     * 试卷列表
     */
    public List<ExamVO> list() {
        return examMapper.selectList(new LambdaQueryWrapper<Exam>().orderByDesc(Exam::getCreateTime))
                .stream().map(this::toExamVO).toList();
    }

    /**
     * 试卷详情(含题目,作答用,隐藏答案)
     */
    public ExamDetailVO detail(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BizException("试卷不存在");
        }
        return buildDetail(exam);
    }

    /**
     * 开始考试
     */
    public ExamRecordVO start(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BizException("试卷不存在");
        }
        ExamRecord record = new ExamRecord();
        record.setUserId(UserContext.getUserId());
        record.setExamId(examId);
        record.setStatus("ongoing");
        record.setObjectiveScore(0);
        record.setSubjectiveScore(0);
        record.setTotalScore(0);
        record.setStartTime(LocalDateTime.now());
        examRecordMapper.insert(record);
        return toExamRecordVO(record);
    }

    /**
     * 交卷:客观题自动判分,主观题 AI 判分
     */
    public ExamReportVO submit(Long examId, ExamSubmitRequest req) {
        Long userId = UserContext.getUserId();
        ExamRecord record = examRecordMapper.selectById(req.getRecordId());
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BizException("考试记录不存在");
        }
        if (!"ongoing".equals(record.getStatus())) {
            throw new BizException("该考试已交卷");
        }
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BizException("试卷不存在");
        }

        List<ExamQuestion> eqs = listExamQuestions(examId);
        Map<Long, Question> qMap = questionMap(eqs);
        Map<Long, String> answerMap = req.getAnswers() == null ? Map.of()
                : req.getAnswers().stream()
                        .filter(a -> a.getQuestionId() != null)
                        .collect(Collectors.toMap(AnswerItem::getQuestionId, AnswerItem::getUserAnswer, (x, y) -> x));

        int objectiveScore = 0;
        int subjectiveScore = 0;
        Map<Long, ExamAnswer> answerResult = new java.util.HashMap<>();

        for (ExamQuestion eq : eqs) {
            Question q = qMap.get(eq.getQuestionId());
            if (q == null) {
                continue;
            }
            String userAnswer = answerMap.get(q.getId());
            ExamAnswer ea = new ExamAnswer();
            ea.setRecordId(record.getId());
            ea.setQuestionId(q.getId());
            ea.setUserAnswer(userAnswer);

            if (JudgeUtil.isObjective(q.getType())) {
                boolean correct = JudgeUtil.judge(q, userAnswer);
                int got = correct ? eq.getScore() : 0;
                objectiveScore += got;
                ea.setCorrect(correct ? 1 : 0);
                ea.setAiScore(got);
                if (!correct) {
                    wrongQuestionService.recordWrong(userId, q.getId());
                }
            } else {
                QuestionParser.JudgeResult jr = judgeSubjective(q, userAnswer, eq.getScore());
                subjectiveScore += jr.score();
                ea.setAiScore(jr.score());
                ea.setAiComment(jr.comment());
            }
            examAnswerMapper.insert(ea);
            answerResult.put(q.getId(), ea);
        }

        int total = objectiveScore + subjectiveScore;
        record.setStatus("submitted");
        record.setObjectiveScore(objectiveScore);
        record.setSubjectiveScore(subjectiveScore);
        record.setTotalScore(total);
        record.setSubmitTime(LocalDateTime.now());
        examRecordMapper.updateById(record);

        return buildReport(record, exam, eqs, qMap, answerResult);
    }

    /**
     * 成绩报告
     */
    public ExamReportVO report(Long recordId) {
        Long userId = UserContext.getUserId();
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BizException("考试记录不存在");
        }
        Exam exam = examMapper.selectById(record.getExamId());
        List<ExamQuestion> eqs = listExamQuestions(record.getExamId());
        Map<Long, Question> qMap = questionMap(eqs);
        Map<Long, ExamAnswer> answerResult = examAnswerMapper.selectList(
                        new LambdaQueryWrapper<ExamAnswer>().eq(ExamAnswer::getRecordId, recordId))
                .stream().collect(Collectors.toMap(ExamAnswer::getQuestionId, a -> a, (x, y) -> x));
        return buildReport(record, exam, eqs, qMap, answerResult);
    }

    private List<ExamQuestion> listExamQuestions(Long examId) {
        return examQuestionMapper.selectList(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, examId)
                .orderByAsc(ExamQuestion::getSort));
    }

    private Map<Long, Question> questionMap(List<ExamQuestion> eqs) {
        List<Long> ids = eqs.stream().map(ExamQuestion::getQuestionId).toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return questionMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
    }

    private ExamDetailVO buildDetail(Exam exam) {
        List<ExamQuestion> eqs = listExamQuestions(exam.getId());
        Map<Long, Question> qMap = questionMap(eqs);
        List<ExamQuestionVO> questions = new ArrayList<>();
        for (ExamQuestion eq : eqs) {
            Question q = qMap.get(eq.getQuestionId());
            if (q == null) {
                continue;
            }
            QuestionVO qvo = questionService.toVOWithoutAnswer(q);
            ExamQuestionVO vo = new ExamQuestionVO();
            vo.setId(qvo.getId());
            vo.setType(qvo.getType());
            vo.setContent(qvo.getContent());
            vo.setOptions(qvo.getOptions());
            vo.setScore(eq.getScore());
            vo.setSort(eq.getSort());
            questions.add(vo);
        }
        ExamDetailVO detail = new ExamDetailVO();
        detail.setId(exam.getId());
        detail.setName(exam.getName());
        detail.setTotalScore(exam.getTotalScore());
        detail.setDurationMinutes(exam.getDurationMinutes());
        detail.setQuestions(questions);
        return detail;
    }

    private ExamReportVO buildReport(ExamRecord record, Exam exam, List<ExamQuestion> eqs,
                                     Map<Long, Question> qMap, Map<Long, ExamAnswer> answerResult) {
        List<ReportItemVO> items = new ArrayList<>();
        for (ExamQuestion eq : eqs) {
            Question q = qMap.get(eq.getQuestionId());
            if (q == null) {
                continue;
            }
            ExamAnswer ea = answerResult.get(q.getId());
            QuestionVO qvo = questionService.toVO(q);
            ReportItemVO item = new ReportItemVO();
            item.setQuestionId(q.getId());
            item.setType(q.getType());
            item.setContent(q.getContent());
            item.setOptions(qvo.getOptions());
            item.setUserAnswer(ea == null ? null : ea.getUserAnswer());
            item.setCorrectAnswer(q.getAnswer());
            item.setAnalysis(q.getAnalysis());
            item.setScore(eq.getScore());
            item.setGotScore(ea == null || ea.getAiScore() == null ? 0 : ea.getAiScore());
            item.setAiComment(ea == null ? null : ea.getAiComment());
            items.add(item);
        }

        ExamReportVO report = new ExamReportVO();
        report.setRecordId(record.getId());
        report.setExamName(exam.getName());
        report.setTotalScore(record.getTotalScore());
        report.setFullScore(exam.getTotalScore());
        report.setObjectiveScore(record.getObjectiveScore());
        report.setSubjectiveScore(record.getSubjectiveScore());
        report.setStatus(record.getStatus());
        report.setStartTime(record.getStartTime());
        report.setSubmitTime(record.getSubmitTime());
        report.setItems(items);
        return report;
    }

    private QuestionParser.JudgeResult judgeSubjective(Question q, String userAnswer, int fullScore) {
        if (userAnswer == null || userAnswer.isBlank()) {
            return new QuestionParser.JudgeResult(0, "未作答");
        }
        PromptTemplate.Prompt prompt = promptTemplate.judge(q.getContent(), q.getAnswer(), userAnswer, fullScore);
        String result = deepSeekClient.chat(prompt.system(), prompt.user(), true);
        return questionParser.parseJudgeResult(result, fullScore);
    }

    private ExamVO toExamVO(Exam exam) {
        ExamVO vo = new ExamVO();
        vo.setId(exam.getId());
        vo.setName(exam.getName());
        vo.setTotalScore(exam.getTotalScore());
        vo.setDurationMinutes(exam.getDurationMinutes());
        vo.setCreateType(exam.getCreateType());
        vo.setCreateTime(exam.getCreateTime());
        return vo;
    }

    private ExamRecordVO toExamRecordVO(ExamRecord record) {
        ExamRecordVO vo = new ExamRecordVO();
        vo.setId(record.getId());
        vo.setExamId(record.getExamId());
        vo.setStatus(record.getStatus());
        vo.setObjectiveScore(record.getObjectiveScore());
        vo.setSubjectiveScore(record.getSubjectiveScore());
        vo.setTotalScore(record.getTotalScore());
        vo.setStartTime(record.getStartTime());
        vo.setSubmitTime(record.getSubmitTime());
        return vo;
    }
}
