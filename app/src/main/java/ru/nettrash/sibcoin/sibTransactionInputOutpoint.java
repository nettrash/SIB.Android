package ru.nettrash.sibcoin;

/**
 * Created by nettrash on 30.01.2018.
 */

public final class sibTransactionInputOutpoint {

    public String Address;
    public String Hash;
    public Integer Index;

    public sibTransactionInputOutpoint(String Address, String Hash, Integer Index) {
        this.Address = Address;
        this.Hash = Hash;
        this.Index = Index;
    }
}
