package com.interviewkb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.interviewkb.dto.request.QuestionQueryRequest;
import com.interviewkb.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    Question selectByIdWithTags(@Param("id") Long id);

    IPage<Question> selectPageWithTags(Page<Question> page, @Param("query") QuestionQueryRequest query);

    List<Question> selectByTagId(@Param("tagId") Long tagId);

    List<Question> fullTextSearch(@Param("keyword") String keyword);

    void incrementViewCount(@Param("id") Long id);

    void updateLikeCount(@Param("id") Long id, @Param("count") Integer count);

    void updateCollectCount(@Param("id") Long id, @Param("count") Integer count);

    void updateCommentCount(@Param("id") Long id, @Param("count") Integer count);
}
