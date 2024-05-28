package com.del.mypass.dao;

import com.del.mypass.db.Position;
import com.del.mypass.utils.CommonException;
import com.del.mypass.utils.StringUtil;
import com.google.common.collect.Lists;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

public class PositionDAO extends AbstractDAO<Position> {

    public PositionDAO(Connection c) {
        super(c, Position.class);
    }

    public void createAndCommit(Position p) throws CommonException {
        transaction(ps -> {
            ps.setString(1, p.getName());
            ps.setString(2, p.getCode());
            ps.setString(3, p.getCategory());
            ps.executeUpdate();
            return null;
        }, "insert into position (name, code, category) values (?, ?, ?)");
    }

    public void updateAndCommit(Position p) throws CommonException {
        transaction(ps -> {
            ps.setString(1, p.getName());
            ps.setString(2, p.getCode());
            ps.setString(3, p.getCategory());
            ps.setLong(4, p.getId());
            ps.executeUpdate();
            return null;
        }, "update position set name=?, code=?, category=? where id=?");
    }

    public void removeAndCommit(Long id) throws CommonException {
        transaction(ps -> {
            ps.setLong(1, id);
            ps.executeUpdate();
            return null;
        }, "delete from position where id=?");
    }

    public Position get(Long id) throws CommonException {
        Position p = session(ps -> {
            ResultSet rs = ps.executeQuery("select id, name, code, category from position where id=" + id);
            if (rs.next()) {
                Position p1 = new Position();
                p1.setId(rs.getLong(1));
                p1.setName(rs.getString(2));
                p1.setCode(rs.getString(3));
                p1.setCategory(rs.getString(4));
                return p1;
            }
            return null;
        });
        return p;
    }

    public List<Position> findAll(String filter) throws CommonException {
        return session(ps -> {
            String sql = !StringUtil.isTrimmedEmpty(filter) ?
                    String.format("select id, name, code, category from position where lower(name) like lower('%s') order by id",
                            StringUtil.wrapIfNotEmpty(filter, "%")) :
                    "select id, name, code, category from position order by id";
            ResultSet rs = ps.executeQuery(sql);
            List<Position> result = Lists.newArrayList();
            while (rs.next()) {
                Position p1 = new Position();
                p1.setId(rs.getLong(1));
                p1.setName(rs.getString(2));
                p1.setCode(rs.getString(3));
                p1.setCategory(rs.getString(4));
                result.add(p1);
            }
            return result;
        });
    }

    public Position find(final String name) throws CommonException {
        List<Position> list = session(ps -> {
            String sql = String.format("select id, name, code, category from position where lower(name) like lower('%s')", name);
            ResultSet rs = ps.executeQuery(sql);
            List<Position> result = Lists.newArrayList();
            while (rs.next()) {
                Position p1 = new Position();
                p1.setId(rs.getLong(1));
                p1.setName(rs.getString(2));
                p1.setCode(rs.getString(3));
                p1.setCategory(rs.getString(4));
                result.add(p1);
            }
            return result;
        });
        if (list.size() > 1) throw new IllegalArgumentException("Нарушение уникальности по имени: " + name);
        if (list.size() == 0) return null;
        return list.iterator().next();
    }

    public void backup(String path, String pwd) throws CommonException {
        transaction(st -> {
            String sql = String.format("SCRIPT DROP TO '%s' COMPRESSION DEFLATE CIPHER AES PASSWORD '%s' TABLE POSITION", path, pwd);
            st.executeQuery(sql);
            return null;
        });
    }

    public void restore(String path, String pwd) throws CommonException {
        transaction(st -> {
            String sql;
            if (StringUtil.isTrimmedEmpty(pwd)) {
                sql = String.format("RUNSCRIPT FROM '%s' ", path);
            } else {
                sql = String.format("RUNSCRIPT FROM '%s' COMPRESSION DEFLATE CIPHER AES PASSWORD '%s'", path, pwd);
            }
            st.executeUpdate(sql);
            return null;
        });
    }

    public int getSize() throws CommonException {
        int size = session(ps -> {
            String sql = String.format("select count(id) from position");
            ResultSet rs = ps.executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
            return 0;
        });
        return size;
    }

    public void renameGroup(String oldName, String newName) throws CommonException {
        transaction(ps -> {
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.executeUpdate();
            return null;
        }, "update position set category=? where category=?");
    }
}
