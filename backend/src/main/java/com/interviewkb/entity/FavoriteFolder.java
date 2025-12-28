package com.interviewkb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("favorite_folder")
public class FavoriteFolder extends BaseEntity {

    private Long userId;
    private String name;
    private String description;
    private String cover;
    private Integer isPublic;
    private Integer isDefault;
    private Integer itemCount;
    private Integer sort;
}
