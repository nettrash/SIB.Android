package ru.nettrash.sibliteandroid;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by nettrash on 20.01.2018.
 */

class BaseActivity extends Activity {

    protected SIBApplication sibApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sibApplication = (SIBApplication)getApplicationContext();
    }

}
