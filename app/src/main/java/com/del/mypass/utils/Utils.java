package com.del.mypass.utils;

import org.apache.log4j.Logger;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.ResourceBundle;

public class Utils {

    final static private Logger logger = Logger.getLogger("QR transfer Logger");

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

    public static String encodePass(String p, String _key) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digestOfPassword = Arrays.copyOf(md.digest(_key.getBytes("utf-8")), 16);
        SecretKey key = new SecretKeySpec(digestOfPassword, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(new byte[16]);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] plainTextBytes = p.getBytes("utf-8");
        return Base64.getEncoder().encodeToString(cipher.doFinal(plainTextBytes));
    }

    public static String decodePass(String p, String _key) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digestOfPassword = Arrays.copyOf(md.digest(_key.getBytes("utf-8")), 16);
        SecretKey key = new SecretKeySpec(digestOfPassword, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(new byte[16]);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] decode = Base64.getDecoder().decode(p);
        return new String(cipher.doFinal(decode), "UTF-8");
    }

    public static ResourceBundle getInfo() {
        if (info == null) {
            info = ResourceBundle.getBundle("info");
        }
        return info;
    }

}
