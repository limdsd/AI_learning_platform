package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class WrongQuestionVO {

    private Long wrongId;
    private Long questionId;
    private Integer wrongCount;
    private String type;
    private String content;
    private List<OptionVO> options;
    private String answer;
    private String analysis;
    private String knowledgePoint;
    private String difficulty;
}
