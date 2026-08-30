package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateQuestionRequest {

    @NotBlank(message = "知识点不能为空")
    private String knowledgePoint;

    /** single / multiple / judge / fill / short_answer */
    @NotBlank(message = "题型不能为空")
    private String type;

    /** easy / medium / hard */
    @NotBlank(message = "难度不能为空")
    private String difficulty;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    @Max(value = 20, message = "单次最多生成20道")
    private Integer count;
}
