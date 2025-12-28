package com.interviewkb.service;

import com.interviewkb.common.result.PageResult;
import com.interviewkb.dto.response.StatisticsResponse;
import com.interviewkb.entity.LearningProgress;

public interface LearningService {

    LearningProgress getProgress(Long userId, Long questionId);

    void updateProgress(Long userId, Long questionId, Integer status, String note);

    void recordStudy(Long userId, Long questionId, String action, Integer duration);

    PageResult<LearningProgress> getProgressList(Long userId, Integer status, int page, int size);

    StatisticsResponse getStatistics(Long userId);
}
