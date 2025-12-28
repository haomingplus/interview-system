package com.interviewkb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("learning_record")
public class LearningRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long questionId;
    private Long categoryId;
    private String action;  // VIEW-查看 STUDY-学习 COMPLETE-完成
    private Integer duration;
    private LocalDate studyDate;
    private LocalDateTime createdAt;
}
