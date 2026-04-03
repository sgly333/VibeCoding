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

    public Set<PaperCategory> getPaperCategories() {
        return paperCategories;
    }

    public void setPaperCategories(Set<PaperCategory> paperCategories) {
        this.paperCategories = paperCategories;
    }
}

