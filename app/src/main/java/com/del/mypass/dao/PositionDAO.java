package com.del.mypass.dao;

import com.del.mypass.db.Position;
import com.del.mypass.utils.StringUtil;
import com.del.mypass.utils.Unchecked;

import java.util.List;

public class PositionDAO extends AbstractDAO<Position, Long> {

    public PositionDAO(EntityManagerProvider manager) {
        super(manager, Position.class);
    }

    public List<Position> findAll(String filter) {
        if (!StringUtil.isTrimmedEmpty(filter)) {
            return Unchecked.cast(manager().
                    createQuery("from Position p where lower(p.name) like lower(:f) ")
                    .setParameter("f", StringUtil.wrapIfNotEmpty(filter, "%"))
                    .getResultList());
        }
        return Unchecked.cast(manager().createQuery("from Position ").getResultList());
    }

    public Position find(String name) {
        List<Position> list = Unchecked.cast(manager().
                createQuery("from Position p where lower(p.name) like lower(:f) ")
                .setParameter("f", name)
                .getResultList());
        if (list.size() > 1) throw new IllegalArgumentException("Нарушение уникальности по имени: " + name);
        if (list.size() == 0) return null;
        return list.iterator().next();

    }

}
