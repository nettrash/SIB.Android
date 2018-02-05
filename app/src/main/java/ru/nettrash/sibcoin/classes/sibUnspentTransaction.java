package ru.nettrash.sibcoin.classes;

import org.json.JSONObject;

/**
 * Created by nettrash on 31.01.2018.
 */

public final class sibUnspentTransaction {

    public String Id;
    public Double Amount;
    public String Address;
    public int N;
    public String Script;

    public sibUnspentTransaction(String Id, Double Amount, String Address, int N, String Script) {

        this.Id = Id;
        this.Amount = Amount;
        this.Address = Address;
        this.N = N;
        this.Script = Script;

    }

    public sibUnspentTransaction(JSONObject object) throws Exception {

        this.Id = object.getString("Id");
        this.Amount = object.getDouble("Amount") / Math.pow(10, 8);
        this.Address = object.getString("Address");
        this.N = object.getInt("N");
        this.Script = object.getString("Script");

    }
}
