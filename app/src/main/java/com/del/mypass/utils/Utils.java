package com.del.mypass.utils;

import org.apache.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class Utils {

    final static private Logger logger = Logger.getLogger("MyPass Logger");

    private static ResourceBundle info;

    public static Logger getLogger() {
        return logger;
    }

    public static boolean isTrimmedEmpty(Object val) {
        return val == null || val.toString().trim().length() == 0;
    }

    public static <T> T nvl(T t1, T t2) {
        return t1 == null ? t2 : t1;
    }

    public static ResourceBundle getInfo() {
        if (info == null) {
            info = ResourceBundle.getBundle("info");
        }
        return info;
    }

    public static SecretKey getKeyFromPassword(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 10 * 65536, 256);
        SecretKey originalKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        return originalKey;
    }

    public static void fixKeyLength() throws CommonException {
        String errorString = "Failed manually overriding key-length permissions.";
        int newMaxKeyLength;
        try {
            if ((newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES")) < 256) {
                Class c = Class.forName("javax.crypto.CryptoAllPermissionCollection");
                Constructor con = c.getDeclaredConstructor();
                con.setAccessible(true);
                Object allPermissionCollection = con.newInstance();
                Field f = c.getDeclaredField("all_allowed");
                f.setAccessible(true);
                f.setBoolean(allPermissionCollection, true);

                c = Class.forName("javax.crypto.CryptoPermissions");
                con = c.getDeclaredConstructor();
                con.setAccessible(true);
                Object allPermissions = con.newInstance();
                f = c.getDeclaredField("perms");
                f.setAccessible(true);
                ((Map) f.get(allPermissions)).put("*", allPermissionCollection);

                c = Class.forName("javax.crypto.JceSecurityManager");
                f = c.getDeclaredField("defaultPolicy");
                f.setAccessible(true);
                Field mf = Field.class.getDeclaredField("modifiers");
                mf.setAccessible(true);
                mf.setInt(f, f.getModifiers() & ~Modifier.FINAL);
                f.set(null, allPermissions);

                newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES");
            }
        } catch (Exception e) {
            throw new CommonException(errorString, e);
        }
        if (newMaxKeyLength < 256)
            throw new CommonException(errorString); // hack failed
    }

    public static Optional<File[]> searchDataSet() {
        File dir = new File("data/");
        if (dir.exists() || dir.mkdir()) {
            return Optional.ofNullable(dir.listFiles());
        }
        return Optional.empty();
    }

    public static File newDataSet() {
        File dir = new File("data/");
        if (dir.exists() || dir.mkdir()) {
            return new File(String.format("data/st%sm", Long.toString(System.currentTimeMillis(), Character.MAX_RADIX)));
        }
        return null;
    }


}
