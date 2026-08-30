package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.ai.DeepSeekClient;
import com.example.demo.ai.PromptTemplate;
import com.example.demo.common.BizException;
import com.example.demo.dto.PracticeResult;
import com.example.demo.dto.PracticeSubmitRequest;
import com.example.demo.dto.QuestionVO;
import com.example.demo.dto.WrongQuestionVO;
import com.example.demo.entity.PracticeRecord;
import com.example.demo.entity.Question;
import com.example.demo.entity.WrongQuestion;
import com.example.demo.mapper.PracticeRecordMapper;
import com.example.demo.mapper.QuestionMapper;
import com.example.demo.mapper.WrongQuestionMapper;
import com.example.demo.security.UserContext;
import com.example.demo.util.JudgeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PracticeService {

    private final QuestionMapper questionMapper;
    private final PracticeRecordMapper practiceRecordMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final QuestionService questionService;
    private final DeepSeekClient deepSeekClient;
    private final PromptTemplate promptTemplate;
    private final WrongQuestionService wrongQuestionService;

    /**
     * 抽题(按知识点/难度,随机)
     */
    public List<QuestionVO> getQuestions(String knowledgePoint, String difficulty, int count) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(knowledgePoint), Question::getKnowledgePoint, knowledgePoint)
                .eq(StringUtils.hasText(difficulty), Question::getDifficulty, difficulty)
                .last("ORDER BY RAND() LIMIT " + count);
        return questionMapper.selectList(wrapper).stream()
                .map(questionService::toVOWithoutAnswer)
                .toList();
    }

    /**
     * 提交作答:客观题自动判分,答错入错题本
     */
    public PracticeResult submit(PracticeSubmitRequest req) {
        Long userId = UserContext.getUserId();
        Question q = questionMapper.selectById(req.getQuestionId());
        if (q == null) {
            throw new BizException("题目不存在");
        }

        boolean correct = JudgeUtil.judge(q, req.getUserAnswer());

        PracticeRecord record = new PracticeRecord();
        record.setUserId(userId);
        record.setQuestionId(req.getQuestionId());
        record.setUserAnswer(req.getUserAnswer());
        record.setCorrect(correct ? 1 : 0);
        record.setCostSeconds(req.getCostSeconds());
        practiceRecordMapper.insert(record);

        if (!correct) {
            wrongQuestionService.recordWrong(userId, req.getQuestionId());
        }

        PracticeResult result = new PracticeResult();
        result.setCorrect(correct);
        result.setCorrectAnswer(q.getAnswer());
        result.setAnalysis(q.getAnalysis());
        return result;
    }

    /**
     * 错题列表
     */
    public List<WrongQuestionVO> wrongList() {
        Long userId = UserContext.getUserId();
        List<WrongQuestion> wqs = wrongQuestionMapper.selectList(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getUserId, userId)
                .eq(WrongQuestion::getMastered, 0)
                .orderByDesc(WrongQuestion::getCreateTime));
        if (wqs.isEmpty()) {
            return List.of();
        }
        Map<Long, WrongQuestion> wqMap = wqs.stream()
                .collect(Collectors.toMap(WrongQuestion::getQuestionId, w -> w));
        List<Question> questions = questionMapper.selectBatchIds(
                wqs.stream().map(WrongQuestion::getQuestionId).toList());
        List<WrongQuestionVO> vos = new ArrayList<>();
        for (Question q : questions) {
            WrongQuestion wq = wqMap.get(q.getId());
            QuestionVO qvo = questionService.toVO(q);
            WrongQuestionVO vo = new WrongQuestionVO();
            vo.setWrongId(wq.getId());
            vo.setQuestionId(q.getId());
            vo.setWrongCount(wq.getWrongCount());
            vo.setType(qvo.getType());
            vo.setContent(qvo.getContent());
            vo.setOptions(qvo.getOptions());
            vo.setAnswer(qvo.getAnswer());
            vo.setAnalysis(qvo.getAnalysis());
            vo.setKnowledgePoint(qvo.getKnowledgePoint());
            vo.setDifficulty(qvo.getDifficulty());
            vos.add(vo);
        }
        return vos;
    }

    /**
     * 标记错题已掌握
     */
    public void masterWrong(Long wrongId) {
        WrongQuestion wq = wrongQuestionMapper.selectById(wrongId);
        if (wq == null) {
            throw new BizException("错题记录不存在");
        }
        wq.setMastered(1);
        wrongQuestionMapper.updateById(wq);
    }

    /**
     * AI 智能解析
     */
    public String aiExplain(Long questionId) {
        Question q = questionMapper.selectById(questionId);
        if (q == null) {
            throw new BizException("题目不存在");
        }
        PromptTemplate.Prompt prompt = promptTemplate.explain(q.getContent(), q.getOptions(), q.getAnswer());
        return deepSeekClient.chat(prompt.system(), prompt.user(), false);
    }

    /**
     * 智能推荐:优先薄弱知识点
     */
    public List<QuestionVO> recommend(int count) {
        Long userId = UserContext.getUserId();
        List<WrongQuestion> wqs = wrongQuestionMapper.selectList(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getUserId, userId)
                .eq(WrongQuestion::getMastered, 0));
        List<Long> wrongIds = wqs.stream().map(WrongQuestion::getQuestionId).toList();

        if (!wrongIds.isEmpty()) {
            List<Question> wrongQuestions = questionMapper.selectBatchIds(wrongIds);
            String weakKp = wrongQuestions.stream()
                    .collect(Collectors.groupingBy(Question::getKnowledgePoint, Collectors.counting()))
                    .entrySet().stream()
                    .filter(e -> StringUtils.hasText(e.getKey()))
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
            if (weakKp != null) {
                List<Question> list = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                        .eq(Question::getKnowledgePoint, weakKp)
                        .notIn(Question::getId, wrongIds)
                        .last("ORDER BY RAND() LIMIT " + count));
                if (!list.isEmpty()) {
                    return list.stream().map(questionService::toVOWithoutAnswer).toList();
                }
            }
        }

        List<Question> list = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .last("ORDER BY RAND() LIMIT " + count));
        return list.stream().map(questionService::toVOWithoutAnswer).toList();
    }

}
