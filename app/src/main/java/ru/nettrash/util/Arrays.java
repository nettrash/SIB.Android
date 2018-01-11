package ru.nettrash.util;

/**
 * Created by nettrash on 11.01.2018.
 */

public final class Arrays
{

    public static byte[] toByteArray(int[] source)
    {
        byte[] retVal = new byte[source.length];
        for (int i=0; i<source.length; i++)
        {
            retVal[i] = source[i] < 128 ? (byte)source[i] : (byte)(source[i] - 256);
        }
        return retVal;
    }

    public static int[] toUnsignedByteArray(byte[] source)
    {
        int[] retVal = new int[source.length];
        for (int i=0; i<source.length; i++)
        {
            retVal[i] = source[i] & 0xff;
        }
        return retVal;
    }

    public static void append(int[] source, int value)
    {
        source = java.util.Arrays.copyOf(source, source.length+1);
        source[source.length-1] = value;
    }

    public static void append(int[] source, int[] value)
    {
        append(source, value, 0, value.length);
    }

    public static void append(int[] source, int[] value, int offset, int size)
    {
        source = java.util.Arrays.copyOf(source, source.length+size);
        System.arraycopy(value, offset, source, source.length-size-1, size);
    }

    public static byte[] subarray(byte[] source, int offset, int size)
    {
        byte[] retVal = new byte[size];
        System.arraycopy(source, offset, retVal, 0, size);
        return retVal;
    }

    public static int[] subarray(int[] source, int offset, int size)
    {
        int[] retVal = new int[size];
        System.arraycopy(source, offset, retVal, 0, size);
        return retVal;
    }

}
