package com.hrq.aiinterview.service;

import com.hrq.aiinterview.entity.ResumeInfo;

public interface AiService {
    String analyzeResume(ResumeInfo resumeInfo);

    /**
     * AI 生成核心技能，用于自动填入简历表单的核心技能字段
     */
    String generateCoreSkills(ResumeInfo resumeInfo);
}