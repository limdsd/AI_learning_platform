package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuestionSaveRequest {

    @NotBlank(message = "题型不能为空")
    private String type;

    @NotBlank(message = "题干不能为空")
    private String content;

    /** 选项 JSON 字符串,如 [{"key":"A","text":"..."}] */
    private String options;

    @NotBlank(message = "答案不能为空")
    private String answer;

    private String analysis;

    private String knowledgePoint;

    private String difficulty;
}
