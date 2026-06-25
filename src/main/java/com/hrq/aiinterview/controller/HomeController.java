package com.hrq.aiinterview.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrq.aiinterview.entity.QuestionBank;
import com.hrq.aiinterview.mapper.QuestionBankMapper;
import com.hrq.aiinterview.mapper.ResumeInfoMapper;
import com.hrq.aiinterview.mapper.SysUserMapper;
import com.hrq.aiinterview.service.QuestionCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final QuestionBankMapper questionBankMapper;
    private final ResumeInfoMapper resumeInfoMapper;
    private final SysUserMapper sysUserMapper;
    private final QuestionCacheService questionCacheService;

    @GetMapping("/")
    public String index(Model model) {
        Long questionCount = questionBankMapper.selectCount(null);
        Long resumeCount = resumeInfoMapper.selectCount(null);
        Long userCount = sysUserMapper.selectCount(null);
        Long testQuestionCount = questionBankMapper.selectCount(new LambdaQueryWrapper<QuestionBank>()
                .eq(QuestionBank::getCategory, "软件测试"));

        model.addAttribute("questionCount", questionCount == null ? 0 : questionCount);
        model.addAttribute("resumeCount", resumeCount == null ? 0 : resumeCount);
        model.addAttribute("userCount", userCount == null ? 0 : userCount);
        model.addAttribute("testQuestionCount", testQuestionCount == null ? 0 : testQuestionCount);

        List<QuestionBank> hotQuestions = questionCacheService.getHotQuestions();
        model.addAttribute("hotQuestions", hotQuestions == null ? Collections.emptyList() : hotQuestions);
        model.addAttribute("cacheMode", questionCacheService.getCacheMode());

        return "index";
    }
}
