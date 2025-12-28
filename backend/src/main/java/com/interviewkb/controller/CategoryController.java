package com.interviewkb.controller;

import com.interviewkb.common.result.Result;
import com.interviewkb.entity.Category;
import com.interviewkb.security.UserPrincipal;
import com.interviewkb.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类管理", description = "知识分类的增删改查")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public Result<List<Category>> getTree() {
        return Result.success(categoryService.getTree());
    }

    @Operation(summary = "获取子分类")
    @GetMapping
    public Result<List<Category>> getByParentId(@RequestParam(defaultValue = "0") Long parentId) {
        return Result.success(categoryService.getByParentId(parentId));
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public Result<Void> create(@RequestBody Category category,
                               @AuthenticationPrincipal UserPrincipal user) {
        categoryService.createCategory(category, user.getId());
        return Result.success();
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody Category category) {
        categoryService.updateCategory(id, category);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}
