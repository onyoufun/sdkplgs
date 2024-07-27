package com.linxcool.sdkface.firebase;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.linxcool.sdkface.feature.YmnPluginWrapper;
import com.linxcool.sdkface.feature.protocol.YFunction;
import com.linxcool.sdkface.feature.protocol.YPlugin;
import com.linxcool.sdkface.util.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

@YPlugin(strategy = YPlugin.Policy.FORCE, entrance = YPlugin.Entrance.ACTIVITY)
public class FirebaseInterface extends YmnPluginWrapper {

    @Override
    public String getPluginId() {
        return "301";
    }

    @Override
    public String getPluginName() {
        return "firebase";
    }

    @Override
    public int getPluginVersion() {
        return 1;
    }

    @Override
    public String getSdkVersion() {
        return "29.2.1";
    }


    private FirebaseAnalytics analytics;
    private Gson gson = new Gson();


    @Override
    public void onInit(Context context) {
        super.onInit(context);
        analytics = FirebaseAnalytics.getInstance(context);
    }

    @YFunction(name = "ymnfirebase_logevent")
    public void logEvent(String eventName, String eventParams) {
        try {
            Map<String, String> map = getMapFrom(eventParams);
            if(map == null) return;

            Bundle params = new Bundle();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                params.putString(entry.getKey(), entry.getValue());
            }
            analytics.logEvent(eventName, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @YFunction(name = "ymnfirebase_logevent2")
    public void logEvent(final LinkedHashMap<String, String> events) {
        String eventName = events.get("eventName");
        String eventParams = events.get("eventParams");
        try {
            Map<String, String> map = getMapFrom(eventParams);
            if(map == null) return;

            Bundle params = new Bundle();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                params.putString(entry.getKey(), entry.getValue());
            }
            analytics.logEvent(eventName, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <T> Map<String,T> getMapFrom(String data) {
        try {
            return gson.fromJson(data,new TypeToken<Map<String,T>>(){}.getType());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @YFunction(name = "ymnfirebase_set_user_property")
    public void setUserProperty(String name, String value) {
        analytics.setUserProperty(name, value);
    }

    @YFunction(name = "ymnfirebase_set_userid")
    public void setUserId(String userid) {
        analytics.setUserId(userid);
    }

    @YFunction(name = "ymnfirebase_set_analytics_enable")
    public void setAnalyticsCollectionEnabled(String enabled) {
        if ("true".equalsIgnoreCase(enabled)) {
            analytics.setAnalyticsCollectionEnabled(true);
        } else {
            analytics.setAnalyticsCollectionEnabled(false);
        }
    }

    @YFunction(name = "ymnfirebase_get_messaging_token")
    public void getMessagingToken(String targetCode) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    return;
                }
                String token = task.getResult();
                sendResult(Integer.parseInt(targetCode), token);
            }
        });
    }

    @YFunction(name = "ymnfirebase_check_messaging_permission")
    public void checkMessagingPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Logger.i("POST_NOTIFICATIONS PERMISSION_REQUEST");
                int requestCode = "ymnfirebase_check_messaging_permission".hashCode();
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        Math.abs(requestCode));
            } else {
                Logger.i("POST_NOTIFICATIONS PERMISSION_GRANTED");
            }
        } else {
            Logger.i("POST_NOTIFICATIONS Build.VERSION.SDK_INT = " + Build.VERSION.SDK_INT);
        }
    }
}
