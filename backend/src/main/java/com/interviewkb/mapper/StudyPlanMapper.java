package com.interviewkb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interviewkb.entity.StudyPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudyPlanMapper extends BaseMapper<StudyPlan> {

    StudyPlan selectByIdWithQuestions(@Param("id") Long id);

    List<StudyPlan> selectByUserId(@Param("userId") Long userId);

    List<StudyPlan> selectActiveByUserId(@Param("userId") Long userId);

    void updateProgress(@Param("id") Long id);
}
