package com.interviewkb.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class StatisticsResponse {
    private Integer totalQuestions;
    private Integer masteredQuestions;
    private Integer studyingQuestions;
    private Integer needReviewQuestions;
    private Integer consecutiveDays;
    private Integer todayStudied;
    private Integer totalStudyDays;

    private List<DailyStats> dailyStats;
    private List<CategoryStats> categoryStats;
    private Map<String, Integer> heatMapData;

    @Data
    @Builder
    public static class DailyStats {
        private String date;
        private Integer count;
        private Integer duration;
    }

    @Data
    @Builder
    public static class CategoryStats {
        private String name;
        private Integer total;
        private Integer mastered;
        private Double rate;
    }
}
