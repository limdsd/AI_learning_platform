package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExamDetailVO {

    private Long id;
    private String name;
    private Integer totalScore;
    private Integer durationMinutes;
    private List<ExamQuestionVO> questions;
}
