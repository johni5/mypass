package com.del.mypass.dao;

import com.del.mypass.db.Position;
import com.del.mypass.utils.CommonException;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

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

    /*OTHER*/

    public void backupData(String path, String pwd) {
        String ext = ".back";
        if (!path.endsWith(ext)) path = path + ext;
        try (Session session = getEntityManager().unwrap(Session.class)) {
            session.beginTransaction();
            session.createSQLQuery("SCRIPT DROP TO :path COMPRESSION DEFLATE CIPHER AES PASSWORD :pwd " +
                    "   TABLE POSITION ").
                    setParameter("path", path).setParameter("pwd", pwd).getResultList();
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
