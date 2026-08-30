package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PracticeSubmitRequest {

    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    private String userAnswer;

    private Integer costSeconds;
}
