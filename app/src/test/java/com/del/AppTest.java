package com.del;

import com.del.mypass.utils.PasswordGenerator;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
//    @Test
//    public void shouldAnswerWithTrue() {
//
//        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ministry");
//        EntityManager em = emf.createEntityManager();
//
//        EntityTransaction tx = em.getTransaction();
//        tx.begin();
//
//        try {
//            City c = new City();
//            c.setName("Кемерово");
//            em.persist(c);
//            tx.commit();
//        } catch (Exception e) {
//            Utils.getLogger().error(e.getMessage(), e);
//            tx.rollback();
//        } finally {
//            em.close();
//            emf.close();
//        }
//    }

    private static final String AES_KEY = "JKHJhkHFufe234^$SW^&d&DV&aw6rU%a";

    public static void main(String[] args) {
        try {


            PasswordGenerator passwordGenerator = new PasswordGenerator.PasswordGeneratorBuilder()
                    .useDigits(true)
                    .useLower(true)
                    .useUpper(true)
                    .usePunctuation(true)
                    .build();
            String password = passwordGenerator.generate();
            System.out.println(password);

            if (1 == 1) return;

            String orignalText = "Hello world";
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digestOfPassword = md.digest(AES_KEY.getBytes("utf-8"));
            SecretKey key = new SecretKeySpec(digestOfPassword, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(new byte[16]);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] plainTextBytes = orignalText.getBytes("utf-8");

            String encodedText = Base64.getEncoder().encodeToString(cipher.doFinal(plainTextBytes));
            System.out.println(encodedText);

            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decode = Base64.getDecoder().decode(encodedText);
            System.out.println(new String(cipher.doFinal(decode), "UTF-8"));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
