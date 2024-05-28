package com.del.mypass.utils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by DodolinEL
 * date: 21.05.2024
 */
public class FileEncrypterDecrypter {

    private Cipher cipher;
    private File file;

    public FileEncrypterDecrypter(String transformation, File file) throws NoSuchPaddingException, NoSuchAlgorithmException {
        this.cipher = Cipher.getInstance(transformation);
        this.file = file;
    }

    public void encrypt(String content, SecretKey secretKey) throws IOException, InvalidKeyException {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] iv = cipher.getIV();

        try (FileOutputStream fileOut = new FileOutputStream(file);
             CipherOutputStream cipherOut = new CipherOutputStream(fileOut, cipher)) {
            fileOut.write(iv);
            cipherOut.write(content.getBytes());
        }
    }

    public String decrypt(SecretKey secretKey) throws IOException, InvalidAlgorithmParameterException, InvalidKeyException {
        String content = "";
        if (file.length() > 0) {
            try (FileInputStream fileIn = new FileInputStream(file)) {
                byte[] fileIv = new byte[16];
                fileIn.read(fileIv);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(fileIv));

                try (
                        CipherInputStream cipherIn = new CipherInputStream(fileIn, cipher);
                        InputStreamReader inputReader = new InputStreamReader(cipherIn);
                        BufferedReader reader = new BufferedReader(inputReader)
                ) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        sb.append(System.lineSeparator());
                    }
                    content = sb.toString();
                }
            }
        }
        return content;
    }

}
