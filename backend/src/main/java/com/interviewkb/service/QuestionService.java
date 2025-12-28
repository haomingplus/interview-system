package com.interviewkb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.interviewkb.common.result.PageResult;
import com.interviewkb.dto.request.QuestionQueryRequest;
import com.interviewkb.dto.request.QuestionRequest;
import com.interviewkb.entity.Question;

import java.util.List;

public interface QuestionService extends IService<Question> {

    Question getDetailById(Long id);

    PageResult<Question> getPage(QuestionQueryRequest query);

    Long createQuestion(QuestionRequest request, Long userId);

    void updateQuestion(Long id, QuestionRequest request, Long userId);

    void deleteQuestion(Long id, Long userId);

    List<Question> search(String keyword);

    void likeQuestion(Long id, Long userId);

    void unlikeQuestion(Long id, Long userId);

    boolean isLiked(Long id, Long userId);
}
