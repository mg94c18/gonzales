package org.mg94c18.gonzales;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        MainActivity.possiblyUpdateForNightMode(getApplicationContext());
        super.onCreate();
    }
}
