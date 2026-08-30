package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.*;
import com.example.demo.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    /** AI 组卷 */
    @PostMapping("/generate")
    public Result<ExamDetailVO> generate(@Valid @RequestBody ExamGenerateRequest req) {
        return Result.success(examService.generate(req));
    }

    /** 试卷列表 */
    @GetMapping("/list")
    public Result<List<ExamVO>> list() {
        return Result.success(examService.list());
    }

    /** 试卷详情 */
    @GetMapping("/{id}/detail")
    public Result<ExamDetailVO> detail(@PathVariable Long id) {
        return Result.success(examService.detail(id));
    }

    /** 开始考试 */
    @PostMapping("/{id}/start")
    public Result<ExamRecordVO> start(@PathVariable Long id) {
        return Result.success(examService.start(id));
    }

    /** 交卷 */
    @PostMapping("/{id}/submit")
    public Result<ExamReportVO> submit(@PathVariable Long id, @Valid @RequestBody ExamSubmitRequest req) {
        return Result.success(examService.submit(id, req));
    }

    /** 成绩报告 */
    @GetMapping("/record/{recordId}")
    public Result<ExamReportVO> report(@PathVariable Long recordId) {
        return Result.success(examService.report(recordId));
    }
}
