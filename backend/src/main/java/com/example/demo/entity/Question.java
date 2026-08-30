package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question")
public class Question extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 题型: single / multiple / judge / fill / short_answer */
    private String type;

    /** 题干 */
    private String content;

    /** 选项 JSON 数组 [{key, text}] */
    private String options;

    /** 答案 */
    private String answer;

    /** 解析 */
    private String analysis;

    /** 知识点 */
    private String knowledgePoint;

    /** 难度: easy / medium / hard */
    private String difficulty;

    /** 来源: AI / manual */
    private String source;
}
