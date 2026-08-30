package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionVO {

    private Long id;
    private String type;
    private String content;
    private List<OptionVO> options;
    private String answer;
    private String analysis;
    private String knowledgePoint;
    private String difficulty;
    private String source;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
