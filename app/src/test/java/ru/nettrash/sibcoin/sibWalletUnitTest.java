package ru.nettrash.sibcoin;

/**
 * Created by nettrash on 16.01.2018.
 */

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class sibWalletUnitTest {

    @Test
    public void sibWalletInitialize_isCorrect() throws Exception {

        sibWallet wallet = new sibWallet();
        wallet.initialize("test");
        assertEquals(true, wallet.Address.equals("SRcxum6SzkCLkgC3W8vzSeiVtdiPrbH9zB"));
        assertEquals(true, wallet.WIF.equals("Kz45ruVNX4YRYobW6nqjCjFnjDw67rRV2ZJoq3akysBX9qQNWHNC"));
    }

}
