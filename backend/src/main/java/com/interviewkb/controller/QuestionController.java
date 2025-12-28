package com.interviewkb.controller;

import com.interviewkb.common.result.PageResult;
import com.interviewkb.common.result.Result;
import com.interviewkb.dto.request.QuestionQueryRequest;
import com.interviewkb.dto.request.QuestionRequest;
import com.interviewkb.entity.Question;
import com.interviewkb.security.UserPrincipal;
import com.interviewkb.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "题目管理", description = "题目的增删改查")
@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "获取题目详情")
    @GetMapping("/{id}")
    public Result<Question> getDetail(@PathVariable Long id) {
        return Result.success(questionService.getDetailById(id));
    }

    @Operation(summary = "分页查询题目")
    @GetMapping
    public Result<PageResult<Question>> getPage(QuestionQueryRequest query) {
        return Result.success(questionService.getPage(query));
    }

    @Operation(summary = "全文搜索题目")
    @GetMapping("/search")
    public Result<List<Question>> search(@RequestParam String keyword) {
        return Result.success(questionService.search(keyword));
    }

    @Operation(summary = "创建题目")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody QuestionRequest request,
                               @AuthenticationPrincipal UserPrincipal user) {
        return Result.success(questionService.createQuestion(request, user.getId()));
    }

    @Operation(summary = "更新题目")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody QuestionRequest request,
                               @AuthenticationPrincipal UserPrincipal user) {
        questionService.updateQuestion(id, request, user.getId());
        return Result.success();
    }

    @Operation(summary = "删除题目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @AuthenticationPrincipal UserPrincipal user) {
        questionService.deleteQuestion(id, user.getId());
        return Result.success();
    }

    @Operation(summary = "点赞题目")
    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id,
                             @AuthenticationPrincipal UserPrincipal user) {
        questionService.likeQuestion(id, user.getId());
        return Result.success();
    }

    @Operation(summary = "取消点赞")
    @DeleteMapping("/{id}/like")
    public Result<Void> unlike(@PathVariable Long id,
                               @AuthenticationPrincipal UserPrincipal user) {
        questionService.unlikeQuestion(id, user.getId());
        return Result.success();
    }

    @Operation(summary = "检查是否已点赞")
    @GetMapping("/{id}/like/status")
    public Result<Boolean> isLiked(@PathVariable Long id,
                                   @AuthenticationPrincipal UserPrincipal user) {
        return Result.success(questionService.isLiked(id, user.getId()));
    }
}
