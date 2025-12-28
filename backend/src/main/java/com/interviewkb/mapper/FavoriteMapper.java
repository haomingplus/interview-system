package com.interviewkb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.interviewkb.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    IPage<Favorite> selectPageByFolderId(Page<Favorite> page, @Param("folderId") Long folderId);

    Favorite selectByUserAndQuestion(@Param("userId") Long userId, @Param("questionId") Long questionId);

    int countByFolderId(@Param("folderId") Long folderId);
}
