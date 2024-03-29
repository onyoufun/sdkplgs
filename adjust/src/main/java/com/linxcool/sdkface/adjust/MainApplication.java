package com.linxcool.sdkface.adjust;

import android.app.Application;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.LogLevel;
import com.linxcool.sdkface.YmnApplication;
import com.linxcool.sdkface.feature.YmnProperties;

public abstract class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        String appToken = getAdjustToken();
        String environment = AdjustConfig.ENVIRONMENT_PRODUCTION;//ENVIRONMENT_SANDBOX
        AdjustConfig config = new AdjustConfig(this, appToken, environment);
//        config.setLogLevel(LogLevel.VERBOSE);//todo test
        Adjust.onCreate(config);
        registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
    }

    public abstract String getAdjustToken();

    private static final class AdjustLifecycleCallbacks implements ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
            Adjust.onResume();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Adjust.onPause();
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {

        }
    }
}
