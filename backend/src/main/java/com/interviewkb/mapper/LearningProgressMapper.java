package com.interviewkb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.interviewkb.entity.LearningProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface LearningProgressMapper extends BaseMapper<LearningProgress> {

    LearningProgress selectByUserAndQuestion(@Param("userId") Long userId, @Param("questionId") Long questionId);

    IPage<LearningProgress> selectPageByUserAndStatus(Page<LearningProgress> page,
            @Param("userId") Long userId, @Param("status") Integer status);

    Map<String, Object> selectStatsByUser(@Param("userId") Long userId);

    int countByUserAndStatus(@Param("userId") Long userId, @Param("status") Integer status);
}
