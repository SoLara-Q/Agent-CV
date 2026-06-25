package com.hrq.aiinterview.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrq.aiinterview.entity.ResumeInfo;
import com.hrq.aiinterview.service.AiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class MockAiServiceImpl implements AiService {

    @Value("${app.ai.use-ollama:false}")
    private boolean useOllama;

    @Value("${app.ai.ollama-url:http://127.0.0.1:11434/api/generate}")
    private String ollamaUrl;

    @Value("${app.ai.model:qwen2.5:1.5b}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public String analyzeResume(ResumeInfo resumeInfo) {
        if (useOllama) {
            try {
                System.out.println("正在调用 Ollama，本地模型：" + model);
                String result = callOllama(resumeInfo);
                if (StringUtils.hasText(result)) {
                    return "【来源：Ollama 本地大模型】\n\n" + cleanMarkdown(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "【来源：规则版兜底分析】\n\n"
                        + "Ollama 调用失败，系统自动使用规则版分析。\n"
                        + "失败原因：" + e.getMessage() + "\n\n"
                        + ruleAnalyze(resumeInfo);
            }
        }

        return "【来源：规则版分析】\n\n" + ruleAnalyze(resumeInfo);
    }
    @Override
    public String generateCoreSkills(ResumeInfo resumeInfo) {
        if (useOllama) {
            try {
                String prompt = buildSkillPrompt(resumeInfo);
                String result = callOllamaByPrompt(prompt);
                if (StringUtils.hasText(result)) {
                    return cleanSkillText(cleanMarkdown(result));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ruleGenerateSkills(resumeInfo);
    }
    private String callOllama(ResumeInfo resumeInfo) throws Exception {
        return callOllamaByPrompt(buildPrompt(resumeInfo));
    }

    private String callOllamaByPrompt(String prompt) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("prompt", prompt);
        body.put("stream", false);

        Map<String, Object> options = new LinkedHashMap<>();
        options.put("temperature", 0.3);
        body.put("options", options);

        String requestJson = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ollamaUrl))
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("Ollama 返回状态码：" + response.statusCode() + "，响应内容：" + response.body());
        }

        JsonNode jsonNode = objectMapper.readTree(response.body());
        String aiText = jsonNode.path("response").asText("");

        if (!StringUtils.hasText(aiText)) {
            throw new RuntimeException("Ollama 返回内容为空：" + response.body());
        }

        return aiText;
    }

    private String buildPrompt(ResumeInfo resumeInfo) {
        String realName = safe(resumeInfo.getRealName());
        String targetJob = safe(resumeInfo.getTargetJob());
        String skills = safe(resumeInfo.getSkills());
        String projectExperience = safe(resumeInfo.getProjectExperience());

        String resumeText = """
                姓名：%s
                目标岗位：%s
                核心技能：%s
                项目经历/简历内容：
                %s
                """.formatted(realName, targetJob, skills, projectExperience);

        if (resumeText.length() > 6000) {
            resumeText = resumeText.substring(0, 6000);
        }

        return """
                你是一名有经验的实习招聘面试官和简历优化专家。
                请根据下面这份简历内容，生成一份个性化中文简历优化建议。

                要求：
                1. 必须结合简历中的真实内容，不要每次输出固定模板。
                2. 不要编造简历中不存在的经历。
                3. 如果目标岗位是软件测试，就重点分析测试用例、Bug 管理、接口测试、自动化测试、JIRA、MySQL、项目验证等能力。
                4. 如果目标岗位是 Java 后端，就重点分析 Spring Boot、MyBatis、MySQL、Redis、接口设计、权限控制等能力。
                5. 如果目标岗位是前端，就重点分析 Vue、Element Plus、页面交互、接口联调、项目展示等能力。
                6. 输出要适合大学生实习求职，语言自然，不要太空泛。
                7. 请按照以下结构输出：

                【AI 简历分析结果】
                一、岗位匹配度分析
                二、简历优势
                三、目前存在的问题
                四、具体优化建议
                五、模拟面试官可能追问的问题

                简历内容如下：
                --------------------
                %s
                --------------------
                """.formatted(resumeText);
    }
    private String buildSkillPrompt(ResumeInfo resumeInfo) {
        String realName = safe(resumeInfo.getRealName());
        String targetJob = safe(resumeInfo.getTargetJob());
        String skills = safe(resumeInfo.getSkills());
        String projectExperience = safe(resumeInfo.getProjectExperience());
        String aiSuggest = safe(resumeInfo.getAiSuggest());

        String resumeText = """
            姓名：%s
            目标岗位：%s
            已有核心技能：%s
            项目经历：
            %s

            已有AI优化建议：
            %s
            """.formatted(realName, targetJob, skills, projectExperience, aiSuggest);

        if (resumeText.length() > 5000) {
            resumeText = resumeText.substring(0, 5000);
        }

        return """
            你是一名简历优化专家。
            请根据下面的简历信息，帮用户生成一段适合直接填写到“核心技能”栏的内容。

            要求：
            1. 只输出核心技能内容，不要输出解释。
            2. 用中文顿号“、”分隔技能。
            3. 不要编造过于夸张的能力。
            4. 要结合目标岗位。
            5. 如果目标岗位是软件测试，重点生成：测试用例、功能测试、接口测试、Postman、JMeter、Bug管理、回归测试、MySQL、JIRA等。
            6. 如果目标岗位是Java后端，重点生成：Java、Spring Boot、MyBatis-Plus、MySQL、Redis、接口开发、权限认证、Linux、Git等。
            7. 如果目标岗位是前端，重点生成：HTML、CSS、JavaScript、Vue、Element Plus、Axios、接口联调等。
            8. 长度控制在 80 到 180 字之间。

            简历信息如下：
            --------------------
            %s
            --------------------
            """.formatted(resumeText);
    }

    private String ruleGenerateSkills(ResumeInfo resumeInfo) {
        String targetJob = safe(resumeInfo.getTargetJob());
        String skills = safe(resumeInfo.getSkills());
        String project = safe(resumeInfo.getProjectExperience());

        String allText = (targetJob + " " + skills + " " + project).toLowerCase(Locale.ROOT);

        Set<String> result = new LinkedHashSet<>();

        if (targetJob.contains("测试") || allText.contains("test") || allText.contains("postman") || allText.contains("jmeter")) {
            result.add("软件测试基础");
            result.add("测试用例设计");
            result.add("功能测试");
            result.add("接口测试");
            result.add("Postman");
            result.add("JMeter");
            result.add("Bug定位与缺陷跟踪");
            result.add("回归测试");
            result.add("MySQL基础");
            result.add("JIRA/Bugzilla");
        } else if (targetJob.contains("前端") || allText.contains("vue") || allText.contains("javascript")) {
            result.add("HTML");
            result.add("CSS");
            result.add("JavaScript");
            result.add("Vue3");
            result.add("Element Plus");
            result.add("Axios");
            result.add("接口联调");
            result.add("页面交互优化");
            result.add("Git");
        } else {
            result.add("Java基础");
            result.add("Spring Boot");
            result.add("Spring MVC");
            result.add("MyBatis-Plus");
            result.add("MySQL");
            result.add("Redis");
            result.add("RESTful接口开发");
            result.add("权限认证");
            result.add("Git");
            result.add("Linux基础");
        }

        if (allText.contains("spring security")) result.add("Spring Security");
        if (allText.contains("jwt")) result.add("JWT认证");
        if (allText.contains("rabbitmq")) result.add("RabbitMQ");
        if (allText.contains("docker")) result.add("Docker");
        if (allText.contains("python")) result.add("Python");
        if (allText.contains("pytorch")) result.add("PyTorch");
        if (allText.contains("flask")) result.add("Flask");
        if (allText.contains("echarts")) result.add("ECharts");
        if (allText.contains("算法") || allText.contains("acm")) result.add("算法与数据结构");

        return String.join("、", result);
    }

    private String cleanSkillText(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }

        String result = text.trim();

        result = result.replace("核心技能：", "");
        result = result.replace("核心技能:", "");
        result = result.replace("技能：", "");
        result = result.replace("技能:", "");

        result = result.replace("\n", "、");
        result = result.replace("，", "、");
        result = result.replace(",", "、");
        result = result.replace("；", "、");
        result = result.replace(";", "、");

        result = result.replaceAll("、{2,}", "、");
        result = result.replaceAll("^、|、$", "");

        if (result.length() > 300) {
            result = result.substring(0, 300);
        }

        return result.trim();
    }
    private String ruleAnalyze(ResumeInfo resumeInfo) {
        String skills = safe(resumeInfo.getSkills());
        String project = safe(resumeInfo.getProjectExperience());
        String targetJob = safe(resumeInfo.getTargetJob());
        String allText = (skills + " " + project).toLowerCase(Locale.ROOT);

        StringBuilder sb = new StringBuilder();
        sb.append("【AI 简历分析结果】\n");
        sb.append("目标岗位：").append(targetJob).append("\n\n");

        sb.append("一、岗位匹配度分析\n");
        int score = 60;
        if (allText.contains("spring boot")) score += 10;
        if (allText.contains("mybatis") || allText.contains("mybatis-plus")) score += 8;
        if (allText.contains("mysql")) score += 8;
        if (allText.contains("redis")) score += 8;
        if (allText.contains("项目") || allText.contains("平台") || allText.contains("系统")) score += 6;
        if (score > 100) score = 100;
        sb.append("综合匹配度约为：").append(score).append(" 分。\n\n");

        sb.append("二、优势提取\n");
        if (allText.contains("spring boot")) {
            sb.append("1. 已体现 Spring Boot 项目经验，建议继续突出 Controller、Service、Mapper 分层设计。\n");
        }
        if (allText.contains("redis")) {
            sb.append("2. 已体现 Redis 技术点，可以补充缓存更新策略、缓存穿透处理等细节。\n");
        }
        if (allText.contains("mysql")) {
            sb.append("3. 已体现 MySQL 基础，可以补充表结构设计、索引、分页查询和事务控制。\n");
        }
        if (!allText.contains("spring boot") && !allText.contains("mysql") && !allText.contains("redis")) {
            sb.append("1. 当前技术关键词较少，建议补充 Java、Spring Boot、MySQL、Redis 等岗位相关技能。\n");
        }

        sb.append("\n三、优化建议\n");
        if (!allText.contains("spring security")) {
            sb.append("1. 建议补充 Spring Security / JWT / RBAC 权限控制经验。\n");
        }
        if (!allText.contains("rabbitmq")) {
            sb.append("2. 可以加入 RabbitMQ 异步处理或消息通知作为项目亮点。\n");
        }
        sb.append("3. 项目经历建议使用 STAR 法描述：项目背景、负责内容、技术实现、最终结果。\n");
        sb.append("4. 建议量化成果，例如“完成 4 个核心模块、设计 5 张业务表、接口平均响应时间低于 200ms”。\n");

        sb.append("\n四、模拟面试追问\n");
        sb.append("1. 你这个项目为什么要使用 Redis？缓存一致性如何保证？\n");
        sb.append("2. Spring Security 的登录认证流程是什么？\n");
        sb.append("3. MyBatis-Plus 分页查询如何实现？\n");
        sb.append("4. 你在项目中遇到过什么 Bug？如何定位和解决？\n");

        return sb.toString();
    }
    private String cleanMarkdown(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }

        String result = text;

        // 统一换行
        result = result.replace("\r\n", "\n");

        // 删除 Markdown 标题符号：#、##、###
        result = result.replaceAll("(?m)^\\s{0,3}#{1,6}\\s*", "");

        // 删除加粗符号：**内容**
        result = result.replaceAll("\\*\\*(.*?)\\*\\*", "$1");

        // 删除加粗符号：__内容__
        result = result.replaceAll("__(.*?)__", "$1");

        // 删除斜体符号：*内容*
        result = result.replaceAll("(?<!\\*)\\*(?!\\*)(.*?)\\*(?!\\*)", "$1");

        // 删除列表前面的 -、*、+
        result = result.replaceAll("(?m)^\\s*[-*+]\\s+", "");

        // 删除引用符号 >
        result = result.replaceAll("(?m)^\\s*>\\s?", "");

        // 删除代码块符号 ```
        result = result.replace("```", "");

        // 删除行尾多余空格
        result = result.replaceAll("[ \\t]+\\n", "\n");

        // 多个空行压缩成两个换行
        result = result.replaceAll("\\n{3,}", "\n\n");

        return result.trim();
    }
    private String safe(String value) {
        return value == null ? "" : value;
    }
}