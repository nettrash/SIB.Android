package ru.nettrash.math;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Created by nettrash on 11.01.2018.
 */

public class BigIntegerUnitTest {

    @Test
    public void BigIntegerZero_isCorrect() throws Exception {
        BigInteger zero = new BigInteger(0);
        Integer[] bytes = zero.toByteArray();
        assertEquals(0, zero.s);
        assertEquals(0, zero.t);
        assertEquals(0, bytes.length);
    }

}
