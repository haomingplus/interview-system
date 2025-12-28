package com.interviewkb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.interviewkb.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    IPage<Comment> selectRootCommentsByQuestionId(Page<Comment> page, @Param("questionId") Long questionId);

    List<Comment> selectRepliesByRootId(@Param("rootId") Long rootId);

    void updateLikeCount(@Param("id") Long id, @Param("count") Integer count);

    void updateReplyCount(@Param("id") Long id, @Param("count") Integer count);
}
