package com.interviewkb.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
public class Category extends BaseEntity {

    private String name;
    private Long parentId;
    private Integer level;
    private Integer sort;
    private String icon;
    private String description;
    private Integer questionCount;
    private Integer status;
    private Long createdBy;

    @TableField(exist = false)
    private List<Category> children;

    @TableField(exist = false)
    private String parentName;
}
