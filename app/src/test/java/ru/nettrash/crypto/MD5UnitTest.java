package ru.nettrash.crypto;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Created by nettrash on 11.01.2018.
 */

public class MD5UnitTest {

    @Test
    public void hashMD5_isCorrect() throws Exception {
        byte[] bytes = "NETTRASH".getBytes();
        MD5 md = new MD5();
        md.update(bytes);
        byte[] hash = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02X", b & 0xff));
        }

        assertEquals("90bc0c1e6158acdddf5eb5f357ae565a", sb.toString().toLowerCase());
    }

}
