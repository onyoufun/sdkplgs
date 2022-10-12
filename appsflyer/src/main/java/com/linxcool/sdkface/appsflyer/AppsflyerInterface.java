package com.linxcool.sdkface.appsflyer;

import android.content.Context;
import android.text.TextUtils;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerLib;
import com.linxcool.sdkface.feature.YmnPluginWrapper;
import com.linxcool.sdkface.feature.protocol.YFunction;
import com.linxcool.sdkface.feature.protocol.YPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@YPlugin(strategy = YPlugin.Policy.FORCE, entrance = YPlugin.Entrance.ACTIVITY)
public class AppsflyerInterface extends YmnPluginWrapper {

    @Override
    public String getPluginId() {
        return "501";
    }

    @Override
    public String getPluginName() {
        return "appsflyer";
    }

    @Override
    public int getPluginVersion() {
        return 1;
    }

    @Override
    public String getSdkVersion() {
        return "6.9.0";
    }

    @Override
    public void onInit(Context context) {
        super.onInit(context);
    }

    @YFunction(name = "appsflyer_log_event")
    public void onLogEvent(String name, String eventParams) {
        Map<String, Object> eventValues = new HashMap<String, Object>();
        if (!TextUtils.isEmpty(eventParams)) {
            eventValues = getMap(eventParams);
        }
        AppsFlyerLib.getInstance().logEvent(getContext(), name , eventValues);
    }

    @YFunction(name = "appsflyer_log_revenue")
    public void onTrackRevenue(String name, String revenue, String currency) {
        Map<String, Object> eventValues = new HashMap<String, Object>();
        eventValues.put(AFInAppEventParameterName.CURRENCY, currency);
        eventValues.put(AFInAppEventParameterName.REVENUE, revenue);

        AppsFlyerLib.getInstance().logEvent(getContext(), AFInAppEventType.PURCHASE, eventValues);
    }

    public Map<String, Object> getMap(String jsonString) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);
            @SuppressWarnings("unchecked")
            Iterator<String> keyIter = jsonObject.keys();
            String key;
            Object value;
            Map<String, Object> valueMap = new HashMap<String, Object>();
            while (keyIter.hasNext()) {
                key = (String) keyIter.next();
                value = jsonObject.get(key);
                valueMap.put(key, value);
            }
            return valueMap;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
