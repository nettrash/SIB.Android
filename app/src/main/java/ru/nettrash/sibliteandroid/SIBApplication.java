package ru.nettrash.sibliteandroid;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

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
        editor.putString("PIN", sPIN);
        editor.commit();
    }

    public boolean checkPIN(String sPIN) {
        SharedPreferences preferences = this.getSharedPreferences("SIBPreferences", Context.MODE_PRIVATE);
        return !sPIN.equals("") && sPIN.equals(preferences.getString("PIN", ""));
    }
}
