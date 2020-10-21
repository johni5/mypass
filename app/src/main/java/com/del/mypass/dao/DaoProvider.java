package com.del.mypass.dao;

import com.del.mypass.utils.CommonException;
import com.google.common.collect.Maps;

import java.util.Map;

public class DaoProvider {

    private EntityManagerProvider managerProvider;

    private Map<Class<? extends AbstractDAO>, AbstractDAO> cache = Maps.newHashMap();

    public DaoProvider(EntityManagerProvider managerProvider) {
        this.managerProvider = managerProvider;
    }

    public PositionDAO getPositionDAO() throws CommonException {
        return lookup(PositionDAO.class);
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractDAO> T lookup(Class<T> daoClass) throws CommonException {
        if (!cache.containsKey(daoClass)) {
            try {
                T instance = daoClass.getConstructor(EntityManagerProvider.class).newInstance(managerProvider);
                cache.put(daoClass, instance);
            } catch (Exception e) {
                throw new CommonException(e);
            }
        }
        return (T) cache.get(daoClass);
    }
}
