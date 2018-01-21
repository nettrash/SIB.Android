package ru.nettrash.sibliteandroid;

import android.app.Application;

import ru.nettrash.sibcoin.models.rootModel;

/**
 * Created by nettrash on 21.01.2018.
 */

public final class SIBApplication extends Application {

    public rootModel model;

    public void initialize() {
        model = new rootModel(this);
    }
}
