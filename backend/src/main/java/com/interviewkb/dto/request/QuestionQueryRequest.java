package com.interviewkb.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class QuestionQueryRequest {
    private String keyword;
    private Long categoryId;
    private Integer difficulty;
    private String source;
    private List<Long> tagIds;
    private Integer status;
    private Integer page = 1;
    private Integer size = 10;
    private String sortField = "createdAt";
    private String sortOrder = "desc";
}
