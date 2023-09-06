package com.linxcool.sdkface.minigame;

import android.content.Context;
import android.content.Intent;

import com.linxcool.sdkface.feature.YmnPluginWrapper;
import com.linxcool.sdkface.feature.protocol.YFunction;
import com.linxcool.sdkface.feature.protocol.YPlugin;

@YPlugin(strategy = YPlugin.Policy.FORCE, entrance = YPlugin.Entrance.ACTIVITY)
public class MiniGameInterface extends YmnPluginWrapper {

    @Override
    public String getPluginId() {
        return "901";
    }

    @Override
    public String getPluginName() {
        return "minigame";
    }

    @Override
    public int getPluginVersion() {
        return 1;
    }

    @Override
    public String getSdkVersion() {
        return "1.1.0";
    }

    @Override
    public void onInit(Context context) {
        super.onInit(context);
    }

    @YFunction(name = "minigame_load_h5game")
    public void loadH5Game() {
        Intent intent = new Intent(getContext(), WebViewActivity.class);
        getActivity().startActivity(intent);
    }
}
