package com.interviewkb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interviewkb.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    List<Tag> selectByQuestionId(@Param("questionId") Long questionId);

    void updateQuestionCount(@Param("id") Long id, @Param("count") Integer count);
}
