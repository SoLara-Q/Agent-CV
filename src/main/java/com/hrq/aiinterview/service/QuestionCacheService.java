package com.hrq.aiinterview.service;

import com.hrq.aiinterview.entity.QuestionBank;

import java.util.List;

public interface QuestionCacheService {
    List<QuestionBank> getHotQuestions();

    void clearHotQuestionCache();

    boolean isRedisEnabled();

    boolean isRedisAvailable();

    String getCacheMode();
}
