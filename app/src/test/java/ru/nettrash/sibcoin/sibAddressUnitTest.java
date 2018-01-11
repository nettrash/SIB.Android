package ru.nettrash.sibcoin;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Created by nettrash on 11.01.2018.
 */

public class sibAddressUnitTest {

    @Test
    public void sibAddressVerify_isCorrect() throws Exception {

        assertEquals(true, sibAddress.verify("SNmdtyvBJ88kg1r7vPSYMzt7yV4tkvdeFp"));
        assertEquals(false, sibAddress.verify("16TFkJYqK73JPbdwMeteGbNbMddVWJP5rG"));
    }

}
