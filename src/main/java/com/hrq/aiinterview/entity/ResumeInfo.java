package com.hrq.aiinterview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resume_info")
public class ResumeInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String realName;
    private String targetJob;
    private String skills;
    private String projectExperience;
    private String aiSuggest;
    private String filePath;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
