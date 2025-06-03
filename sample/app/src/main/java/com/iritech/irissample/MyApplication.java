package com.iritech.irissample;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.iritech.iris.LanguageHelper;

public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        Context context = LanguageHelper.onAttach(base, "en"); // Giả sử "en" là mặc định
        super.attachBaseContext(context);
        Log.d("LanguageDebug", "MyApplication attachBaseContext - Language applied: " + LanguageHelper.getLanguage(context));
    }
}