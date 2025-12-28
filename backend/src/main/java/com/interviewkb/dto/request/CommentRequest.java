package com.interviewkb.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {
    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Long parentId = 0L;

    private Long replyToUserId;
}
