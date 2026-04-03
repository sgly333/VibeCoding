package com.example.paper.controller;

import com.example.paper.dto.CategoryCreateRequest;
import com.example.paper.dto.CategoryDTO;
import com.example.paper.dto.CategoryUpdateRequest;
import com.example.paper.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public List<CategoryDTO> list() {
        return categoryService.listCategories();
    }

    @PostMapping(value = "/categories", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDTO add(@RequestBody CategoryCreateRequest request) {
        return categoryService.addCategory(request);
    }

    @PutMapping(value = "/categories/{id}", consumes = "application/json")
    public CategoryDTO update(@PathVariable("id") Integer id, @Valid @RequestBody CategoryUpdateRequest request) {
        return categoryService.updateCategory(id, request);
    }

    @DeleteMapping("/categories/{id}")
    public void delete(@PathVariable("id") Integer id) {
        categoryService.deleteCategory(id);
    }
}

