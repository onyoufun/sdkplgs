package com.linxcool.sdkface.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.linxcool.sdkface.YmnSdk;
import com.linxcool.sdkface.YmnCallback;
import com.linxcool.sdkface.feature.protocol.IUserFeature;

/**
 * Created by huchanghai on 2017/9/5.
 */
public abstract class SdkWrapperActivity extends Activity implements YmnCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        YmnSdk.registPlugin("com.linxcool.sdkface.googleplay.GooglePlayInterface");
        YmnSdk.registPlugin("com.linxcool.sdkface.ironsource.IronSourceInterface");
        YmnSdk.setDebugMode(true);
        YmnSdk.registCallback(this);
        YmnSdk.initialize(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        YmnSdk.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        YmnSdk.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        YmnSdk.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        YmnSdk.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        YmnSdk.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        YmnSdk.onDestroy();
        YmnSdk.removeCallback(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        YmnSdk.onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        YmnSdk.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (YmnSdk.isSupportFunction(IUserFeature.FUNCTION_EXIT)) {
            YmnSdk.callFunction(IUserFeature.FUNCTION_EXIT);
        } else {
            super.onBackPressed();
        }
    }
}
