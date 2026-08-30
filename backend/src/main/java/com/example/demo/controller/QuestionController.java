package com.example.demo.controller;

import com.example.demo.common.PageResult;
import com.example.demo.common.Result;
import com.example.demo.dto.GenerateQuestionRequest;
import com.example.demo.dto.QuestionSaveRequest;
import com.example.demo.dto.QuestionVO;
import com.example.demo.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /** AI 智能生成题目 */
    @PostMapping("/ai-generate")
    public Result<List<QuestionVO>> aiGenerate(@Valid @RequestBody GenerateQuestionRequest req) {
        return Result.success(questionService.generateByAI(req));
    }

    /** 手动新增 */
    @PostMapping
    public Result<QuestionVO> save(@Valid @RequestBody QuestionSaveRequest req) {
        return Result.success(questionService.save(req));
    }

    /** 分页查询 */
    @GetMapping("/page")
    public Result<PageResult<QuestionVO>> page(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(required = false) String knowledgePoint,
                                               @RequestParam(required = false) String type,
                                               @RequestParam(required = false) String difficulty) {
        return Result.success(questionService.page(page, size, knowledgePoint, type, difficulty));
    }

    /** 更新 */
    @PutMapping("/{id}")
    public Result<QuestionVO> update(@PathVariable Long id, @Valid @RequestBody QuestionSaveRequest req) {
        return Result.success(questionService.update(id, req));
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return Result.success();
    }
}
