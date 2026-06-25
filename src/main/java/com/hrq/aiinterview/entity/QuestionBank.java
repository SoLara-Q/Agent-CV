package com.hrq.aiinterview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("question_bank")
public class QuestionBank {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String answer;
    private String category;
    private String difficulty;
    private String jobType;
    private String tags;
    private Integer viewCount;
    private LocalDateTime createTime;
}
