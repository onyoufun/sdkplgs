package com.linxcool.sdkface.appsflyer;

import android.app.Application;

import com.appsflyer.AppsFlyerLib;

public class AFApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppsFlyerLib.getInstance().init("", null, this);
        AppsFlyerLib.getInstance().start(this);
    }
}
