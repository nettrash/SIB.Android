package ru.nettrash.crypto;

/**
 * Created by nettrash on 11.01.2018.
 */

abstract class SHA2Core extends MDHelper {

    SHA2Core()
    {
        super(false, 8);
    }

    private static final int[] K = {
            0x428A2F98, 0x71374491, 0xB5C0FBCF, 0xE9B5DBA5,
            0x3956C25B, 0x59F111F1, 0x923F82A4, 0xAB1C5ED5,
            0xD807AA98, 0x12835B01, 0x243185BE, 0x550C7DC3,
            0x72BE5D74, 0x80DEB1FE, 0x9BDC06A7, 0xC19BF174,
            0xE49B69C1, 0xEFBE4786, 0x0FC19DC6, 0x240CA1CC,
            0x2DE92C6F, 0x4A7484AA, 0x5CB0A9DC, 0x76F988DA,
            0x983E5152, 0xA831C66D, 0xB00327C8, 0xBF597FC7,
            0xC6E00BF3, 0xD5A79147, 0x06CA6351, 0x14292967,
            0x27B70A85, 0x2E1B2138, 0x4D2C6DFC, 0x53380D13,
            0x650A7354, 0x766A0ABB, 0x81C2C92E, 0x92722C85,
            0xA2BFE8A1, 0xA81A664B, 0xC24B8B70, 0xC76C51A3,
            0xD192E819, 0xD6990624, 0xF40E3585, 0x106AA070,
            0x19A4C116, 0x1E376C08, 0x2748774C, 0x34B0BCB5,
            0x391C0CB3, 0x4ED8AA4A, 0x5B9CCA4F, 0x682E6FF3,
            0x748F82EE, 0x78A5636F, 0x84C87814, 0x8CC70208,
            0x90BEFFFA, 0xA4506CEB, 0xBEF9A3F7, 0xC67178F2
    };

    private int[] currentVal, W;

    protected Digest copyState(SHA2Core dst)
    {
        System.arraycopy(currentVal, 0, dst.currentVal, 0,
                currentVal.length);
        return super.copyState(dst);
    }

    public int getBlockLength()
    {
        return 64;
    }

    protected void engineReset()
    {
        System.arraycopy(getInitVal(), 0, currentVal, 0, 8);
    }

    abstract int[] getInitVal();

    protected void doPadding(byte[] output, int outputOffset)
    {
        makeMDPadding();
        int olen = getDigestLength();
        for (int i = 0, j = 0; j < olen; i ++, j += 4)
            encodeBEInt(currentVal[i], output, outputOffset + j);
    }

    protected void doInit()
    {
        currentVal = new int[8];
        W = new int[64];
        engineReset();
    }

    private static final void encodeBEInt(int val, byte[] buf, int off)
    {
        buf[off + 0] = (byte)(val >>> 24);
        buf[off + 1] = (byte)(val >>> 16);
        buf[off + 2] = (byte)(val >>> 8);
        buf[off + 3] = (byte)val;
    }

    private static final int decodeBEInt(byte[] buf, int off)
    {
        return ((buf[off] & 0xFF) << 24)
                | ((buf[off + 1] & 0xFF) << 16)
                | ((buf[off + 2] & 0xFF) << 8)
                | (buf[off + 3] & 0xFF);
    }

    static private int circularLeft(int x, int n)
    {
        return (x << n) | (x >>> (32 - n));
    }

    protected void processBlock(byte[] data)
    {
        int A = currentVal[0];
        int B = currentVal[1];
        int C = currentVal[2];
        int D = currentVal[3];
        int E = currentVal[4];
        int F = currentVal[5];
        int G = currentVal[6];
        int H = currentVal[7];

        for (int i = 0; i < 16; i ++)
            W[i] = decodeBEInt(data, 4 * i);
        for (int i = 16; i < 64; i ++) {
            W[i] = (circularLeft(W[i - 2], 15)
                    ^ circularLeft(W[i - 2], 13)
                    ^ (W[i - 2] >>> 10))
                    + W[i - 7]
                    + (circularLeft(W[i - 15], 25)
                    ^ circularLeft(W[i - 15], 14)
                    ^ (W[i - 15] >>> 3))
                    + W[i - 16];
        }
        for (int i = 0; i < 64; i ++) {
            int T1 = H + (circularLeft(E, 26) ^ circularLeft(E, 21)
                    ^ circularLeft(E, 7)) + ((F & E) ^ (G & ~E))
                    + K[i] + W[i];
            int T2 = (circularLeft(A, 30) ^ circularLeft(A, 19)
                    ^ circularLeft(A, 10))
                    + ((A & B) ^ (A & C) ^ (B & C));
            H = G; G = F; F = E; E = D + T1;
            D = C; C = B; B = A; A = T1 + T2;
        }
        currentVal[0] += A;
        currentVal[1] += B;
        currentVal[2] += C;
        currentVal[3] += D;
        currentVal[4] += E;
        currentVal[5] += F;
        currentVal[6] += G;
        currentVal[7] += H;
    }

    public String toString()
    {
        return "SHA-" + (getDigestLength() << 3);
    }
}