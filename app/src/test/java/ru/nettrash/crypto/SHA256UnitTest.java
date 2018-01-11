package ru.nettrash.crypto;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Created by nettrash on 11.01.2018.
 */

public class SHA256UnitTest {

    @Test
    public void hashSHA256_isCorrect() throws Exception {
        byte[] bytes = "NETTRASH".getBytes();
        SHA256 md = new SHA256();
        md.update(bytes);
        byte[] hash = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02X", b & 0xff));
        }

        assertEquals("1dbc11294e55278e06b32729c22144fd51da3d20b770ef9270af380d33b2525d", sb.toString().toLowerCase());
    }

}
