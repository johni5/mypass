package com.del.mypass.dao;

import com.del.mypass.db.Position;
import com.del.mypass.utils.*;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ServiceManager implements EntityManagerProvider {

    final static private Logger logger = Logger.getLogger(ServiceManager.class);

    private static ThreadLocal<ServiceManager> instance = new ThreadLocal<>();

    private EntityManager entityManager;
    private DaoProvider provider;

    public static ServiceManager getInstance() {
        ServiceManager serviceManager = instance.get();
        if (serviceManager == null) {
            serviceManager = new ServiceManager();
            instance.set(serviceManager);
            logger.info("ServiceManager[" + serviceManager.hashCode() + "] has been created [" + serviceManager.getEntityManager().isOpen() + "]");
        }
        return serviceManager;
    }

    public static void close() {
        ServiceManager serviceManager = instance.get();
        if (serviceManager != null) {
            serviceManager.closeConnections();
            instance.remove();
            logger.info("ServiceManager[" + serviceManager.hashCode() + "] has been closed");
        }
    }

    private DaoProvider getProvider() {
        if (provider == null) {
            provider = new DaoProvider(this);
        }
        return provider;
    }

    @Override
    public EntityManager getEntityManager() {
        if (!isReady()) {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("mypass");
            entityManager = emf.createEntityManager();
        }
        return entityManager;
    }

    private void closeConnections() {
        if (isReady()) entityManager.close();
    }

    public boolean isReady() {
        return entityManager != null && entityManager.isOpen();
    }

    public void clear() {
        getEntityManager().clear();
    }

    /*POSITION*/

    public void createPosition(Position district) throws CommonException {
        getProvider().getPositionDAO().createAndCommit(district);
    }

    public void updatePosition(Position district) throws CommonException {
        getProvider().getPositionDAO().updateAndCommit(district);
    }

    public void deletePosition(Long id) throws CommonException {
        getProvider().getPositionDAO().removeAndCommit(id);
    }

    public List<Position> allPositions(String text) throws CommonException {
        return getProvider().getPositionDAO().findAll(text);
    }

    public Position findPosition(String name) throws CommonException {
        return getProvider().getPositionDAO().find(name);
    }

    /*OTHER*/

    public void backupData(String path, String pwd, SecretLocator secretLocator) throws CommonException {
        String ext = ".back";
        if (!path.endsWith(ext)) path = path + ext;
        try (Session session = getEntityManager().unwrap(Session.class)) {
            session.beginTransaction();
            if (StringUtil.isTrimmedEmpty(pwd)) {
                List<Position> all = getProvider().getPositionDAO().findAll(null);
                Map<String, String> dictionary = Maps.newHashMap();
                all.forEach(p -> {
                    try {
                        dictionary.put(p.getName(), Utils.decodePass(p.getName(), secretLocator.read()));
                        dictionary.put(p.getCode(), Utils.decodePass(p.getCode(), secretLocator.read()));
                    } catch (Exception e) {
                        dictionary.put(p.getName(), "unknown");
                    }
                });

                List list = session.createSQLQuery("SCRIPT TABLE POSITION ").getResultList();
                final StringBuilder sb = new StringBuilder();
                list.forEach(s -> sb.append(s).append(System.lineSeparator()));
                String sql = sb.toString();
                for (String key : dictionary.keySet()) {
                    sql = sql.replace(key, dictionary.get(key));
                }
                File f = new File(path);
                try {
                    if (f.exists() || f.createNewFile()) {
                        FileUtils.writeToFile(new File(path), sql.getBytes(StandardCharsets.UTF_8));
                    }
                } catch (IOException e) {
                    throw new CommonException(e);
                }
            } else {
                session.createSQLQuery("SCRIPT DROP TO :path COMPRESSION DEFLATE CIPHER AES PASSWORD :pwd " +
                        "   TABLE POSITION ").
                        setParameter("path", path).setParameter("pwd", pwd).getResultList();
            }
            session.getTransaction().commit();
        }
    }

    public void restoreData(String path, String pwd) {
        try (Session session = getEntityManager().unwrap(Session.class)) {
            session.beginTransaction();
            session.createSQLQuery("RUNSCRIPT FROM :path COMPRESSION DEFLATE CIPHER AES PASSWORD :pwd").
                    setParameter("path", path).setParameter("pwd", pwd).executeUpdate();
            session.getTransaction().commit();
        }
    }

}
