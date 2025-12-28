package com.interviewkb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interviewkb.entity.LearningRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface LearningRecordMapper extends BaseMapper<LearningRecord> {

    List<Map<String, Object>> selectDailyStats(@Param("userId") Long userId,
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<Map<String, Object>> selectCategoryStats(@Param("userId") Long userId);

    Map<String, Integer> selectHeatMapData(@Param("userId") Long userId,
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    int countConsecutiveDays(@Param("userId") Long userId, @Param("date") LocalDate date);

    int countTodayStudied(@Param("userId") Long userId, @Param("date") LocalDate date);
}
