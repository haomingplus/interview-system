package com.interviewkb.controller;

import com.interviewkb.common.result.PageResult;
import com.interviewkb.common.result.Result;
import com.interviewkb.dto.response.StatisticsResponse;
import com.interviewkb.entity.LearningProgress;
import com.interviewkb.security.UserPrincipal;
import com.interviewkb.service.LearningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "学习管理", description = "学习进度追踪和统计")
@RestController
@RequestMapping("/learning")
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;

    @Operation(summary = "获取题目学习进度")
    @GetMapping("/progress/{questionId}")
    public Result<LearningProgress> getProgress(@PathVariable Long questionId,
                                                 @AuthenticationPrincipal UserPrincipal user) {
        return Result.success(learningService.getProgress(user.getId(), questionId));
    }

    @Operation(summary = "更新学习进度")
    @PostMapping("/progress/{questionId}")
    public Result<Void> updateProgress(@PathVariable Long questionId,
                                        @RequestParam Integer status,
                                        @RequestParam(required = false) String note,
                                        @AuthenticationPrincipal UserPrincipal user) {
        learningService.updateProgress(user.getId(), questionId, status, note);
        return Result.success();
    }

    @Operation(summary = "记录学习行为")
    @PostMapping("/record")
    public Result<Void> recordStudy(@RequestParam Long questionId,
                                     @RequestParam String action,
                                     @RequestParam(required = false) Integer duration,
                                     @AuthenticationPrincipal UserPrincipal user) {
        learningService.recordStudy(user.getId(), questionId, action, duration);
        return Result.success();
    }

    @Operation(summary = "获取学习进度列表")
    @GetMapping("/progress")
    public Result<PageResult<LearningProgress>> getProgressList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal user) {
        return Result.success(learningService.getProgressList(user.getId(), status, page, size));
    }

    @Operation(summary = "获取学习统计")
    @GetMapping("/statistics")
    public Result<StatisticsResponse> getStatistics(@AuthenticationPrincipal UserPrincipal user) {
        return Result.success(learningService.getStatistics(user.getId()));
    }
}
