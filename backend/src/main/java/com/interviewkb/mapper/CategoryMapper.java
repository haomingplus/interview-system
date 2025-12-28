package com.interviewkb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interviewkb.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    List<Category> selectAllWithChildren();

    List<Category> selectByParentId(@Param("parentId") Long parentId);

    void updateQuestionCount(@Param("id") Long id, @Param("count") Integer count);
}
