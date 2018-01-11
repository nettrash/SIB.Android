package ru.nettrash.crypto;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Created by nettrash on 11.01.2018.
 */

public class RIPEMD160UnitTest {

    @Test
    public void hashRIPEMD160_isCorrect() throws Exception {
        byte[] bytes = "NETTRASH".getBytes();
        RIPEMD160 md = new RIPEMD160();
        md.update(bytes);
        byte[] hash = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02X", b & 0xff));
        }

        assertEquals("f221ac849e46be0afb53ef24610bba35ae1ceb3b", sb.toString().toLowerCase());
    }

}
