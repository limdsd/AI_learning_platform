package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExamGenerateRequest {

    @NotBlank(message = "试卷名称不能为空")
    private String name;

    @NotNull(message = "时长不能为空")
    @Min(value = 5, message = "时长至少5分钟")
    @Max(value = 180, message = "时长最多180分钟")
    private Integer durationMinutes;

    /** 知识点,可选 */
    private String knowledgePoint;

    /** 题型,默认 single */
    private String type;

    /** 难度,默认 medium */
    private String difficulty;

    @NotNull(message = "题量不能为空")
    @Min(value = 1, message = "题量至少1")
    @Max(value = 30, message = "题量最多30")
    private Integer count;
}
