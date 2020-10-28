package com.del.mypass.view;

import com.del.mypass.db.Position;

public class PositionItem {

    private String name;
    private Integer category;
    private Position position;

    public PositionItem(String name, Integer category, Position position) {
        this.name = name;
        this.category = category;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return position.getCode();
    }

    public Long getId() {
        return position.getId();
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return name;
    }

}
