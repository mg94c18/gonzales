package org.mg94c18.gonzales;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        MainActivity.possiblyUpdateForNightMode(getApplicationContext());
        super.onCreate();
    }
}
