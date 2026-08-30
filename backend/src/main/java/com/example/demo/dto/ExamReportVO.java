package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamReportVO {

    private Long recordId;
    private String examName;
    private Integer totalScore;
    private Integer fullScore;
    private Integer objectiveScore;
    private Integer subjectiveScore;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    private List<ReportItemVO> items;
}
