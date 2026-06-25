package com.hrq.aiinterview.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrq.aiinterview.entity.QuestionBank;
import com.hrq.aiinterview.entity.ResumeInfo;
import com.hrq.aiinterview.entity.SysUser;
import com.hrq.aiinterview.mapper.QuestionBankMapper;
import com.hrq.aiinterview.mapper.ResumeInfoMapper;
import com.hrq.aiinterview.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;
    private final QuestionBankMapper questionBankMapper;
    private final ResumeInfoMapper resumeInfoMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initUsers();
        initQuestions();
        initResume();
    }

    private void initUsers() {
        if (sysUserMapper.selectCount(null) > 0) {
            return;
        }
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setNickname("管理员");
        admin.setRole("ADMIN");
        admin.setEnabled(1);
        admin.setCreateTime(LocalDateTime.now());
        sysUserMapper.insert(admin);

        SysUser user = new SysUser();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setNickname("普通用户");
        user.setRole("USER");
        user.setEnabled(1);
        user.setCreateTime(LocalDateTime.now());
        sysUserMapper.insert(user);
    }

    private void initQuestions() {
        if (questionBankMapper.selectCount(null) > 0) {
            return;
        }
        String[][] data = new String[][] {
                {"Java基础", "Java 中面向对象的三大特性是什么？", "封装、继承、多态。封装隐藏实现细节，继承复用父类能力，多态让同一接口在不同对象上表现出不同行为。", "简单", "Java后端", "Java,面向对象,OOP"},
                {"Java基础", "ArrayList 和 LinkedList 有什么区别？", "ArrayList 底层是动态数组，随机查询快；LinkedList 底层是双向链表，插入删除更灵活，但随机访问较慢。", "简单", "Java后端", "集合,ArrayList,LinkedList"},
                {"Java基础", "HashMap 的底层结构是什么？", "JDK8 以后 HashMap 主要由数组、链表和红黑树组成，发生哈希冲突时先链表存储，链表过长并满足条件后会转成红黑树。", "中等", "Java后端", "HashMap,集合,红黑树"},
                {"Java基础", "== 和 equals 的区别是什么？", "== 比较基本类型的值或对象引用地址；equals 默认比较引用地址，但很多类会重写它用于比较对象内容。", "简单", "Java后端", "equals,对象比较"},
                {"Java基础", "final 关键字可以修饰什么？", "final 可以修饰类、方法和变量。修饰类表示不能被继承，修饰方法表示不能被重写，修饰变量表示只能赋值一次。", "简单", "Java后端", "final,关键字"},
                {"Java基础", "Java 异常体系分为哪几类？", "Java 异常顶层是 Throwable，下面主要分为 Error 和 Exception，Exception 又分为运行时异常和编译期异常。", "简单", "Java后端", "异常,Exception"},
                {"Java基础", "接口和抽象类有什么区别？", "接口更强调能力规范，抽象类更强调代码复用和继承层次。接口可以多实现，抽象类只能单继承。", "中等", "Java后端", "接口,抽象类"},
                {"Java基础", "什么是线程安全？", "多个线程同时访问共享数据时，不会出现数据错乱、脏读或状态不一致，就可以认为是线程安全。", "中等", "Java后端", "多线程,线程安全"},
                {"Java基础", "synchronized 的作用是什么？", "synchronized 用于加锁，保证同一时刻只有一个线程进入临界区，从而保护共享资源的一致性。", "中等", "Java后端", "synchronized,锁"},
                {"Java基础", "JVM 内存区域主要有哪些？", "主要包括堆、方法区、虚拟机栈、本地方法栈、程序计数器，其中堆是对象实例主要存放区域。", "中等", "Java后端", "JVM,内存模型"},
                {"Spring Boot", "Spring Boot 的核心优点是什么？", "Spring Boot 通过自动配置、Starter 依赖和内置 Web 容器简化项目搭建，让开发者能快速构建可运行的 Spring 应用。", "简单", "Java后端", "SpringBoot,自动配置"},
                {"Spring Boot", "Spring Boot 自动配置的核心原理是什么？", "核心是自动配置类、条件注解和 spring.factories/AutoConfiguration.imports，根据类路径和配置条件自动装配 Bean。", "中等", "Java后端", "自动配置,条件注解"},
                {"Spring Boot", "Controller、Service、Mapper 分别负责什么？", "Controller 负责接收请求和返回响应，Service 处理业务逻辑，Mapper 负责数据库访问。", "简单", "Java后端", "分层架构,MVC"},
                {"Spring Boot", "@RestController 和 @Controller 有什么区别？", "@RestController 等价于 @Controller 加 @ResponseBody，适合返回 JSON；@Controller 通常用于返回页面视图。", "简单", "Java后端", "Controller,REST"},
                {"Spring Boot", "@Autowired 和构造器注入有什么区别？", "构造器注入能保证依赖不可变且更利于测试，字段注入写法简单但不利于暴露依赖关系。", "中等", "Java后端", "依赖注入,IOC"},
                {"Spring Boot", "Spring IOC 是什么？", "IOC 是控制反转，Bean 的创建和依赖关系交给 Spring 容器管理，降低对象之间的耦合。", "中等", "Java后端", "IOC,Bean"},
                {"Spring Boot", "Spring AOP 可以用在哪些场景？", "AOP 常用于日志记录、权限校验、事务管理、接口耗时统计和统一异常处理等横切逻辑。", "中等", "Java后端", "AOP,切面"},
                {"Spring Boot", "MyBatis-Plus 相比 MyBatis 有什么优势？", "MyBatis-Plus 提供 BaseMapper、条件构造器、分页插件和通用 CRUD，减少重复 SQL 编写。", "简单", "Java后端", "MyBatisPlus,CRUD"},
                {"Spring Boot", "Spring Boot 项目如何做全局异常处理？", "可以使用 @ControllerAdvice 或 @RestControllerAdvice 配合 @ExceptionHandler 统一捕获异常并返回规范错误信息。", "中等", "Java后端", "异常处理,ControllerAdvice"},
                {"Spring Boot", "Spring Security 在项目中主要做什么？", "Spring Security 负责认证和授权，例如登录校验、密码加密、角色权限控制和退出登录。", "中等", "Java后端", "SpringSecurity,权限"},
                {"MySQL", "MySQL 索引是什么？有什么作用？", "索引是一种提高查询效率的数据结构，常见实现是 B+Tree，可以减少扫描行数，但会增加存储和写入维护成本。", "简单", "Java后端", "索引,B+Tree"},
                {"MySQL", "为什么 InnoDB 常用 B+Tree 索引？", "B+Tree 层级低、磁盘 IO 少，叶子节点有序且链表相连，适合范围查询和排序。", "中等", "Java后端", "InnoDB,B+Tree"},
                {"MySQL", "什么是事务？", "事务是一组要么全部成功、要么全部失败的数据库操作，用于保证数据一致性。", "简单", "Java后端", "事务,ACID"},
                {"MySQL", "事务的 ACID 是什么？", "ACID 指原子性、一致性、隔离性、持久性，是事务可靠执行的四个特征。", "简单", "Java后端", "ACID,事务"},
                {"MySQL", "MySQL 常见隔离级别有哪些？", "读未提交、读已提交、可重复读、串行化。InnoDB 默认通常是可重复读。", "中等", "Java后端", "隔离级别,事务"},
                {"MySQL", "什么是慢查询？如何优化？", "慢查询是执行时间较长的 SQL。优化可从建立合适索引、减少全表扫描、避免 select *、分析执行计划等方面入手。", "中等", "Java后端", "慢查询,SQL优化"},
                {"MySQL", "left join 和 inner join 有什么区别？", "inner join 只返回两表匹配的数据；left join 返回左表全部数据，右表没有匹配则为 NULL。", "简单", "Java后端", "多表查询,join"},
                {"MySQL", "什么情况下索引可能失效？", "对索引列使用函数、like 左模糊、隐式类型转换、or 条件不合理、联合索引不满足最左前缀等都可能导致索引失效。", "中等", "Java后端", "索引失效,SQL"},
                {"MySQL", "varchar 和 char 有什么区别？", "char 是定长字符串，varchar 是变长字符串。char 适合长度固定的数据，varchar 更节省空间。", "简单", "Java后端", "字段类型,char,varchar"},
                {"MySQL", "如何设计一张用户表？", "一般包含 id、username、password、nickname、role、enabled、create_time 等字段，并给 username 加唯一索引。", "简单", "Java后端", "表设计,用户表"},
                {"Redis", "Redis 为什么速度快？", "Redis 基于内存操作，采用高效数据结构，单线程事件循环避免大量锁竞争，同时使用 IO 多路复用处理连接。", "中等", "Java后端", "Redis,性能"},
                {"Redis", "Redis 常见数据类型有哪些？", "常见有 String、Hash、List、Set、ZSet，也支持 Bitmap、HyperLogLog、Stream 等结构。", "简单", "Java后端", "数据结构,Redis"},
                {"Redis", "什么是缓存穿透？如何解决？", "缓存穿透是查询不存在的数据导致请求直接打到数据库，可通过参数校验、缓存空值、布隆过滤器解决。", "中等", "Java后端", "缓存穿透,布隆过滤器"},
                {"Redis", "什么是缓存击穿？如何解决？", "缓存击穿是热点 Key 过期瞬间大量请求访问数据库，可用互斥锁、逻辑过期、热点数据不过期解决。", "中等", "Java后端", "缓存击穿,热点Key"},
                {"Redis", "什么是缓存雪崩？如何解决？", "缓存雪崩是大量 Key 同时失效导致数据库压力骤增，可通过随机过期时间、限流降级、缓存预热解决。", "中等", "Java后端", "缓存雪崩,限流"},
                {"Redis", "Redis 过期策略有哪些？", "Redis 采用惰性删除和定期删除结合的方式处理过期 Key，同时配合内存淘汰策略控制内存。", "中等", "Java后端", "过期策略,淘汰策略"},
                {"Redis", "Redis 如何实现分布式锁？", "通常使用 set key value nx ex 命令加锁，并使用唯一 value 防止误删，复杂场景可使用 Redisson。", "困难", "Java后端", "分布式锁,Redisson"},
                {"Redis", "项目中 Redis 可以缓存什么？", "可以缓存热门面试题、首页统计数据、登录验证码、用户权限信息等访问频繁且变化不大的数据。", "简单", "Java后端", "缓存设计,项目"},
                {"Redis", "Redis 持久化方式有哪些？", "主要有 RDB 和 AOF。RDB 是快照持久化，AOF 是追加日志持久化，二者可以结合使用。", "中等", "Java后端", "RDB,AOF"},
                {"Redis", "Redis 和 MySQL 的关系是什么？", "MySQL 负责可靠持久化存储，Redis 通常作为缓存或高性能数据结构服务，提高读取速度和系统吞吐。", "简单", "Java后端", "缓存,数据库"},
                {"软件测试", "软件测试的目的是什么？", "测试的目的是发现问题、验证功能是否符合需求，并通过缺陷反馈提升软件质量。", "简单", "软件测试", "测试基础,质量"},
                {"软件测试", "测试用例一般包含哪些内容？", "通常包含用例编号、标题、前置条件、测试步骤、测试数据、预期结果、实际结果、优先级等。", "简单", "软件测试", "测试用例"},
                {"软件测试", "功能测试主要测什么？", "功能测试主要验证系统功能是否满足需求，包括正常流程、异常流程、边界值和权限控制。", "简单", "软件测试", "功能测试"},
                {"软件测试", "接口测试主要关注哪些点？", "关注请求方式、URL、参数、请求头、状态码、响应字段、业务逻辑、异常参数、权限校验和响应时间。", "中等", "软件测试", "接口测试,Postman"},
                {"软件测试", "Bug 生命周期是什么？", "常见流程是新建、指派、修复、待验证、已关闭，若验证失败则重新打开。", "简单", "软件测试", "Bug流程"},
                {"软件测试", "黑盒测试和白盒测试有什么区别？", "黑盒关注输入输出和业务功能，不关心内部代码；白盒关注代码逻辑、分支、路径和覆盖率。", "简单", "软件测试", "黑盒,白盒"},
                {"软件测试", "边界值测试是什么？", "边界值测试是针对输入范围边界附近的数据设计用例，因为程序错误常出现在边界位置。", "简单", "软件测试", "边界值"},
                {"软件测试", "等价类划分是什么？", "把输入数据划分为有效等价类和无效等价类，从每类中选代表数据进行测试，提高用例效率。", "简单", "软件测试", "等价类"},
                {"软件测试", "如何测试登录功能？", "可从正确登录、错误密码、空值、账号不存在、账号禁用、验证码、权限跳转、安全限制等角度设计。", "中等", "软件测试", "登录测试"},
                {"软件测试", "JMeter 常用于什么场景？", "JMeter 常用于接口压测、并发测试、性能测试和简单的接口自动化验证。", "简单", "软件测试", "JMeter,性能测试"},
                {"前端基础", "HTML、CSS、JavaScript 分别负责什么？", "HTML 负责结构，CSS 负责样式，JavaScript 负责交互和动态逻辑。", "简单", "前端开发", "HTML,CSS,JavaScript"},
                {"前端基础", "GET 和 POST 有什么区别？", "GET 常用于查询，参数通常在 URL 上；POST 常用于提交数据，请求体承载参数，更适合新增或复杂提交。", "简单", "前端开发", "HTTP,GET,POST"},
                {"前端基础", "Vue 中 v-if 和 v-show 的区别是什么？", "v-if 是条件渲染，会创建或销毁 DOM；v-show 通过 display 控制显示隐藏，适合频繁切换。", "简单", "前端开发", "Vue,v-if,v-show"},
                {"前端基础", "什么是跨域？", "跨域是浏览器同源策略导致不同协议、域名或端口之间的请求受限，常用 CORS 或代理解决。", "中等", "前端开发", "跨域,CORS"},
                {"前端基础", "Axios 的作用是什么？", "Axios 是前端常用 HTTP 请求库，用于向后端发送 GET、POST 等请求并处理响应。", "简单", "前端开发", "Axios,HTTP"},
                {"前端基础", "Thymeleaf 的作用是什么？", "Thymeleaf 是服务端模板引擎，可以把后端 Model 数据渲染到 HTML 页面中。", "简单", "前端开发", "Thymeleaf,模板"},
                {"前端基础", "表单提交需要关注哪些测试点？", "需要关注必填校验、格式校验、长度限制、重复提交、成功提示、失败提示和数据库是否保存正确。", "简单", "软件测试", "表单测试,前端"},
                {"前端基础", "什么是响应式布局？", "响应式布局是页面能根据不同屏幕宽度自动调整结构和样式，提升移动端和桌面端体验。", "简单", "前端开发", "响应式"},
                {"前端基础", "localStorage 和 sessionStorage 有什么区别？", "localStorage 长期保存，除非主动清除；sessionStorage 只在当前会话窗口内有效。", "简单", "前端开发", "浏览器存储"},
                {"前端基础", "如何排查页面按钮点击无反应？", "可以检查浏览器控制台报错、事件绑定、接口请求、权限控制、元素遮挡和后端响应。", "中等", "软件测试", "前端调试,测试"},
                {"算法题", "两数之和怎么做？", "可以用哈希表记录遍历过的数字及下标，遍历时查找 target - nums[i] 是否存在，时间复杂度 O(n)。", "简单", "算法岗/通用", "哈希表,数组"},
                {"算法题", "如何判断一个字符串是否是回文串？", "使用双指针从左右两端向中间移动，依次比较字符是否相等。", "简单", "算法岗/通用", "字符串,双指针"},
                {"算法题", "快速排序的基本思想是什么？", "选择一个基准值，将数组分为小于和大于基准的两部分，再递归排序。", "中等", "算法岗/通用", "排序,快排"},
                {"算法题", "二分查找适用于什么条件？", "二分查找适用于有序数据，每次排除一半搜索范围，时间复杂度 O(log n)。", "简单", "算法岗/通用", "二分"},
                {"算法题", "什么是动态规划？", "动态规划把复杂问题拆成有重叠子问题和最优子结构的小问题，通过状态定义和状态转移求解。", "中等", "算法岗/通用", "动态规划,DP"},
                {"算法题", "最长递增子序列的状态如何定义？", "可以定义 dp[i] 表示以第 i 个元素结尾的最长递增子序列长度，再枚举前面的 j 进行转移。", "中等", "算法岗/通用", "LIS,DP"},
                {"算法题", "BFS 和 DFS 有什么区别？", "BFS 按层扩展，适合求最短步数；DFS 沿路径深入，适合搜索所有方案或连通性。", "中等", "算法岗/通用", "BFS,DFS"},
                {"算法题", "栈适合解决什么问题？", "栈具有后进先出特性，常用于括号匹配、表达式求值、单调栈和函数调用等场景。", "简单", "算法岗/通用", "栈"},
                {"算法题", "什么是时间复杂度？", "时间复杂度描述算法运行时间随输入规模增长的趋势，常用 O(1)、O(log n)、O(n)、O(n²) 表示。", "简单", "算法岗/通用", "复杂度"},
                {"算法题", "如何判断链表是否有环？", "可以使用快慢指针，快指针每次走两步，慢指针每次走一步，如果相遇说明有环。", "中等", "算法岗/通用", "链表,快慢指针"},
                {"项目面试", "请介绍一下你的 AI 简历分析项目。", "该项目是基于 Spring Boot 的 AI 面试刷题与简历优化平台，支持登录注册、题库管理、简历上传解析、AI 优化建议和数据统计。", "中等", "通用", "项目介绍,AI简历"},
                {"项目面试", "这个项目的核心业务流程是什么？", "用户登录后可以管理题库、上传 DOCX 简历，系统解析简历文本并结合目标岗位生成优化建议，同时提供数据看板展示。", "中等", "通用", "业务流程"},
                {"项目面试", "项目中为什么使用 MyBatis-Plus？", "因为项目包含较多基础 CRUD 和分页查询，MyBatis-Plus 可以减少重复代码，提高开发效率。", "简单", "Java后端", "MyBatisPlus,项目"},
                {"项目面试", "项目中 Redis 用来做什么？", "Redis 用于缓存热门面试题，减少数据库查询压力；当 Redis 不可用时，系统可以降级为本地缓存或数据库查询。", "中等", "Java后端", "Redis,项目亮点"},
                {"项目面试", "简历上传功能如何实现？", "前端通过表单上传 DOCX 文件，后端 MultipartFile 接收，保存到 uploads 目录，并使用 Apache POI 读取文档文本。", "中等", "Java后端", "文件上传,Apache POI"},
                {"项目面试", "AI 分析功能如何实现？", "系统把简历技能、项目经历和目标岗位组织成提示词，调用本地 Ollama 或模拟 AI 服务生成岗位匹配度、优势和修改建议。", "中等", "AI应用", "Ollama,提示词"},
                {"项目面试", "项目中最大的难点是什么？", "难点是让简历解析、岗位关键词和 AI 建议形成闭环，同时保证文件上传异常、AI 不可用和缓存不可用时系统仍能正常运行。", "中等", "通用", "项目难点"},
                {"项目面试", "如何保证登录权限安全？", "使用 Spring Security 做认证授权，密码用 BCrypt 加密，管理功能限制 ADMIN 角色访问。", "中等", "Java后端", "权限,安全"},
                {"项目面试", "你如何测试题库 CRUD？", "测试新增、编辑、删除、分页、搜索、分类筛选、必填校验和数据库记录变化，同时检查普通用户和管理员权限差异。", "中等", "软件测试", "CRUD测试"},
                {"项目面试", "这个项目还能怎么优化？", "可以增加错题本、收藏题目、AI 自动生成追问题、简历与题库标签匹配推荐、接口自动化测试和部署脚本。", "简单", "通用", "项目优化"},
                {"HR面试", "请做一个一分钟自我介绍。", "可以按学校专业、技术栈、项目经历、实习/竞赛经历和求职方向组织，重点突出岗位匹配能力。", "简单", "通用", "自我介绍"},
                {"HR面试", "你的优势是什么？", "可以回答有计算机基础、项目实践、测试意识或开发能力，能结合接口、页面和数据库定位问题，学习速度快。", "简单", "通用", "优势"},
                {"HR面试", "你的不足是什么？", "可以说企业级流程经验还需积累，但会主动学习规范、记录问题并快速适应团队协作。", "简单", "通用", "不足"},
                {"HR面试", "为什么选择我们公司？", "可以从岗位方向、业务场景、技术成长空间和自己能力匹配度回答，避免只说想找实习。", "简单", "通用", "求职动机"},
                {"HR面试", "为什么想做软件测试？", "可以说测试不只是点页面，而是从用户和质量角度验证系统，自己的开发基础也有助于接口和数据库定位。", "简单", "软件测试", "测试岗位"},
                {"HR面试", "能接受重复性工作吗？", "可以回答能接受，测试需要耐心和细致，会通过清单、记录和自动化思维提升效率。", "简单", "通用", "职业态度"},
                {"HR面试", "你期望从实习中获得什么？", "希望熟悉真实项目流程、提升工程协作能力，并在功能测试、接口测试或后端开发中承担实际任务。", "简单", "通用", "实习目标"},
                {"HR面试", "遇到不会的问题怎么办？", "先自己查日志、文档和代码定位，再整理现象、复现步骤和尝试方案，必要时向同事请教。", "简单", "通用", "问题解决"},
                {"HR面试", "你如何看待加班？", "可以接受合理项目节点下的加班，但也会尽量通过计划、沟通和效率减少无效加班。", "简单", "通用", "工作态度"},
                {"HR面试", "你还有什么想问的吗？", "可以问实习生主要负责的模块、团队测试流程、导师机制、技术栈和转正/长期培养标准。", "简单", "通用", "反问问题"}
        };
        for (int i = 0; i < data.length; i++) {
            insertQuestion(data[i][0], data[i][1], data[i][2], data[i][3], data[i][4], data[i][5], i);
        }
    }

    private void insertQuestion(String category, String title, String answer, String difficulty, String jobType, String tags, int index) {
        QuestionBank q = new QuestionBank();
        q.setCategory(category);
        q.setTitle(title);
        q.setAnswer(answer);
        q.setDifficulty(difficulty);
        q.setJobType(jobType);
        q.setTags(tags);
        q.setViewCount(index % 7);
        q.setCreateTime(LocalDateTime.now().minusMinutes(index));
        questionBankMapper.insert(q);
    }

    private void initResume() {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "user"));
        if (user == null || resumeInfoMapper.selectCount(null) > 0) {
            return;
        }
        ResumeInfo resume = new ResumeInfo();
        resume.setUserId(user.getId());
        resume.setRealName("黄同学");
        resume.setTargetJob("Java后端开发实习生");
        resume.setSkills("Java、Spring Boot、MyBatis-Plus、MySQL、Redis、Git、Linux");
        resume.setProjectExperience("基于 Spring Boot 实现面试刷题与简历优化平台，包含题库管理、简历分析、数据看板等模块。");
        resume.setAiSuggest("点击 AI 分析按钮后自动生成优化建议。");
        resume.setCreateTime(LocalDateTime.now());
        resume.setUpdateTime(LocalDateTime.now());
        resumeInfoMapper.insert(resume);
    }
}
