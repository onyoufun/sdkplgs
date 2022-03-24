package com.linxcool.sdkface.firebase;


import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.linxcool.sdkface.feature.YmnPluginWrapper;
import com.linxcool.sdkface.feature.protocol.YFunction;
import com.linxcool.sdkface.feature.protocol.YPlugin;

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

}
