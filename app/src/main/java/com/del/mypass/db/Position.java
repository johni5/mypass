package com.del.mypass.db;

import com.del.mypass.utils.Utils;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Позиция
 */
@Table(name = "POSITION",
        uniqueConstraints = @UniqueConstraint(columnNames = {"NAME", "CODE"}))
@Entity(name = "Position")
public class Position implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Basic
    @Column(name = "NAME", nullable = false)
    private String name;

    @Basic
    @Column(name = "CODE", nullable = false)
    private String code;

    @Basic
    @Column(name = "CATEGORY")
    private String category;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
//        return String.format("[%s] %s -> %s", id, Utils.nvl(category, "-"), name);
        return name;
    }
}
