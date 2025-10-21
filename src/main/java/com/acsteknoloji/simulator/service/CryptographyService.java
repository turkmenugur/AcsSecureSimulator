package com.acsteknoloji.simulator.service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class CryptographyService {
    private static final String ALGORITHM = "AES";
    private static final String HASH_ALGORITHM = "SHA-256";

    // Her mesajlaşma için benzersiz bir anahtar oluşturabiliriz,
    // ancak simülasyon kolaylığı için şimdilik sabit bir anahtar kullanacağız.
    // Gerçek bir sistemde bu anahtar Diffie-Hellman gibi bir protokolle oluşturulurdu.
    private static final String SECRET_KEY = "AcsTeknoloji!Key";

    public String encrypt(String plainText) throws Exception{
        SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decrypt(String encryptedText) throws Exception {
        SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public String hash(String text) throws Exception{
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        return  Base64.getEncoder().encodeToString(hashBytes);
    }
}
