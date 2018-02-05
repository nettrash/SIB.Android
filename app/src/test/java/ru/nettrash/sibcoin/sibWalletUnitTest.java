package ru.nettrash.sibcoin;

/**
 * Created by nettrash on 16.01.2018.
 */

import android.util.Log;

import org.junit.Test;

import ru.nettrash.crypto.EllipticCurve;
import ru.nettrash.math.BigInteger;
import ru.nettrash.sibcoin.classes.sibUnspentTransaction;
import ru.nettrash.sibcoin.database.Address;
import ru.nettrash.util.Arrays;

import static org.junit.Assert.assertEquals;

public class sibWalletUnitTest {

    @Test
    public void sibWalletInitialize_isCorrect() throws Exception {

        sibWallet wallet = new sibWallet();
        wallet.initialize("test");
        assertEquals(true, wallet.Address.equals("SRcxum6SzkCLkgC3W8vzSeiVtdiPrbH9zB"));
        assertEquals(true, wallet.WIF.equals("Kz45ruVNX4YRYobW6nqjCjFnjDw67rRV2ZJoq3akysBX9qQNWHNC"));
    }

    @Test
    public void sibTransactionSign_isCorrect() throws Exception {
        sibTransaction tx = new sibTransaction();
        tx.addInput(new sibUnspentTransaction("6f0bc852576ab14ba3034de7a31395391b3860f1269d938248e25059f9e0f151", 1.0, "SQDtZDsmQqinZw7tRr9JVKX7iF8RndCLNW", 0, "76a91420214eb53d755ce189873609c5d17404db01539488ac"));

        tx.addOutput("SQBYgoR2r67ihxZToKfeLrxtCjGPWTrZRh", 0.1);

        Address addr = new Address();
        addr.setAddress("SQDtZDsmQqinZw7tRr9JVKX7iF8RndCLNW");
        addr.setAddressType((short)0);
        addr.setCompressed(true);
        addr.setWif("KxeSJPbsGzdMzn9BEsWZJzLLGduZZWCJp2BbL9GbvY2UvexCCnnp");
        addr.setPrivateKey(new int[] { 42, 142, 181, 162, 240, 227, 230, 146, 76, 87, 211, 205, 139, 192, 200, 164, 24, 19, 119, 69, 144, 212, 247, 82, 3, 234, 176, 215, 192, 106, 36, 56 });
        addr.setPublicKey(new int[] { 2, 5, 14, 229, 161, 25, 28, 166, 246, 74, 254, 167, 71, 35, 198, 29, 110, 236, 119, 50, 25, 124, 97, 120, 71, 241, 196, 142, 48, 59, 89, 255, 61 });

        int[] s = tx.sign(new Address[] { addr });

        int[] script = sibAddress.spendToScript("SQBYgoR2r67ihxZToKfeLrxtCjGPWTrZRh");

        assertEquals(s.length, 158);
    }

}
