package com.interviewkb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.interviewkb.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {

    List<Category> getTree();

    List<Category> getByParentId(Long parentId);

    void createCategory(Category category, Long userId);

    void updateCategory(Long id, Category category);

    void deleteCategory(Long id);

    void updateQuestionCounts();
}
