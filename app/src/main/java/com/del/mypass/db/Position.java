package com.del.mypass.db;

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

    @Override
    public String toString() {
        return name;
    }
}
