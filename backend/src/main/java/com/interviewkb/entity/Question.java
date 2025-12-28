package com.interviewkb.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question")
public class Question extends BaseEntity {

    private String title;
    private String content;
    private String answer;
    private Long categoryId;
    private Integer difficulty;
    private String source;
    private String sourceUrl;
    private Integer viewCount;
    private Integer likeCount;
    private Integer collectCount;
    private Integer commentCount;
    private Integer status;
    private Integer isTop;
    private Integer isRecommend;
    private Long createdBy;

    @TableField(exist = false)
    private String categoryName;

    @TableField(exist = false)
    private String creatorName;

    @TableField(exist = false)
    private List<Tag> tags;

    @TableField(exist = false)
    private Boolean isLiked;

    @TableField(exist = false)
    private Boolean isCollected;

    @TableField(exist = false)
    private Integer studyStatus;
}
