package com.example.skillboxchat;

import android.util.Base64;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
    private static SecretKeySpec keySpec;
    private static String pass = "kryjovnik";

    static {
        try {
            MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = pass.getBytes();
            shaDigest.update(bytes, 0, bytes.length);
            byte[] hash = shaDigest.digest();
            keySpec = new SecretKeySpec(hash, "AES");
        } catch (Exception e) {
            e.printStackTrace();
        }
    };


    public static String encrypt(String unencryptedText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encypted  = cipher.doFinal(unencryptedText.getBytes());
        return Base64.encodeToString(encypted, Base64.DEFAULT);
    }

    public static String decrypt(String decryptedText) throws Exception {
        byte[] ciphered = Base64.decode(decryptedText, Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] rawText = cipher.doFinal(ciphered);
        return new String(rawText, "UTF-8");
    }
}
