package com.del.mypass.dao;

import com.del.mypass.utils.CommonException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

abstract public class AbstractDAO<T> {

    private Connection c;
    private Class<T> tClass;

    public AbstractDAO(Connection c, Class<T> tClass) {
        this.c = c;
        this.tClass = tClass;
    }

    protected <V> V session(Transaction<Statement, V> t) throws CommonException {
        try (Statement st = c.createStatement()) {
            V res = t.begin(st);
            return res;
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    protected <V> V transaction(Transaction<Statement, V> t) throws CommonException {
        try (Statement st = c.createStatement()) {
            V res = t.begin(st);
            c.commit();
            return res;
        } catch (Exception e) {
            try {
                c.rollback();
            } catch (SQLException ex) {
                //
            }
            throw new CommonException(e);
        }
    }

    protected <V> V transaction(Transaction<PreparedStatement, V> t, String sql) throws CommonException {
        try (PreparedStatement st = c.prepareStatement(sql)) {
            V res = t.begin(st);
            c.commit();
            return res;
        } catch (Exception e) {
            try {
                c.rollback();
            } catch (SQLException ex) {
                //
            }
            throw new CommonException(e);
        }
    }

    Statement statement() throws SQLException {
        return c.createStatement();
    }

    PreparedStatement prepareStatement(String sql) throws SQLException {
        return c.prepareStatement(sql);
    }

    public static Long getLong(Object obj, Long def) {
        return obj instanceof Number ? ((Number) obj).longValue() : def;
    }

    public static Integer getInt(Object obj, Integer dif) {
        return obj != null ? ((Number) obj).intValue() : dif;
    }

    public static Double getDouble(Object obj, Double def) {
        return obj != null ? ((Number) obj).doubleValue() : def;
    }

    public static Date getDate(Object obj, Date def) {
        return obj != null ? (Date) obj : def;
    }

    public static String getString(Object obj, String def) {
        return obj != null ? obj.toString() : def;
    }


}
