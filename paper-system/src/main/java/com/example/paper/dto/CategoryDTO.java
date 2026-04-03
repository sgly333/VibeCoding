package com.example.paper.dto;

public class CategoryDTO {
    private Integer id;
    private String name;
    private String themeColor;

    public CategoryDTO() {
    }

    public CategoryDTO(Integer id, String name, String themeColor) {
        this.id = id;
        this.name = name;
        this.themeColor = themeColor;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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
}

