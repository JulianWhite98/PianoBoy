package com.mwangblog.midiprocessing;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class AssetsApp extends Application {

    private static final String TAG = "AssetsApp";

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        Log.i(TAG, "onCreate()");
    }

    public static Context getmContext() {
        return mContext;
    }
}
