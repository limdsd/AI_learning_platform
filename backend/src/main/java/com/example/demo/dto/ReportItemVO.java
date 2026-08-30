package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReportItemVO {

    private Long questionId;
    private String type;
    private String content;
    private List<OptionVO> options;
    private String userAnswer;
    private String correctAnswer;
    private String analysis;
    private Integer score;
    private Integer gotScore;
    private String aiComment;
}
