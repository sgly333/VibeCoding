package com.example.paper.repository;

import com.example.paper.entity.PaperCategory;
import com.example.paper.entity.PaperCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaperCategoryRepository extends JpaRepository<PaperCategory, PaperCategoryId> {
    long countByIdCategoryId(Integer categoryId);
}

