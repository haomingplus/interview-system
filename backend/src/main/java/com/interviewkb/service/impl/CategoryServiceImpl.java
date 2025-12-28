package com.interviewkb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interviewkb.common.exception.BusinessException;
import com.interviewkb.common.result.ResultCode;
import com.interviewkb.entity.Category;
import com.interviewkb.entity.Question;
import com.interviewkb.mapper.CategoryMapper;
import com.interviewkb.mapper.QuestionMapper;
import com.interviewkb.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final QuestionMapper questionMapper;

    @Override
    @Cacheable(value = "category", key = "'tree'")
    public List<Category> getTree() {
        List<Category> allCategories = categoryMapper.selectAllWithChildren();
        return buildTree(allCategories, 0L);
    }

    @Override
    public List<Category> getByParentId(Long parentId) {
        return categoryMapper.selectByParentId(parentId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "category", allEntries = true)
    public void createCategory(Category category, Long userId) {
        // 设置层级
        if (category.getParentId() == null || category.getParentId() == 0) {
            category.setParentId(0L);
            category.setLevel(1);
        } else {
            Category parent = categoryMapper.selectById(category.getParentId());
            if (parent == null) {
                throw new BusinessException("父分类不存在");
            }
            category.setLevel(parent.getLevel() + 1);
            if (category.getLevel() > 3) {
                throw new BusinessException("最多支持三级分类");
            }
        }
        category.setCreatedBy(userId);
        category.setQuestionCount(0);
        category.setStatus(1);
        categoryMapper.insert(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = "category", allEntries = true)
    public void updateCategory(Long id, Category category) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        category.setId(id);
        categoryMapper.updateById(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = "category", allEntries = true)
    public void deleteCategory(Long id) {
        // 检查是否有子分类
        Long childCount = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>().eq(Category::getParentId, id)
        );
        if (childCount > 0) {
            throw new BusinessException(ResultCode.CATEGORY_HAS_CHILDREN);
        }

        // 检查是否有题目
        Long questionCount = questionMapper.selectCount(
                new LambdaQueryWrapper<Question>().eq(Question::getCategoryId, id)
        );
        if (questionCount > 0) {
            throw new BusinessException(ResultCode.CATEGORY_HAS_QUESTIONS);
        }

        categoryMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateQuestionCounts() {
        List<Category> categories = categoryMapper.selectList(null);
        for (Category category : categories) {
            Long count = questionMapper.selectCount(
                    new LambdaQueryWrapper<Question>()
                            .eq(Question::getCategoryId, category.getId())
                            .eq(Question::getStatus, 1)
            );
            categoryMapper.updateQuestionCount(category.getId(), count.intValue());
        }
    }

    private List<Category> buildTree(List<Category> categories, Long parentId) {
        Map<Long, List<Category>> groupedByParent = categories.stream()
                .collect(Collectors.groupingBy(Category::getParentId));

        List<Category> roots = groupedByParent.getOrDefault(parentId, new ArrayList<>());
        for (Category root : roots) {
            root.setChildren(buildTree(categories, root.getId()));
        }
        return roots;
    }
}
