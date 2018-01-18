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
        int[] bytes = zero.toByteArray();
        assertEquals(0, zero.s);
        assertEquals(0, zero.t);
        assertEquals(0, bytes.length);
        assertEquals(0, zero.intValue());
    }

    @Test
    public void BigIntegerOne_isCorrect() throws Exception {
        BigInteger one = new BigInteger(1);
        int[] bytes = one.toByteArray();
        assertEquals(0, one.s);
        assertEquals(1, one.t);
        assertEquals(1, bytes.length);
        assertEquals(1, one.intValue());
    }

    @Test
    public void BigIntegerAdd_isCorrect() throws Exception {
        BigInteger one = new BigInteger(1);
        BigInteger two = new BigInteger(2);
        BigInteger sum = one.add(two);
        int[] bytes = sum.toByteArray();
        assertEquals(0, sum.s);
        assertEquals(1, sum.t);
        assertEquals(1, bytes.length);
        assertEquals(3, sum.intValue());
    }

    @Test
    public void BigIntegerSubtract_isCorrect() throws Exception {
        BigInteger one = new BigInteger(1);
        BigInteger two = new BigInteger(2);
        BigInteger sub = two.subtract(one);
        int[] bytes = sub.toByteArray();
        assertEquals(0, sub.s);
        assertEquals(1, sub.t);
        assertEquals(1, bytes.length);
        assertEquals(1, sub.intValue());
    }

}
