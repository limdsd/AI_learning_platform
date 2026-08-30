package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.entity.WrongQuestion;
import com.example.demo.mapper.WrongQuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 错题本服务
 */
@Service
@RequiredArgsConstructor
public class WrongQuestionService {

    private final WrongQuestionMapper wrongQuestionMapper;

    /**
     * 记录错题(存在则次数+1)
     */
    public void recordWrong(Long userId, Long questionId) {
        WrongQuestion existing = wrongQuestionMapper.selectOne(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getUserId, userId)
                .eq(WrongQuestion::getQuestionId, questionId));
        if (existing == null) {
            WrongQuestion wq = new WrongQuestion();
            wq.setUserId(userId);
            wq.setQuestionId(questionId);
            wq.setWrongCount(1);
            wq.setMastered(0);
            wrongQuestionMapper.insert(wq);
        } else {
            existing.setWrongCount(existing.getWrongCount() + 1);
            existing.setMastered(0);
            wrongQuestionMapper.updateById(existing);
        }
    }
}
