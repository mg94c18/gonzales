package org.mg94c18.gonzales;

import static org.mg94c18.gonzales.Logger.LOG_V;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class PlaybackService extends Service {
    public static final String ACTION_PLAY = "play";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) { LOG_V("onCreate()"); }
    }

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) { LOG_V("onDestroy()"); }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (BuildConfig.DEBUG) { LOG_V("onStartCommand(" + flags + "," + startId + ")"); }
        return START_NOT_STICKY;
    }
}
