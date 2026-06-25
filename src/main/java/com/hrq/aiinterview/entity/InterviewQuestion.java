package com.hrq.aiinterview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("interview_question")
public class InterviewQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String jobType;
    private String questionTitle;
    private String answerAnalysis;
    private String difficulty;
    private Integer hot;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
