package ru.nettrash.sibcoin;

import ru.nettrash.math.BigInteger;

/**
 * Created by nettrash on 30.01.2018.
 */

public final class sibTransactionOutput {

    public BigInteger Amount;
    public int[] ScriptedAddress;
    public long Satoshi;

    public sibTransactionOutput(int[] ScriptedAddress, BigInteger Amount, long Satoshi) {
        this.ScriptedAddress = ScriptedAddress.clone();
        this.Amount = Amount.clone();
        this.Satoshi = Satoshi;
    }
}
