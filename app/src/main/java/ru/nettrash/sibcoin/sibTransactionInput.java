package ru.nettrash.sibcoin;

import ru.nettrash.util.Arrays;

/**
 * Created by nettrash on 30.01.2018.
 */

public final class sibTransactionInput {

    public sibTransactionInputOutpoint Outpoint;
    public int[] Script;
    public Long Sequence;

    public sibTransactionInput(String Address, String Hash, Integer Index, String sScript, Integer lockTime) {
        Outpoint = new sibTransactionInputOutpoint(Address, Hash, Index);
        Script = Arrays.toUnsignedByteArray(Arrays.hexStringToByteArray(sScript));
        Sequence = lockTime == 0 ? 4294967295L : 0;

    }

    public sibTransactionInput(String Address, String Hash, Integer Index, int[] Script, Integer lockTime) {
        Outpoint = new sibTransactionInputOutpoint(Address, Hash, Index);
        this.Script = Script.clone();
        Sequence = lockTime == 0 ? 4294967295L : 0;
    }
}
