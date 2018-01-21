package ru.nettrash.sibcoin.models;

/**
 * Created by nettrash on 18.01.2018.
 */

import android.arch.persistence.room.Room;
import android.content.Context;

import java.util.List;
import ru.nettrash.sibcoin.database.Address;
import ru.nettrash.sibcoin.database.AddressDao;
import ru.nettrash.sibcoin.database.SIBDatabase;
import ru.nettrash.sibcoin.sibWallet;

public final class rootModel extends baseModel {

    private SIBDatabase database;

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

}
