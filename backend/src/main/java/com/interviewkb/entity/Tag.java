package com.interviewkb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tag")
public class Tag extends BaseEntity {

    private String name;
    private String color;
    private String description;
    private Integer questionCount;
    private Long createdBy;
}
