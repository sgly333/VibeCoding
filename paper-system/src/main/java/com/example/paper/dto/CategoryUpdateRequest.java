package com.example.paper.dto;

import jakarta.validation.constraints.NotBlank;

public class CategoryUpdateRequest {

    @NotBlank
    private String name;

    /**
     * hex 颜色，例如：#2f6fed（可选）
     */
    private String themeColor;

    /**
     * 分类描述（可选）
     */
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

