package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ExamSubmitRequest {

    @NotNull(message = "考试记录ID不能为空")
    private Long recordId;

    private List<AnswerItem> answers;
}
