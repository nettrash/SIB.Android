package ru.nettrash.sibcoin.models;

/**
 * Created by nettrash on 18.01.2018.
 */

import android.arch.persistence.room.Room;
import android.content.Context;

import org.jetbrains.annotations.Contract;

import java.util.List;
import ru.nettrash.sibcoin.database.Address;
import ru.nettrash.sibcoin.database.AddressDao;
import ru.nettrash.sibcoin.database.SIBDatabase;
import ru.nettrash.sibcoin.sibWallet;

public final class rootModel extends baseModel {

    private SIBDatabase database;
    private Double balance;

    private void _init(Context context) {
        database = SIBDatabase.getSIBDatabase(context);
    }

    public rootModel(Context context) {

        _init(context);

    }

    public boolean firstRun() {
        return database.addressDao().count() == 0;
    }

    public void storeWallet(sibWallet wallet, short nWalletType) {
        Address address = new Address();
        address.setAddress(wallet.Address);
        address.setAddressType(nWalletType);
        address.setCompressed(wallet.Compressed);
        address.setPrivateKey(wallet.PrivateKey);
        address.setPublicKey(wallet.PublicKey);
        address.setWif(wallet.WIF);
        database.addressDao().insertAll(address);
    }

    public List<Address> getAddresses() throws Exception { return database.addressDao().getAll(); }

    public Address getAddressForInput() throws Exception {
        List<Address> incoming = database.addressDao().findOnlyIncoming();
        Address[] addresses = incoming.toArray(new Address[0]);
        return addresses[addresses.length-1];
    }

    public void setBalance(Double value) { balance = value; }

    @Contract(pure = true)
    public Double getBalance() { return balance; }

}
