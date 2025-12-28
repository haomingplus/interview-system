package com.interviewkb.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("study_plan")
public class StudyPlan extends BaseEntity {

    private Long userId;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer dailyTarget;
    private Integer totalQuestions;
    private Integer completedQuestions;
    private BigDecimal progress;
    private Integer status;  // 0-暂停 1-进行中 2-已完成 3-已放弃
    private LocalTime remindTime;
    private Integer remindEnabled;

    @TableField(exist = false)
    private List<Question> questions;
}
