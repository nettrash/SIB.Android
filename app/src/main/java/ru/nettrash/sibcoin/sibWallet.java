package ru.nettrash.sibcoin;

import ru.nettrash.crypto.EllipticCurve;
import ru.nettrash.crypto.PointFP;
import ru.nettrash.crypto.SHA256;
import ru.nettrash.math.BigInteger;
import ru.nettrash.util.Arrays;

/**
 * Created by nettrash on 10.01.2018.
 */

public final class sibWallet {

    public static int KeyTypePublic = 0x3f;
    public static int KeyTypePrivate = 0x80;
    public static int KeyTypeMultisign = 0x28;

    public static String Ticker = "SIB";
    public static String URIScheme = "sibcoin:";

    private byte OperationReturnMax = 40;

    public boolean Compressed = true;

    public int[] PrivateKey;
    public int[] PublicKey;
    public String Address;
    public String WIF;

    public sibWallet() throws Exception {
    }

    public sibWallet(int[] privateKeyData) throws Exception {
        PrivateKey = privateKeyData;
        PublicKey = generatePublicKey(PrivateKey);
        Address = sibAddress.forKey(PublicKey);
        WIF = sibAddress.wifFromPrivateKey(PrivateKey);
        WIF = sibAddress.wifFromPrivateKey(PrivateKey);
    }

    private int[] generatePublicKey(int[] key) throws Exception {
        BigInteger privateKeyBigInteger = new BigInteger(Arrays.toByteArray(key));
        EllipticCurve curve = new EllipticCurve();
        PointFP curvePt = curve.G.multiply(privateKeyBigInteger);
        BigInteger x = curvePt.getX().toBigInteger();
        BigInteger y = curvePt.getY().toBigInteger();

        if (Compressed) {
            int[] a = EllipticCurve.integerToBytes(x, 32);
            if (y.isEven()) {
                int[] header = new int[] { 0x02 };

                return Arrays.append(header, a);
            } else {
                int[] header = new int[] { 0x03 };

                return Arrays.append(header, a);
            }
        } else {
            return Arrays.append(EllipticCurve.integerToBytes(x, 32), EllipticCurve.integerToBytes(y, 32));
        }
    }

    public void initialize(String secret) throws Exception {
        String sourceForPrivateKey = "SIBPrivateKey"+(3571 * secret.length()) + secret + "NETTRASHSIB";
        byte[] sourceForPrivateKeyData = sourceForPrivateKey.getBytes();
        SHA256 sha256 = new SHA256();
        sha256.update(sourceForPrivateKeyData);
        PrivateKey = Arrays.toUnsignedByteArray(sha256.digest());
        PublicKey = generatePublicKey(PrivateKey);
        Address = sibAddress.forKey(PublicKey);
        WIF = sibAddress.wifFromPrivateKey(PrivateKey);
    }

}
