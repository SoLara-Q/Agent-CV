package com.hrq.aiinterview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hrq.aiinterview.entity.QuestionBank;
import com.hrq.aiinterview.mapper.QuestionBankMapper;
import com.hrq.aiinterview.service.QuestionCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;

/**
 * 高频题目缓存服务。
 *
 * Redis 演示逻辑：
 * 1. app.cache.use-redis=true 时，优先从 Redis 读取高浏览题目。
 * 2. Redis 中没有数据时，从 question_bank 查询浏览量最高的 5 道题，并写入 Redis，过期时间 10 分钟。
 * 3. 新增、修改、删除或查看题目后，会清理 Redis 缓存，保证展示数据更新。
 * 4. Redis 没启动时不会影响系统运行，会自动降级为本地内存缓存和数据库查询。
 */
@Service
public class QuestionCacheServiceImpl implements QuestionCacheService {

    private static final Logger log = LoggerFactory.getLogger(QuestionCacheServiceImpl.class);
    private static final String HOT_QUESTION_KEY = "ai-interview:question-bank:hot-questions";
    private static final long LOCAL_CACHE_MILLIS = 10 * 60 * 1000L;

    private final QuestionBankMapper questionBankMapper;
    private final StringRedisTemplate redisTemplate;
    private final boolean useRedis;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private List<QuestionBank> localHotQuestionsCache;
    private long localCacheExpireAt;

    public QuestionCacheServiceImpl(QuestionBankMapper questionBankMapper,
                                    ObjectProvider<StringRedisTemplate> redisTemplateProvider,
                                    @Value("${app.cache.use-redis:false}") boolean useRedis) {
        this.questionBankMapper = questionBankMapper;
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
        this.useRedis = useRedis;
    }

    @Override
    public List<QuestionBank> getHotQuestions() {
        List<QuestionBank> cacheResult = getFromCache();
        if (cacheResult != null) {
            return cacheResult;
        }

        List<QuestionBank> list = questionBankMapper.selectList(new LambdaQueryWrapper<QuestionBank>()
                .orderByDesc(QuestionBank::getViewCount)
                .orderByDesc(QuestionBank::getCreateTime)
                .last("limit 5"));

        saveToCache(list);
        return list;
    }

    @Override
    public void clearHotQuestionCache() {
        localHotQuestionsCache = null;
        localCacheExpireAt = 0;
        if (!useRedis || redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.delete(HOT_QUESTION_KEY);
            log.info("已清理 Redis 高频题目缓存，key={}", HOT_QUESTION_KEY);
        } catch (Exception e) {
            log.warn("清理 Redis 缓存失败，已自动忽略：{}", e.getMessage());
        }
    }

    @Override
    public boolean isRedisEnabled() {
        return useRedis;
    }

    @Override
    public boolean isRedisAvailable() {
        if (!useRedis || redisTemplate == null || redisTemplate.getConnectionFactory() == null) {
            return false;
        }
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        try (RedisConnection connection = factory.getConnection()) {
            String pong = connection.ping();
            return "PONG".equalsIgnoreCase(pong);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getCacheMode() {
        if (!useRedis) {
            return "本地内存缓存";
        }
        return isRedisAvailable() ? "Redis 缓存已连接" : "Redis 未连接，已降级";
    }

    private List<QuestionBank> getFromCache() {
        if (useRedis && redisTemplate != null) {
            try {
                String cache = redisTemplate.opsForValue().get(HOT_QUESTION_KEY);
                if (StringUtils.hasText(cache)) {
                    log.info("从 Redis 读取高频题目缓存，key={}", HOT_QUESTION_KEY);
                    return objectMapper.readValue(cache, new TypeReference<List<QuestionBank>>() {});
                }
            } catch (Exception e) {
                log.warn("Redis 读取失败，自动降级：{}", e.getMessage());
            }
        }

        long now = System.currentTimeMillis();
        if (localHotQuestionsCache != null && now < localCacheExpireAt) {
            log.info("从本地内存缓存读取高频题目");
            return localHotQuestionsCache;
        }
        return null;
    }

    private void saveToCache(List<QuestionBank> list) {
        localHotQuestionsCache = list;
        localCacheExpireAt = System.currentTimeMillis() + LOCAL_CACHE_MILLIS;

        if (!useRedis || redisTemplate == null) {
            return;
        }

        try {
            redisTemplate.opsForValue().set(HOT_QUESTION_KEY, objectMapper.writeValueAsString(list), Duration.ofMinutes(10));
            log.info("高频题目已写入 Redis 缓存，key={}，过期时间=10分钟", HOT_QUESTION_KEY);
        } catch (Exception e) {
            log.warn("写入 Redis 失败，不影响主流程：{}", e.getMessage());
        }
    }
}
