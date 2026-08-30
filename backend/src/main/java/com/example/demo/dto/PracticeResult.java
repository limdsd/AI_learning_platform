package com.example.demo.dto;

import lombok.Data;

@Data
public class PracticeResult {

    private boolean correct;
    private String correctAnswer;
    private String analysis;
}
