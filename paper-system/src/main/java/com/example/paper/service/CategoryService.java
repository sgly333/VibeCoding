package com.example.paper.service;

import com.example.paper.dto.CategoryCreateRequest;
import com.example.paper.dto.CategoryDTO;
import com.example.paper.dto.CategoryUpdateRequest;
import com.example.paper.entity.Category;
import com.example.paper.exception.ApiException;
import com.example.paper.repository.CategoryRepository;
import com.example.paper.repository.PaperCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final PaperCategoryRepository paperCategoryRepository;

    public CategoryService(CategoryRepository categoryRepository, PaperCategoryRepository paperCategoryRepository) {
        this.categoryRepository = categoryRepository;
        this.paperCategoryRepository = paperCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> listCategories() {
        return categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(Category::getId, Comparator.nullsLast(Integer::compareTo)))
                .filter(c -> c.getName() != null && !c.getName().trim().isEmpty())
                .map(c -> new CategoryDTO(c.getId(), c.getName(), c.getThemeColor(), c.getDescription()))
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDTO addCategory(CategoryCreateRequest req) {
        String name = req.getName() == null ? "" : req.getName().trim();
        if (name.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "category name is empty");
        }

        if (categoryRepository.existsByName(name)) {
            throw new ApiException(HttpStatus.CONFLICT, "category already exists: " + name);
        }

        String themeColor = normalizeHexColor(req.getThemeColor());
        String description = normalizeDescription(req.getDescription());

        Category c = new Category();
        c.setName(name);
        c.setThemeColor(themeColor);
        c.setDescription(description);
        categoryRepository.save(c);
        return new CategoryDTO(c.getId(), c.getName(), c.getThemeColor(), c.getDescription());
    }

    @Transactional
    public CategoryDTO updateCategory(Integer id, CategoryUpdateRequest req) {
        if (id == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "id is required");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "category not found: " + id));

        String newName = req.getName() == null ? "" : req.getName().trim();
        if (newName.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "category name is empty");
        }

        if (!newName.equals(category.getName()) && categoryRepository.existsByName(newName)) {
            throw new ApiException(HttpStatus.CONFLICT, "category already exists: " + newName);
        }

        String themeColor = normalizeHexColor(req.getThemeColor());
        String description = normalizeDescription(req.getDescription());
        category.setName(newName);
        category.setThemeColor(themeColor);
        category.setDescription(description);

        Category saved = categoryRepository.save(category);
        return new CategoryDTO(saved.getId(), saved.getName(), saved.getThemeColor(), saved.getDescription());
    }

    @Transactional
    public void deleteCategory(Integer id) {
        if (id == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "id is required");
        }
        long refCount = paperCategoryRepository.countByIdCategoryId(id);
        if (refCount > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "该分类下有论文无法删除");
        }
        if (!categoryRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private String normalizeHexColor(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;
        if (!s.startsWith("#")) s = "#" + s;
        if (!s.matches("^#[0-9a-fA-F]{6}$")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid themeColor, expected hex like #2f6fed");
        }
        return s.toLowerCase();
    }

    private String normalizeDescription(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;
        if (s.length() > 1000) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "description too long, max 1000 chars");
        }
        return s;
    }
}

