package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExamQuestionVO {

    private Long id;
    private String type;
    private String content;
    private List<OptionVO> options;
    private Integer score;
    private Integer sort;
}
