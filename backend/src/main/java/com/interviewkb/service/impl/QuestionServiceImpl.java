package com.interviewkb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interviewkb.common.exception.BusinessException;
import com.interviewkb.common.result.PageResult;
import com.interviewkb.common.result.ResultCode;
import com.interviewkb.dto.request.QuestionQueryRequest;
import com.interviewkb.dto.request.QuestionRequest;
import com.interviewkb.entity.Question;
import com.interviewkb.mapper.QuestionMapper;
import com.interviewkb.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    private final QuestionMapper questionMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String QUESTION_LIKE_PREFIX = "question:like:";
    private static final String QUESTION_VIEW_PREFIX = "question:view:";

    @Override
    public Question getDetailById(Long id) {
        Question question = questionMapper.selectByIdWithTags(id);
        if (question == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        // 异步增加浏览量
        incrementViewCount(id);
        return question;
    }

    @Override
    public PageResult<Question> getPage(QuestionQueryRequest query) {
        Page<Question> page = new Page<>(query.getPage(), query.getSize());
        IPage<Question> result = questionMapper.selectPageWithTags(page, query);
        return PageResult.of(result);
    }

    @Override
    @Transactional
    public Long createQuestion(QuestionRequest request, Long userId) {
        Question question = new Question();
        question.setTitle(request.getTitle());
        question.setContent(request.getContent());
        question.setAnswer(request.getAnswer());
        question.setCategoryId(request.getCategoryId());
        question.setDifficulty(request.getDifficulty());
        question.setSource(request.getSource());
        question.setSourceUrl(request.getSourceUrl());
        question.setStatus(request.getStatus());
        question.setIsTop(request.getIsTop());
        question.setIsRecommend(request.getIsRecommend());
        question.setCreatedBy(userId);
        question.setViewCount(0);
        question.setLikeCount(0);
        question.setCollectCount(0);
        question.setCommentCount(0);

        questionMapper.insert(question);

        // 保存标签关联
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            saveQuestionTags(question.getId(), request.getTagIds());
        }

        return question.getId();
    }

    @Override
    @Transactional
    public void updateQuestion(Long id, QuestionRequest request, Long userId) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        question.setTitle(request.getTitle());
        question.setContent(request.getContent());
        question.setAnswer(request.getAnswer());
        question.setCategoryId(request.getCategoryId());
        question.setDifficulty(request.getDifficulty());
        question.setSource(request.getSource());
        question.setSourceUrl(request.getSourceUrl());
        question.setStatus(request.getStatus());
        question.setIsTop(request.getIsTop());
        question.setIsRecommend(request.getIsRecommend());

        questionMapper.updateById(question);

        // 更新标签关联
        if (request.getTagIds() != null) {
            deleteQuestionTags(id);
            if (!request.getTagIds().isEmpty()) {
                saveQuestionTags(id, request.getTagIds());
            }
        }
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id, Long userId) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        questionMapper.deleteById(id);
        deleteQuestionTags(id);
    }

    @Override
    public List<Question> search(String keyword) {
        return questionMapper.fullTextSearch(keyword);
    }

    @Override
    public void likeQuestion(Long id, Long userId) {
        String key = QUESTION_LIKE_PREFIX + id;
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId);
        if (Boolean.TRUE.equals(isMember)) {
            return;
        }
        redisTemplate.opsForSet().add(key, userId);
        questionMapper.updateLikeCount(id, 1);
    }

    @Override
    public void unlikeQuestion(Long id, Long userId) {
        String key = QUESTION_LIKE_PREFIX + id;
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId);
        if (Boolean.FALSE.equals(isMember)) {
            return;
        }
        redisTemplate.opsForSet().remove(key, userId);
        questionMapper.updateLikeCount(id, -1);
    }

    @Override
    public boolean isLiked(Long id, Long userId) {
        String key = QUESTION_LIKE_PREFIX + id;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, userId));
    }

    private void incrementViewCount(Long id) {
        String key = QUESTION_VIEW_PREFIX + id;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count >= 10) {
            questionMapper.incrementViewCount(id);
            redisTemplate.delete(key);
        }
    }

    private void saveQuestionTags(Long questionId, List<Long> tagIds) {
        // 实际实现需要插入question_tag表
        log.debug("保存题目标签: questionId={}, tagIds={}", questionId, tagIds);
    }

    private void deleteQuestionTags(Long questionId) {
        // 实际实现需要删除question_tag表中的关联
        log.debug("删除题目标签: questionId={}", questionId);
    }
}
