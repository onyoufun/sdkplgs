package com.linxcool.sdkface.minigame;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.webkit.WebSettings;

public class NetworkUtils {

    public static boolean isAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == manager) return false;
        NetworkInfo info = manager.getActiveNetworkInfo();
        return (info != null && info.isConnected() && info.isAvailable());
    }

    public static int getWebCacheMode(Context context) {
        if(NetworkUtils.isAvailable(context))
            return WebSettings.LOAD_DEFAULT;
        else
            return WebSettings.LOAD_CACHE_ELSE_NETWORK;
    }
}
