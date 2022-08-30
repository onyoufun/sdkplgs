package com.linxcool.sdkface.adjust;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustEvent;
import com.linxcool.sdkface.feature.YmnPluginWrapper;
import com.linxcool.sdkface.feature.protocol.YFunction;
import com.linxcool.sdkface.feature.protocol.YPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@YPlugin(strategy = YPlugin.Policy.FORCE, entrance = YPlugin.Entrance.ACTIVITY)
public class AdjustInterface extends YmnPluginWrapper {

    @Override
    public String getPluginId() {
        return "401";
    }

    @Override
    public String getPluginName() {
        return "adjust";
    }

    @Override
    public int getPluginVersion() {
        return 1;
    }

    @Override
    public String getSdkVersion() {
        return "4.31.0";
    }

    @Override
    public void onInit(Context context) {
        super.onInit(context);
    }

    @YFunction(name = "adjust_track_event")
    public void onTrackEvent(String name, String eventParams) {
        AdjustEvent adjustEvent = new AdjustEvent(name);
        if (!TextUtils.isEmpty(eventParams)) {
            Map map = getMap(eventParams);
            for (Object key : map.keySet()) {
                String type = String.valueOf(key);
                String value = String.valueOf(map.get(type));
                adjustEvent.addPartnerParameter(type, value);
            }
            Adjust.trackEvent(adjustEvent);
        }
    }

    @YFunction(name = "adjust_track_revenue")
    public void onTrackRevenue(String name, String revenue, String currency) {
        AdjustEvent adjustEvent = new AdjustEvent(name);
        // currency code -> https://help.adjust.com/zh/article/supported-currencies
        adjustEvent.setRevenue(Float.parseFloat(revenue),currency);
        Adjust.trackEvent(adjustEvent);
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
