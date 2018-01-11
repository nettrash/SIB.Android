package ru.nettrash.crypto;

/**
 * Created by nettrash on 11.01.2018.
 */

public class MD5 extends MDHelper {

    public MD5()
    {
        super(true, 8);
    }

    private int[] currentVal, X;

    public Digest copy()
    {
        MD5 d = new MD5();
        System.arraycopy(currentVal, 0, d.currentVal, 0,
                currentVal.length);
        return copyState(d);
    }

    public int getDigestLength()
    {
        return 16;
    }

    public int getBlockLength()
    {
        return 64;
    }

    protected void engineReset()
    {
        currentVal[0] = (int)0x67452301;
        currentVal[1] = (int)0xEFCDAB89;
        currentVal[2] = (int)0x98BADCFE;
        currentVal[3] = (int)0x10325476;
    }

    protected void doPadding(byte[] output, int outputOffset)
    {
        makeMDPadding();
        for (int i = 0; i < 4; i ++)
            encodeLEInt(currentVal[i],
                    output, outputOffset + 4 * i);
    }

    protected void doInit()
    {
        currentVal = new int[4];
        X = new int[16];
        engineReset();
    }

    private static final int circularLeft(int x, int n)
    {
        return (x << n) | (x >>> (32 - n));
    }

    private static final void encodeLEInt(int val, byte[] buf, int off)
    {
        buf[off + 0] = (byte)val;
        buf[off + 1] = (byte)(val >>> 8);
        buf[off + 2] = (byte)(val >>> 16);
        buf[off + 3] = (byte)(val >>> 24);
    }

    private static final int decodeLEInt(byte[] buf, int off)
    {
        return (buf[off] & 0xFF)
                | ((buf[off + 1] & 0xFF) << 8)
                | ((buf[off + 2] & 0xFF) << 16)
                | ((buf[off + 3] & 0xFF) << 24);
    }

    private static final int F(int X, int Y, int Z)
    {
        return (Y & X) | (Z & ~X);
    }

    private static final int G(int X, int Y, int Z)
    {
        return (X & Z) | (Y & ~Z);
    }

    private static final int H(int X, int Y, int Z)
    {
        return X ^ Y ^ Z;
    }

    private static final int I(int X, int Y, int Z)
    {
        return Y ^ (X | ~Z);
    }

