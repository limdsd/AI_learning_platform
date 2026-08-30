package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.PracticeResult;
import com.example.demo.dto.PracticeSubmitRequest;
import com.example.demo.dto.QuestionVO;
import com.example.demo.dto.WrongQuestionVO;
import com.example.demo.service.PracticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/practice")
@RequiredArgsConstructor
public class PracticeController {

    private final PracticeService practiceService;

    /** 抽题 */
    @GetMapping("/questions")
    public Result<List<QuestionVO>> questions(@RequestParam(required = false) String knowledgePoint,
                                              @RequestParam(required = false) String difficulty,
                                              @RequestParam(defaultValue = "10") int count) {
        return Result.success(practiceService.getQuestions(knowledgePoint, difficulty, count));
    }

    /** 提交作答 */
    @PostMapping("/submit")
    public Result<PracticeResult> submit(@Valid @RequestBody PracticeSubmitRequest req) {
        return Result.success(practiceService.submit(req));
    }

    /** 错题列表 */
    @GetMapping("/wrong")
    public Result<List<WrongQuestionVO>> wrong() {
        return Result.success(practiceService.wrongList());
    }

    /** 标记已掌握 */
    @PostMapping("/wrong/{id}/master")
    public Result<Void> master(@PathVariable Long id) {
        practiceService.masterWrong(id);
        return Result.success();
    }

    /** AI 智能解析 */
    @PostMapping("/ai-explain")
    public Result<String> aiExplain(@RequestParam Long questionId) {
        return Result.success(practiceService.aiExplain(questionId));
    }

    /** AI 智能解析(RAG 增强:检索相似题作为参考上下文) */
    @PostMapping("/ai-explain-rag")
    public Result<String> aiExplainRag(@RequestParam Long questionId) {
        return Result.success(practiceService.aiExplainWithRag(questionId));
    }

    /** 智能推荐 */
    @GetMapping("/recommend")
    public Result<List<QuestionVO>> recommend(@RequestParam(defaultValue = "10") int count) {
        return Result.success(practiceService.recommend(count));
    }

    /** 相似题推荐(RAG 语义检索) */
    @GetMapping("/similar")
    public Result<List<QuestionVO>> similar(@RequestParam Long questionId,
                                            @RequestParam(defaultValue = "5") int count) {
        return Result.success(practiceService.similar(questionId, count));
    }
}
