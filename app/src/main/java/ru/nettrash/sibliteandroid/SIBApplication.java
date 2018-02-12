package ru.nettrash.sibliteandroid;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Base64;

import org.jetbrains.annotations.Contract;

import ru.nettrash.sibcoin.models.rootModel;

/**
 * Created by nettrash on 21.01.2018.
 */

public final class SIBApplication extends Application {

    public rootModel model;

    public void initialize() {
        model = new rootModel(this);
    }

    public void setPIN(String sPIN) {
        SharedPreferences preferences = this.getSharedPreferences("SIBPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("PIN", Base64.encodeToString((sPIN + "NETTRASH_SIB_WALLET").getBytes(), Base64.NO_WRAP));
        editor.commit();
    }

    public boolean checkPIN(String sPIN) {
        SharedPreferences preferences = this.getSharedPreferences("SIBPreferences", Context.MODE_PRIVATE);
        String storedPIN = preferences.getString("PIN", "");
        String enteredPIN = Base64.encodeToString((sPIN + "NETTRASH_SIB_WALLET").getBytes(), Base64.NO_WRAP);
        return !sPIN.equals("") && enteredPIN.equals(storedPIN);
    }

    @NonNull
    @Contract(pure = true)
    public String getCurrencySymbol() {
        return "₽";
    }

    @NonNull
    @Contract(pure = true)
    public String getCurrency() {
        return "RUB";
    }

}
