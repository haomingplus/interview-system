package com.interviewkb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interviewkb.entity.FavoriteFolder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavoriteFolderMapper extends BaseMapper<FavoriteFolder> {

    List<FavoriteFolder> selectByUserId(@Param("userId") Long userId);

    FavoriteFolder selectDefaultByUserId(@Param("userId") Long userId);

    void updateItemCount(@Param("id") Long id, @Param("count") Integer count);
}
