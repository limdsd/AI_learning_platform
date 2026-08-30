package com.example.demo.util;

import com.example.demo.entity.Question;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 客观题判分工具
 */
public final class JudgeUtil {

    private JudgeUtil() {
    }

    /** 是否为客观题(可自动判分) */
    public static boolean isObjective(String type) {
        return "single".equals(type) || "multiple".equals(type) || "judge".equals(type);
    }

    /**
     * 客观题判分;主观题(fill/short_answer)返回 false,交由 AI 判分或自评
     */
    public static boolean judge(Question q, String userAnswer) {
        if (!isObjective(q.getType())) {
            return false;
        }
        if (userAnswer == null) {
            return false;
        }
        String ans = userAnswer.trim();
        String correct = q.getAnswer() == null ? "" : q.getAnswer().trim();
        if ("multiple".equals(q.getType())) {
            return sameSet(correct, ans);
        }
        return ans.equalsIgnoreCase(correct);
    }

    private static boolean sameSet(String a, String b) {
        return toSet(a).equals(toSet(b));
    }

    private static Set<String> toSet(String s) {
        return Arrays.stream(s.split("[,，]"))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .collect(Collectors.toSet());
    }
}
