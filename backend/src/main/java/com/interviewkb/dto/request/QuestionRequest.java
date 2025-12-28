package com.interviewkb.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class QuestionRequest {
    @NotBlank(message = "题目标题不能为空")
    private String title;

    private String content;

    private String answer;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    private Integer difficulty = 2;

    private String source;

    private String sourceUrl;

    private List<Long> tagIds;

    private Integer status = 1;

    private Integer isTop = 0;

    private Integer isRecommend = 0;
}
