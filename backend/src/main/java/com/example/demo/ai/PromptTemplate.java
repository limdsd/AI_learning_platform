package com.example.demo.ai;

import org.springframework.stereotype.Component;

/**
 * 提示词模板(出题 / 解析 / 判分)
 */
@Component
public class PromptTemplate {

    /** 提示词对(系统 + 用户) */
    public record Prompt(String system, String user) {
    }

    private static String typeDesc(String type) {
        return switch (type) {
            case "single" -> "单选题";
            case "multiple" -> "多选题";
            case "judge" -> "判断题";
            case "fill" -> "填空题";
            case "short_answer" -> "简答题";
            default -> "题目";
        };
    }

    private static String difficultyDesc(String difficulty) {
        return switch (difficulty) {
            case "easy" -> "简单";
            case "medium" -> "中等";
            case "hard" -> "困难";
            default -> "中等";
        };
    }

    /**
     * 智能出题提示词
     */
    public Prompt questionGeneration(String knowledgePoint, String type, String difficulty, int count) {
        String system = "你是一位专业的出题专家,擅长根据知识点设计高质量的题目。" +
                "你必须严格按照用户要求的 JSON 格式输出,不要输出任何 JSON 以外的文字或 markdown 代码块。";
        String user = """
                请围绕知识点「%s」生成 %d 道%s,难度为%s。

                严格输出如下 JSON 结构(不要包含任何 markdown 代码块或其他文字):
                {"questions":[{"type":"%s","content":"题干","options":[{"key":"A","text":"选项内容"}],"answer":"答案","analysis":"解析","knowledgePoint":"%s","difficulty":"%s"}]}

                各题型要求:
                - 单选题(single): 提供 4 个选项,answer 填正确选项字母,如 "A"
                - 多选题(multiple): 提供 4 个选项,answer 填多个正确选项字母用逗号分隔,如 "A,C"
                - 判断题(judge): options 固定为 [{"key":"A","text":"对"},{"key":"B","text":"错"}],answer 填 "对" 或 "错"
                - 填空题(fill): options 为空数组 [],answer 填参考答案(多个空用 | 分隔)
                - 简答题(short_answer): options 为空数组 [],answer 填参考答案要点

                每道题必须有 content、answer、analysis 字段,题目不要重复。
                """.formatted(knowledgePoint, count, typeDesc(type), difficultyDesc(difficulty),
                type, knowledgePoint, difficulty);
        return new Prompt(system, user);
    }

    /**
     * AI 智能解析提示词
     */
    public Prompt explain(String content, String options, String answer) {
        String system = "你是一位耐心的学习辅导老师,擅长用通俗易懂的方式讲解题目。";
        String user = """
                请为下面这道题给出详细、易懂的解析,包括解题思路、涉及的知识点和易错点。

                【题干】
                %s
                %s

                【参考答案】
                %s

                请直接输出解析文字。
                """.formatted(content, (options == null ? "" : options), answer);
        return new Prompt(system, user);
    }

    /**
     * 主观题 AI 判分提示词
     */
    public Prompt judge(String content, String answer, String userAnswer, int fullScore) {
        String system = "你是一位严谨的阅卷老师,负责为主观题(填空/简答)评分。";
        String user = """
                请根据参考答案,为下面的学生作答评分。

                【题目】
                %s

                【参考答案】
                %s

                【学生作答】
                %s

                本题满分 %d 分。请严格输出如下 JSON(不要包含其他文字):
                {"score": 0, "comment": "评分说明"}

                其中 score 为 0 到 %d 之间的整数,comment 为简短的评分说明。
                """.formatted(content, answer, userAnswer, fullScore, fullScore);
        return new Prompt(system, user);
    }
}
