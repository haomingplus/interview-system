package com.interviewkb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("learning_progress")
public class LearningProgress implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long questionId;
    private Integer status;  // 0-未学 1-学习中 2-已掌握 3-需复习
    private Integer studyCount;
    private LocalDateTime lastStudyTime;
    private Integer totalStudyDuration;
    private String note;
    private Integer masteryLevel;
    private LocalDateTime nextReviewTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private Question question;
}
