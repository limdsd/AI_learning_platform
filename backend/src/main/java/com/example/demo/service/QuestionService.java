package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.ai.DeepSeekClient;
import com.example.demo.ai.PromptTemplate;
import com.example.demo.ai.QuestionParser;
import com.example.demo.common.BizException;
import com.example.demo.common.PageResult;
import com.example.demo.dto.GenerateQuestionRequest;
import com.example.demo.dto.OptionVO;
import com.example.demo.dto.QuestionSaveRequest;
import com.example.demo.dto.QuestionVO;
import com.example.demo.entity.Question;
import com.example.demo.mapper.QuestionMapper;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionMapper questionMapper;
    private final DeepSeekClient deepSeekClient;
    private final PromptTemplate promptTemplate;
    private final QuestionParser questionParser;
    private final ObjectMapper objectMapper;

    /**
     * AI 智能生成题目并入库
     */
    public List<QuestionVO> generateByAI(GenerateQuestionRequest req) {
        PromptTemplate.Prompt prompt = promptTemplate.questionGeneration(
                req.getKnowledgePoint(), req.getType(), req.getDifficulty(), req.getCount());
        String result = deepSeekClient.chat(prompt.system(), prompt.user(), true);
        List<Question> questions = questionParser.parseGeneratedQuestions(result);
        for (Question q : questions) {
            questionMapper.insert(q);
        }
        return questions.stream().map(this::toVO).toList();
    }

    /**
     * 手动新增题目
     */
    public QuestionVO save(QuestionSaveRequest req) {
        Question q = new Question();
        applyRequest(q, req);
        q.setSource("manual");
        questionMapper.insert(q);
        return toVO(q);
    }

    /**
     * 分页查询
     */
    public PageResult<QuestionVO> page(int page, int size, String knowledgePoint, String type, String difficulty) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(knowledgePoint), Question::getKnowledgePoint, knowledgePoint)
                .eq(StringUtils.hasText(type), Question::getType, type)
                .eq(StringUtils.hasText(difficulty), Question::getDifficulty, difficulty)
                .orderByDesc(Question::getCreateTime);
        Page<Question> result = questionMapper.selectPage(new Page<>(page, size), wrapper);
        List<QuestionVO> vos = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(result.getTotal(), vos);
    }

    /**
     * 更新题目
     */
    public QuestionVO update(Long id, QuestionSaveRequest req) {
        Question q = questionMapper.selectById(id);
        if (q == null) {
            throw new BizException("题目不存在");
        }
        applyRequest(q, req);
        questionMapper.updateById(q);
        return toVO(q);
    }

    /**
     * 删除题目(逻辑删除)
     */
    public void delete(Long id) {
        questionMapper.deleteById(id);
    }

    private void applyRequest(Question q, QuestionSaveRequest req) {
        q.setType(req.getType());
        q.setContent(req.getContent());
        q.setOptions(req.getOptions());
        q.setAnswer(req.getAnswer());
        q.setAnalysis(req.getAnalysis());
        q.setKnowledgePoint(req.getKnowledgePoint());
        q.setDifficulty(req.getDifficulty());
    }

    /**
     * 实体转 VO(options JSON 字符串 -> 列表)
     */
    public QuestionVO toVO(Question q) {
        QuestionVO vo = new QuestionVO();
        vo.setId(q.getId());
        vo.setType(q.getType());
        vo.setContent(q.getContent());
        vo.setOptions(parseOptions(q.getOptions()));
        vo.setAnswer(q.getAnswer());
        vo.setAnalysis(q.getAnalysis());
        vo.setKnowledgePoint(q.getKnowledgePoint());
        vo.setDifficulty(q.getDifficulty());
        vo.setSource(q.getSource());
        vo.setCreateTime(q.getCreateTime());
        return vo;
    }

    /**
     * 实体转 VO 并隐藏答案(刷题/考试作答场景)
     */
    public QuestionVO toVOWithoutAnswer(Question q) {
        QuestionVO vo = toVO(q);
        vo.setAnswer(null);
        vo.setAnalysis(null);
        return vo;
    }

    private List<OptionVO> parseOptions(String optionsJson) {
        if (!StringUtils.hasText(optionsJson)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<List<OptionVO>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}
