package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_answer")
public class ExamAnswer extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long recordId;

    private Long questionId;

    private String userAnswer;

    /** 客观题判定 1对 0错 */
    private Integer correct;

    /** 主观题 AI 得分 */
    private Integer aiScore;

    /** 主观题 AI 评语 */
    private String aiComment;
}
