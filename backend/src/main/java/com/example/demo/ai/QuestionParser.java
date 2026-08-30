package com.example.demo.ai;

import com.example.demo.common.BizException;
import com.example.demo.entity.Question;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 返回结果解析器
 */
@Component
public class QuestionParser {

    private final ObjectMapper objectMapper;

    public QuestionParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 解析出题接口返回的 JSON,转换为 Question 列表
     */
    public List<Question> parseGeneratedQuestions(String json) {
        String cleaned = extractJson(json);
        try {
            JsonNode root = objectMapper.readTree(cleaned);
            JsonNode arr = root.path("questions");
            if (arr.isMissingNode() || !arr.isArray()) {
                throw new BizException("AI 返回格式异常,缺少 questions 数组");
            }
            List<Question> list = new ArrayList<>();
            for (JsonNode node : arr) {
                String content = node.path("content").asText(null);
                if (content == null || content.isBlank()) {
                    continue;
                }
                Question q = new Question();
                q.setType(node.path("type").asText("single"));
                q.setContent(content);
                JsonNode opt = node.path("options");
                q.setOptions((opt.isMissingNode() || opt.isNull()) ? "[]" : opt.toString());
                q.setAnswer(node.path("answer").asText(""));
                q.setAnalysis(node.path("analysis").asText(null));
                q.setKnowledgePoint(node.path("knowledgePoint").asText(null));
                q.setDifficulty(node.path("difficulty").asText("medium"));
                q.setSource("AI");
                list.add(q);
            }
            if (list.isEmpty()) {
                throw new BizException("AI 未生成有效题目,请重试");
            }
            return list;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("解析 AI 返回的题目失败: " + e.getMessage());
        }
    }

    /** 判分结果 */
    public record JudgeResult(int score, String comment) {
    }

    /**
     * 解析判分接口返回的 JSON
     */
    public JudgeResult parseJudgeResult(String json, int fullScore) {
        String cleaned = extractJson(json);
        try {
            JsonNode root = objectMapper.readTree(cleaned);
            int score = root.path("score").asInt(0);
            String comment = root.path("comment").asText("");
            // 限制在合法范围内
            score = Math.max(0, Math.min(fullScore, score));
            return new JudgeResult(score, comment);
        } catch (Exception e) {
            throw new BizException("解析 AI 判分结果失败: " + e.getMessage());
        }
    }

    /**
     * 提取 JSON 字符串(去除可能的 markdown 代码块包裹)
     */
    private String extractJson(String s) {
        String t = s == null ? "" : s.trim();
        int start = t.indexOf('{');
        int end = t.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return t.substring(start, end + 1);
        }
        return t;
    }
}