    protected void processBlock(byte[] data)
    {
        int A = currentVal[0], B = currentVal[1];
        int C = currentVal[2], D = currentVal[3];

        for (int i = 0; i < 16; i ++)
            X[i] = decodeLEInt(data, 4 * i);

        A = B + circularLeft(A + F(B, C, D) + X[ 0] + 0xD76AA478, 7);
        D = A + circularLeft(D + F(A, B, C) + X[ 1] + 0xE8C7B756, 12);
        C = D + circularLeft(C + F(D, A, B) + X[ 2] + 0x242070DB, 17);
        B = C + circularLeft(B + F(C, D, A) + X[ 3] + 0xC1BDCEEE, 22);
        A = B + circularLeft(A + F(B, C, D) + X[ 4] + 0xF57C0FAF, 7);
        D = A + circularLeft(D + F(A, B, C) + X[ 5] + 0x4787C62A, 12);
        C = D + circularLeft(C + F(D, A, B) + X[ 6] + 0xA8304613, 17);
        B = C + circularLeft(B + F(C, D, A) + X[ 7] + 0xFD469501, 22);
        A = B + circularLeft(A + F(B, C, D) + X[ 8] + 0x698098D8, 7);
        D = A + circularLeft(D + F(A, B, C) + X[ 9] + 0x8B44F7AF, 12);
        C = D + circularLeft(C + F(D, A, B) + X[10] + 0xFFFF5BB1, 17);
        B = C + circularLeft(B + F(C, D, A) + X[11] + 0x895CD7BE, 22);
        A = B + circularLeft(A + F(B, C, D) + X[12] + 0x6B901122, 7);
        D = A + circularLeft(D + F(A, B, C) + X[13] + 0xFD987193, 12);
        C = D + circularLeft(C + F(D, A, B) + X[14] + 0xA679438E, 17);
        B = C + circularLeft(B + F(C, D, A) + X[15] + 0x49B40821, 22);

        A = B + circularLeft(A + G(B, C, D) + X[ 1] + 0xF61E2562, 5);
        D = A + circularLeft(D + G(A, B, C) + X[ 6] + 0xC040B340, 9);
        C = D + circularLeft(C + G(D, A, B) + X[11] + 0x265E5A51, 14);
        B = C + circularLeft(B + G(C, D, A) + X[ 0] + 0xE9B6C7AA, 20);
        A = B + circularLeft(A + G(B, C, D) + X[ 5] + 0xD62F105D, 5);
        D = A + circularLeft(D + G(A, B, C) + X[10] + 0x02441453, 9);
        C = D + circularLeft(C + G(D, A, B) + X[15] + 0xD8A1E681, 14);
        B = C + circularLeft(B + G(C, D, A) + X[ 4] + 0xE7D3FBC8, 20);
        A = B + circularLeft(A + G(B, C, D) + X[ 9] + 0x21E1CDE6, 5);
        D = A + circularLeft(D + G(A, B, C) + X[14] + 0xC33707D6, 9);
        C = D + circularLeft(C + G(D, A, B) + X[ 3] + 0xF4D50D87, 14);
        B = C + circularLeft(B + G(C, D, A) + X[ 8] + 0x455A14ED, 20);
        A = B + circularLeft(A + G(B, C, D) + X[13] + 0xA9E3E905, 5);
        D = A + circularLeft(D + G(A, B, C) + X[ 2] + 0xFCEFA3F8, 9);
        C = D + circularLeft(C + G(D, A, B) + X[ 7] + 0x676F02D9, 14);
        B = C + circularLeft(B + G(C, D, A) + X[12] + 0x8D2A4C8A, 20);

        A = B + circularLeft(A + H(B, C, D) + X[ 5] + 0xFFFA3942, 4);
        D = A + circularLeft(D + H(A, B, C) + X[ 8] + 0x8771F681, 11);
        C = D + circularLeft(C + H(D, A, B) + X[11] + 0x6D9D6122, 16);
        B = C + circularLeft(B + H(C, D, A) + X[14] + 0xFDE5380C, 23);
        A = B + circularLeft(A + H(B, C, D) + X[ 1] + 0xA4BEEA44, 4);
        D = A + circularLeft(D + H(A, B, C) + X[ 4] + 0x4BDECFA9, 11);
        C = D + circularLeft(C + H(D, A, B) + X[ 7] + 0xF6BB4B60, 16);
        B = C + circularLeft(B + H(C, D, A) + X[10] + 0xBEBFBC70, 23);
        A = B + circularLeft(A + H(B, C, D) + X[13] + 0x289B7EC6, 4);
        D = A + circularLeft(D + H(A, B, C) + X[ 0] + 0xEAA127FA, 11);
        C = D + circularLeft(C + H(D, A, B) + X[ 3] + 0xD4EF3085, 16);
        B = C + circularLeft(B + H(C, D, A) + X[ 6] + 0x04881D05, 23);
        A = B + circularLeft(A + H(B, C, D) + X[ 9] + 0xD9D4D039, 4);
        D = A + circularLeft(D + H(A, B, C) + X[12] + 0xE6DB99E5, 11);
        C = D + circularLeft(C + H(D, A, B) + X[15] + 0x1FA27CF8, 16);
        B = C + circularLeft(B + H(C, D, A) + X[ 2] + 0xC4AC5665, 23);

        A = B + circularLeft(A + I(B, C, D) + X[ 0] + 0xF4292244, 6);
        D = A + circularLeft(D + I(A, B, C) + X[ 7] + 0x432AFF97, 10);
        C = D + circularLeft(C + I(D, A, B) + X[14] + 0xAB9423A7, 15);
        B = C + circularLeft(B + I(C, D, A) + X[ 5] + 0xFC93A039, 21);
        A = B + circularLeft(A + I(B, C, D) + X[12] + 0x655B59C3, 6);
        D = A + circularLeft(D + I(A, B, C) + X[ 3] + 0x8F0CCC92, 10);
        C = D + circularLeft(C + I(D, A, B) + X[10] + 0xFFEFF47D, 15);
        B = C + circularLeft(B + I(C, D, A) + X[ 1] + 0x85845DD1, 21);
        A = B + circularLeft(A + I(B, C, D) + X[ 8] + 0x6FA87E4F, 6);
        D = A + circularLeft(D + I(A, B, C) + X[15] + 0xFE2CE6E0, 10);
        C = D + circularLeft(C + I(D, A, B) + X[ 6] + 0xA3014314, 15);
        B = C + circularLeft(B + I(C, D, A) + X[13] + 0x4E0811A1, 21);
        A = B + circularLeft(A + I(B, C, D) + X[ 4] + 0xF7537E82, 6);
        D = A + circularLeft(D + I(A, B, C) + X[11] + 0xBD3AF235, 10);
        C = D + circularLeft(C + I(D, A, B) + X[ 2] + 0x2AD7D2BB, 15);
        B = C + circularLeft(B + I(C, D, A) + X[ 9] + 0xEB86D391, 21);

        currentVal[0] += A;
        currentVal[1] += B;
        currentVal[2] += C;
        currentVal[3] += D;
    }

    public String toString()
    {
        return "MD5";
    }
}