package com.del.mypass.dao;

import com.del.mypass.utils.CommonException;
import com.google.common.collect.Maps;

import java.sql.Connection;
import java.util.Map;

public class DaoProvider {

    private Connection connection;

    private Map<Class<? extends AbstractDAO>, AbstractDAO> cache = Maps.newHashMap();

    public DaoProvider(Connection connection) {
        this.connection = connection;
    }

    public PositionDAO getPositionDAO() throws CommonException {
        return lookup(PositionDAO.class);
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractDAO> T lookup(Class<T> daoClass) throws CommonException {
        if (!cache.containsKey(daoClass)) {
            try {
                T instance = daoClass.getConstructor(Connection.class).newInstance(connection);
                cache.put(daoClass, instance);
            } catch (Exception e) {
                throw new CommonException(e);
            }
        }
        return (T) cache.get(daoClass);
    }
}
