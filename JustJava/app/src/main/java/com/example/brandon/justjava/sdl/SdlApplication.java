package com.example.brandon.justjava.sdl;

import android.app.Application;

public class SdlApplication extends Application{

    private static final String TAG = SdlApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        LockScreenActivity.registerActivityLifecycle(this);
    }

}
