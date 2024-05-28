package com.del.mypass.dao;

import com.del.mypass.db.Position;
import com.del.mypass.utils.CommonException;
import com.del.mypass.utils.FileEncrypterDecrypter;
import com.del.mypass.utils.Utils;
import org.apache.log4j.Logger;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.List;

public class ServiceManager {

    final static private Logger logger = Logger.getLogger(ServiceManager.class);

    private static final String DB_URL = "jdbc:h2:mem:mypass";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private static ThreadLocal<ServiceManager> instance = new ThreadLocal<>();

    private DaoProvider provider;
    private Connection connection;
    private FileEncrypterDecrypter file;

    private ServiceManager() {
    }

    public static ServiceManager begin(SecretKey key) throws CommonException {
        Utils.fixKeyLength();

        ServiceManager serviceManager = instance.get();
        if (serviceManager != null) {
            serviceManager.closeConnection();
            instance.remove();
        }
        serviceManager = new ServiceManager();
        if (serviceManager.init(key)) {
            instance.set(serviceManager);
            logger.info("ServiceManager[" + serviceManager.hashCode() + "] has been created");
        } else {
            throw new CommonException("Service Manager not started");
        }
        return getInstance();
    }

    public static ServiceManager getInstance() {
        return instance.get();
    }

    public static boolean isReady() {
        return instance.get() != null;
    }

    private boolean init(SecretKey key) {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            connection.setAutoCommit(false);
            boolean schemaExists = false;
            try (PreparedStatement ps = connection.prepareStatement("SHOW SCHEMAS")) {
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    String name = resultSet.getString(1);
                    if (name.equalsIgnoreCase("mypass")) {
                        schemaExists = true;
                        break;
                    }
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new SQLException("SHOW SCHEMAS ERROR", e);
            }
            if (!schemaExists) {
                String sql = getInitSQL(key);
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.executeUpdate();
                    connection.commit();
                    logger.info("RUNSCRIPT: " + sql);
                } catch (SQLException e) {
                    connection.rollback();
                    throw new SQLException("RUNSCRIPT ERROR", e);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ex) {
                    //
                }
            }
            return false;
        }
        return true;
    }

    private String getInitSQL(SecretKey key) throws CommonException {
        final StringBuilder sql = new StringBuilder();
        Utils.searchDataSet().ifPresent(files -> {
            for (File f : files) {
                try {
                    this.file = new FileEncrypterDecrypter(TRANSFORMATION, f);
                    sql.append(this.file.decrypt(key));
                    break;
                } catch (Exception e) {
                    Utils.getLogger().info(String.format("Read file '%s' error: %s", f.getName(), e.getMessage()));
                }
            }
        });
        if (sql.length() == 0) {
            try {
                this.file = new FileEncrypterDecrypter(TRANSFORMATION, Utils.newDataSet());
                InputStream resSt = ServiceManager.class.getResourceAsStream("/META-INF/init.sql");
                try (BufferedReader br = new BufferedReader(new InputStreamReader(resSt))) {
                    String line = br.readLine();
                    while (line != null) {
                        sql.append(line);
                        sql.append(System.lineSeparator());
                        line = br.readLine();
                    }
                }
            } catch (Exception e) {
                throw new CommonException(e);
            }
        }
        return sql.toString();
    }

    private DaoProvider getProvider() {
        if (provider == null) {
            provider = new DaoProvider(connection);
        }
        return provider;
    }

    public void closeConnection() throws CommonException {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            throw new CommonException(e);
        }
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

    public int getSize() throws CommonException {
        return getProvider().getPositionDAO().getSize();
    }

    public Position findPosition(String name) throws CommonException {
        return getProvider().getPositionDAO().find(name);
    }

    /*OTHER*/

    public void backupData(String path, String pwd) throws CommonException {
        String ext = ".back";
        if (!path.endsWith(ext)) path = path + ext;
        getProvider().getPositionDAO().backup(path, pwd);
    }

    public void restoreData(String path, String pwd) throws CommonException {
        getProvider().getPositionDAO().restore(path, pwd);
    }

    public void save(SecretKey secretKey) throws CommonException {
        try {
            try (PreparedStatement ps = connection.prepareStatement("SCRIPT TABLE position")) {
                ResultSet rs = ps.executeQuery();
                StringBuilder sql = new StringBuilder();
                while (rs.next()) {
                    sql.append(rs.getString(1));
                    sql.append(System.lineSeparator());
                }
                if (sql.length() > 0) {
                    if (file != null) {
                        file.encrypt(sql.toString(), secretKey);
                    }
                }
            } catch (Exception e) {
                connection.rollback();
                throw new SQLException("BACKUP ERROR", e);
            }
        } catch (SQLException e) {
            throw new CommonException(e);
        }
    }

    public void renameGroup(String oldName, String newName) throws CommonException {
        getProvider().getPositionDAO().renameGroup(oldName, newName);
    }
}
