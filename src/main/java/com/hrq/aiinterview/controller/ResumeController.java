package com.hrq.aiinterview.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrq.aiinterview.entity.ResumeInfo;
import com.hrq.aiinterview.entity.SysUser;
import com.hrq.aiinterview.mapper.ResumeInfoMapper;
import com.hrq.aiinterview.mapper.SysUserMapper;
import com.hrq.aiinterview.service.AiService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/resumes")
public class ResumeController {

    private final ResumeInfoMapper resumeInfoMapper;
    private final SysUserMapper sysUserMapper;
    private final AiService aiService;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") long page,
                       @RequestParam(defaultValue = "") String keyword,
                       Authentication authentication,
                       Model model) {
        SysUser current = currentUser(authentication);
        LambdaQueryWrapper<ResumeInfo> wrapper = new LambdaQueryWrapper<>();
        if (!isAdmin(authentication)) {
            wrapper.eq(ResumeInfo::getUserId, current.getId());
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ResumeInfo::getRealName, keyword)
                    .or()
                    .like(ResumeInfo::getTargetJob, keyword));
        }
        wrapper.orderByDesc(ResumeInfo::getUpdateTime);
        Page<ResumeInfo> result = resumeInfoMapper.selectPage(new Page<>(page, 8), wrapper);
        model.addAttribute("page", result);
        model.addAttribute("keyword", keyword);
        return "resume/list";
    }

    @GetMapping("/form")
    public String form(@RequestParam(required = false) Long id, Model model) {
        ResumeInfo resume = id == null ? new ResumeInfo() : resumeInfoMapper.selectById(id);
        model.addAttribute("resume", resume == null ? new ResumeInfo() : resume);
        return "resume/form";
    }

    @PostMapping("/save")
    public String save(ResumeInfo resume,
                       @RequestParam(value = "file", required = false) MultipartFile file,
                       Authentication authentication) throws IOException {
        SysUser current = currentUser(authentication);
        LocalDateTime now = LocalDateTime.now();
        resume.setUpdateTime(now);
        if (resume.getUserId() == null) {
            resume.setUserId(current.getId());
        }
        handleUpload(resume, file);
        if (resume.getId() == null) {
            resume.setCreateTime(now);
            resumeInfoMapper.insert(resume);
        } else {
            resumeInfoMapper.updateById(resume);
        }
        return "redirect:/resumes";
    }
    @PostMapping("/generate-skills")
    @ResponseBody
    public Map<String, Object> generateSkills(@RequestBody ResumeInfo resume) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (resume.getId() != null) {
                ResumeInfo dbResume = resumeInfoMapper.selectById(resume.getId());
                if (dbResume != null) {
                    if (!StringUtils.hasText(resume.getRealName())) {
                        resume.setRealName(dbResume.getRealName());
                    }
                    if (!StringUtils.hasText(resume.getTargetJob())) {
                        resume.setTargetJob(dbResume.getTargetJob());
                    }
                    if (!StringUtils.hasText(resume.getSkills())) {
                        resume.setSkills(dbResume.getSkills());
                    }
                    if (!StringUtils.hasText(resume.getProjectExperience())) {
                        resume.setProjectExperience(dbResume.getProjectExperience());
                    }
                    if (!StringUtils.hasText(resume.getAiSuggest())) {
                        resume.setAiSuggest(dbResume.getAiSuggest());
                    }
                }
            }

            String skills = aiService.generateCoreSkills(resume);

            result.put("success", true);
            result.put("skills", skills);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "AI生成核心技能失败：" + e.getMessage());
        }

        return result;
    }
    @GetMapping("/analyze/{id}")
    public String analyze(@PathVariable Long id) {
        ResumeInfo resume = resumeInfoMapper.selectById(id);
        if (resume != null) {
            System.out.println("========== 开始 AI 分析 ==========");
            System.out.println("当前简历ID：" + id);
            System.out.println("目标岗位：" + resume.getTargetJob());
            System.out.println("技能内容：" + resume.getSkills());
            System.out.println("项目经历长度：" + (resume.getProjectExperience() == null ? 0 : resume.getProjectExperience().length()));
            System.out.println("AiService 实现类：" + aiService.getClass().getName());

            String result = aiService.analyzeResume(resume);

            System.out.println("AI 返回内容前100字：" + (result.length() > 100 ? result.substring(0, 100) : result));
            System.out.println("========== AI 分析结束 ==========");

            resume.setAiSuggest(result);
            resume.setUpdateTime(LocalDateTime.now());
            resumeInfoMapper.updateById(resume);
        }
        return "redirect:/resumes/form?id=" + id;
    }
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        resumeInfoMapper.deleteById(id);
        return "redirect:/resumes";
    }

    private void handleUpload(ResumeInfo resume, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return;
        }

        String originalFilename = file.getOriginalFilename() == null ? "resume.docx" : file.getOriginalFilename();
        String lowerName = originalFilename.toLowerCase(Locale.ROOT);

        if (!lowerName.endsWith(".docx")) {
            throw new IllegalArgumentException("请上传 .docx 格式的 Word 简历文件");
        }

        // 关键修改：把 uploads 转成绝对路径，避免 Tomcat 把它解析到临时目录
        Path dir = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        String safeName = UUID.randomUUID() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9.\\-_\\u4e00-\\u9fa5]", "_");

        // 关键修改：target 也必须是绝对路径
        Path target = dir.resolve(safeName).normalize();

        // 防止路径穿越
        if (!target.startsWith(dir)) {
            throw new IllegalArgumentException("非法文件路径");
        }

        file.transferTo(target.toFile());

        // 数据库里仍然保存访问路径，不保存本地磁盘绝对路径
        resume.setFilePath("/uploads/" + safeName);

        String docxText = readDocxText(target);
        if (StringUtils.hasText(docxText)) {
            String cleanText = docxText.trim();

            if (!StringUtils.hasText(resume.getProjectExperience())) {
                resume.setProjectExperience(cleanText);
            } else {
                resume.setProjectExperience(
                        resume.getProjectExperience().trim()
                                + "\n\n【DOCX简历提取内容】\n"
                                + cleanText
                );
            }
        }
    }
    private String readDocxText(Path docxPath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(docxPath);
             XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private SysUser currentUser(Authentication authentication) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, authentication.getName()));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
