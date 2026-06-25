package com.hrq.aiinterview.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrq.aiinterview.entity.QuestionBank;
import com.hrq.aiinterview.mapper.QuestionBankMapper;
import com.hrq.aiinterview.service.QuestionCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/questions")
public class QuestionController {

    private static final List<String> CATEGORIES = List.of("Java基础", "Spring Boot", "MySQL", "Redis", "软件测试", "前端基础", "算法题", "项目面试", "HR面试");
    private static final List<String> JOB_TYPES = List.of("Java后端", "软件测试", "前端开发", "AI应用", "算法岗/通用", "通用");
    private static final List<String> DIFFICULTIES = List.of("简单", "中等", "困难");

    private final QuestionBankMapper questionBankMapper;
    private final QuestionCacheService questionCacheService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") long page,
                       @RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "") String category,
                       @RequestParam(defaultValue = "") String jobType,
                       @RequestParam(defaultValue = "") String difficulty,
                       Model model) {
        LambdaQueryWrapper<QuestionBank> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(QuestionBank::getTitle, keyword)
                    .or()
                    .like(QuestionBank::getAnswer, keyword)
                    .or()
                    .like(QuestionBank::getTags, keyword));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(QuestionBank::getCategory, category);
        }
        if (StringUtils.hasText(jobType)) {
            wrapper.eq(QuestionBank::getJobType, jobType);
        }
        if (StringUtils.hasText(difficulty)) {
            wrapper.eq(QuestionBank::getDifficulty, difficulty);
        }
        wrapper.orderByDesc(QuestionBank::getCreateTime).orderByDesc(QuestionBank::getId);
        Page<QuestionBank> result = questionBankMapper.selectPage(new Page<>(page, 10), wrapper);

        model.addAttribute("page", result);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("jobType", jobType);
        model.addAttribute("difficulty", difficulty);
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("jobTypes", JOB_TYPES);
        model.addAttribute("difficulties", DIFFICULTIES);
        model.addAttribute("hotQuestions", questionCacheService.getHotQuestions());
        return "question/list";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        QuestionBank question = questionBankMapper.selectById(id);
        if (question == null) {
            return "redirect:/questions";
        }
        question.setViewCount(question.getViewCount() == null ? 1 : question.getViewCount() + 1);
        questionBankMapper.updateById(question);
        questionCacheService.clearHotQuestionCache();
        model.addAttribute("question", question);
        return "question/detail";
    }

    @GetMapping("/form")
    public String form(@RequestParam(required = false) Long id, Model model) {
        QuestionBank question = id == null ? new QuestionBank() : questionBankMapper.selectById(id);
        model.addAttribute("question", question == null ? new QuestionBank() : question);
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("jobTypes", JOB_TYPES);
        model.addAttribute("difficulties", DIFFICULTIES);
        return "question/form";
    }

    @PostMapping("/save")
    public String save(QuestionBank question) {
        if (question.getViewCount() == null) {
            question.setViewCount(0);
        }
        if (question.getId() == null) {
            question.setCreateTime(LocalDateTime.now());
            questionBankMapper.insert(question);
        } else {
            QuestionBank old = questionBankMapper.selectById(question.getId());
            if (old != null && question.getCreateTime() == null) {
                question.setCreateTime(old.getCreateTime());
            }
            questionBankMapper.updateById(question);
        }
        questionCacheService.clearHotQuestionCache();
        return "redirect:/questions";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        questionBankMapper.deleteById(id);
        questionCacheService.clearHotQuestionCache();
        return "redirect:/questions";
    }
}
