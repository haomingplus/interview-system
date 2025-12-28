package com.interviewkb.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.interviewkb.common.result.PageResult;
import com.interviewkb.dto.response.StatisticsResponse;
import com.interviewkb.entity.LearningProgress;
import com.interviewkb.entity.LearningRecord;
import com.interviewkb.entity.Question;
import com.interviewkb.mapper.LearningProgressMapper;
import com.interviewkb.mapper.LearningRecordMapper;
import com.interviewkb.mapper.QuestionMapper;
import com.interviewkb.service.LearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LearningServiceImpl implements LearningService {

    private final LearningProgressMapper progressMapper;
    private final LearningRecordMapper recordMapper;
    private final QuestionMapper questionMapper;

    @Override
    public LearningProgress getProgress(Long userId, Long questionId) {
        return progressMapper.selectByUserAndQuestion(userId, questionId);
    }

    @Override
    @Transactional
    public void updateProgress(Long userId, Long questionId, Integer status, String note) {
        LearningProgress progress = progressMapper.selectByUserAndQuestion(userId, questionId);

        if (progress == null) {
            progress = new LearningProgress();
            progress.setUserId(userId);
            progress.setQuestionId(questionId);
            progress.setStatus(status);
            progress.setNote(note);
            progress.setStudyCount(1);
            progress.setLastStudyTime(LocalDateTime.now());
            progressMapper.insert(progress);
        } else {
            progress.setStatus(status);
            if (note != null) {
                progress.setNote(note);
            }
            progress.setStudyCount(progress.getStudyCount() + 1);
            progress.setLastStudyTime(LocalDateTime.now());
            progressMapper.updateById(progress);
        }
    }

    @Override
    @Transactional
    public void recordStudy(Long userId, Long questionId, String action, Integer duration) {
        Question question = questionMapper.selectById(questionId);

        LearningRecord record = new LearningRecord();
        record.setUserId(userId);
        record.setQuestionId(questionId);
        record.setCategoryId(question != null ? question.getCategoryId() : null);
        record.setAction(action);
        record.setDuration(duration != null ? duration : 0);
        record.setStudyDate(LocalDate.now());

        recordMapper.insert(record);

        // 更新学习进度中的累计时长
        LearningProgress progress = progressMapper.selectByUserAndQuestion(userId, questionId);
        if (progress != null && duration != null) {
            progress.setTotalStudyDuration(progress.getTotalStudyDuration() + duration);
            progressMapper.updateById(progress);
        }
    }

    @Override
    public PageResult<LearningProgress> getProgressList(Long userId, Integer status, int page, int size) {
        Page<LearningProgress> pageParam = new Page<>(page, size);
        IPage<LearningProgress> result = progressMapper.selectPageByUserAndStatus(pageParam, userId, status);
        return PageResult.of(result);
    }

    @Override
    public StatisticsResponse getStatistics(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(30);
        LocalDate yearStart = today.minusDays(365);

        // 获取各状态的题目数量
        int masteredCount = progressMapper.countByUserAndStatus(userId, 2);
        int studyingCount = progressMapper.countByUserAndStatus(userId, 1);
        int needReviewCount = progressMapper.countByUserAndStatus(userId, 3);
        int totalCount = masteredCount + studyingCount + needReviewCount +
                progressMapper.countByUserAndStatus(userId, 0);

        // 获取连续学习天数
        int consecutiveDays = recordMapper.countConsecutiveDays(userId, today);

        // 获取今日学习数量
        int todayStudied = recordMapper.countTodayStudied(userId, today);

        // 获取每日学习统计
        List<Map<String, Object>> dailyData = recordMapper.selectDailyStats(userId, startDate, today);
        List<StatisticsResponse.DailyStats> dailyStats = new ArrayList<>();
        for (Map<String, Object> data : dailyData) {
            dailyStats.add(StatisticsResponse.DailyStats.builder()
                    .date(data.get("date").toString())
                    .count(((Number) data.get("count")).intValue())
                    .duration(((Number) data.get("duration")).intValue())
                    .build());
        }

        // 获取分类统计
        List<Map<String, Object>> categoryData = recordMapper.selectCategoryStats(userId);
        List<StatisticsResponse.CategoryStats> categoryStats = new ArrayList<>();
        for (Map<String, Object> data : categoryData) {
            int total = ((Number) data.get("total")).intValue();
            int mastered = ((Number) data.get("mastered")).intValue();
            categoryStats.add(StatisticsResponse.CategoryStats.builder()
                    .name(data.get("name").toString())
                    .total(total)
                    .mastered(mastered)
                    .rate(total > 0 ? (double) mastered / total * 100 : 0)
                    .build());
        }

        // 获取热力图数据
        Map<String, Integer> heatMapData = recordMapper.selectHeatMapData(userId, yearStart, today);
        if (heatMapData == null) {
            heatMapData = new HashMap<>();
        }

        return StatisticsResponse.builder()
                .totalQuestions(totalCount)
                .masteredQuestions(masteredCount)
                .studyingQuestions(studyingCount)
                .needReviewQuestions(needReviewCount)
                .consecutiveDays(consecutiveDays)
                .todayStudied(todayStudied)
                .totalStudyDays(dailyStats.size())
                .dailyStats(dailyStats)
                .categoryStats(categoryStats)
                .heatMapData(heatMapData)
                .build();
    }
}
