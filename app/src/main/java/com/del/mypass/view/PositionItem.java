package com.del.mypass.view;

import com.del.mypass.db.Position;

public class PositionItem implements Comparable<PositionItem> {

    private Position position;

    public PositionItem(Position position) {
        this.position = position;
    }

    public String getName() {
        return position.getName();
    }

    public String getCode() {
        return position.getCode();
    }

    public Long getId() {
        return position.getId();
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(PositionItem o) {
        return o.position.getId().compareTo(position.getId());
    }
}
