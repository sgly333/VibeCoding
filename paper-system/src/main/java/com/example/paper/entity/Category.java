package com.example.paper.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    /**
     * 分类主题色（hex），例如：#2f6fed
     */
    @Column(name = "theme_color", length = 16)
    private String themeColor;

    /**
     * 分类描述，供 LLM 分类时参考
     */
    @Column(name = "description", length = 1000)
    private String description;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PaperCategory> paperCategories = new HashSet<>();

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<PaperCategory> getPaperCategories() {
        return paperCategories;
    }

    public void setPaperCategories(Set<PaperCategory> paperCategories) {
        this.paperCategories = paperCategories;
    }
}

