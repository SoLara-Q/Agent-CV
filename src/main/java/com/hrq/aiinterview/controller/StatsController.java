package com.hrq.aiinterview.controller;

import com.hrq.aiinterview.entity.QuestionBank;
import com.hrq.aiinterview.mapper.QuestionBankMapper;
import com.hrq.aiinterview.mapper.ResumeInfoMapper;
import com.hrq.aiinterview.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class StatsController {

    private final QuestionBankMapper questionBankMapper;
    private final ResumeInfoMapper resumeInfoMapper;
    private final SysUserMapper sysUserMapper;

    @GetMapping("/stats")
    public String stats(Model model) {
        model.addAttribute("questionCount", questionBankMapper.selectCount(null));
        model.addAttribute("resumeCount", resumeInfoMapper.selectCount(null));
        model.addAttribute("userCount", sysUserMapper.selectCount(null));
        return "stats/index";
    }

    @GetMapping("/api/stats/job-types")
    @ResponseBody
    public Map<String, Object> jobTypeStats() {
        List<QuestionBank> questions = questionBankMapper.selectList(null);
        Map<String, Long> group = questions.stream()
                .collect(Collectors.groupingBy(q -> q.getJobType() == null ? "未分类" : q.getJobType(), LinkedHashMap::new, Collectors.counting()));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("names", group.keySet());
        result.put("values", group.values());
        return result;
    }

    @GetMapping("/api/stats/categories")
    @ResponseBody
    public Map<String, Object> categoryStats() {
        List<QuestionBank> questions = questionBankMapper.selectList(null);
        Map<String, Long> group = questions.stream()
                .collect(Collectors.groupingBy(q -> q.getCategory() == null ? "未分类" : q.getCategory(), LinkedHashMap::new, Collectors.counting()));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("names", group.keySet());
        result.put("values", group.values());
        return result;
    }
}
