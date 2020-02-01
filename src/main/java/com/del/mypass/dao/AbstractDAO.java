package com.del.mypass.dao;

import com.del.mypass.utils.CommonException;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.concurrent.Callable;

abstract public class AbstractDAO<T, ID> {

    private EntityManagerProvider managerProvider;
    private Class<T> tClass;

    public AbstractDAO(EntityManagerProvider managerProvider, Class<T> tClass) {
        this.managerProvider = managerProvider;
        this.tClass = tClass;
    }

    public void createAndCommit(T entity) throws CommonException {
        transaction(() -> {
            manager().persist(entity);
            manager().flush();
            return null;
        });
    }

    public void create(T entity) {
        manager().persist(entity);
    }

    public T updateAndCommit(T entity) throws CommonException {
        return transaction(() -> {
            manager().merge(entity);
            manager().flush();
            return entity;
        });
    }

    public T update(T entity) {
        return manager().merge(entity);
    }

    public void refresh(T entity) {
        manager().refresh(entity);
    }

    public void removeAndCommit(ID id) throws CommonException {
        transaction(() -> {
            remove(id);
            manager().flush();
            return null;
        });
    }

    protected <V> V transaction(Callable<V> t) throws CommonException {
        manager().getTransaction().begin();
        V result;
        try {
            result = t.call();
        } catch (Exception e) {
            manager().getTransaction().rollback();
            throw new CommonException(e);
        }
        manager().getTransaction().commit();
        return result;
    }

    public void remove(ID id) {
        manager().remove(get(id));
    }

    public T get(ID id) {
        return manager().find(tClass, id);
    }

    protected EntityManager manager() {
        return managerProvider.getEntityManager();
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
