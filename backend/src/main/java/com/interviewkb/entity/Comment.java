package com.interviewkb.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment")
public class Comment extends BaseEntity {

    private Long questionId;
    private Long userId;
    private String content;
    private Long parentId;
    private Long replyToUserId;
    private Long rootId;
    private Integer likeCount;
    private Integer replyCount;
    private Integer status;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String userAvatar;

    @TableField(exist = false)
    private String replyToUsername;

    @TableField(exist = false)
    private List<Comment> replies;

    @TableField(exist = false)
    private Boolean isLiked;
}
