package ru.nettrash.crypto;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import android.util.Base64;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import ru.nettrash.util.Arrays;

/**
 * Created by nettrash on 06.02.2018.
 */

public class AESTest {

    @Test
    public void AESPKCS7PaddingEncryption_isCorrect() throws Exception {
        String password = "aes-encryption-password";
        String iv = "20219510518024419136177230";
        String textToEncrypt = "text to encryption";

        MD5 md5 = new MD5();
        md5.update(iv.getBytes());
        byte[] ivb = md5.digest();

        md5 = new MD5();
        md5.update(password.getBytes());
        byte[] passwordb = md5.digest();

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        SecretKeySpec secretKeySpec = new SecretKeySpec(passwordb, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivb);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

        byte[] data = textToEncrypt.getBytes();
        byte[] encrypted = cipher.doFinal(data);

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] decrypted = cipher.doFinal(encrypted);

        String sDecrypted = new String(decrypted);

        assertEquals(data.length, decrypted.length);
        assertEquals(true, java.util.Arrays.hashCode(data) == java.util.Arrays.hashCode(decrypted));
        assertEquals(true, sDecrypted.equals(textToEncrypt));
    }
}
