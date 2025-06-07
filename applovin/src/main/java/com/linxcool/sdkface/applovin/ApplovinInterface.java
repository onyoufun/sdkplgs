package com.linxcool.sdkface.applovin;


import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;
import com.linxcool.sdkface.YmnCode;
import com.linxcool.sdkface.feature.YmnPluginWrapper;
import com.linxcool.sdkface.feature.protocol.YFunction;
import com.linxcool.sdkface.feature.protocol.YPlugin;
import com.linxcool.sdkface.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@YPlugin(strategy = YPlugin.Policy.FORCE, entrance = YPlugin.Entrance.ACTIVITY)
public class ApplovinInterface extends YmnPluginWrapper implements YmnCode {

    private static final int YMNIS_INTERSTITIAL_REQUEST_SUCCESS = 12500;// 插屏广告请求成功
    private static final int YMNIS_INTERSTITIAL_REQUEST_FAIL = 12501;// 插屏广告请求失败
    private static final int YMNIS_INTERSTITIAL_CLOSE = 12502;// 关闭插屏广告
    private static final int YMNIS_INTERSTITIAL_DISPLAYED = 12503;// 展示插屏广告
    private static final int YMNIS_INTERSTITIAL_CLICKED = 12504;// 点击了插屏广告
    private static final int YMNIS_INTERSTITIAL_READY = 12505;// 插屏广告准备完毕
    private static final int YMNIS_INTERSTITIAL_UNREADY = 12506;// 插屏广告未准备完毕

    private static final int YMNIS_REWARD_REQUEST_SUCCESS = 12507;// 请求奖励式广告成功
    private static final int YMNIS_REWARD_REQUEST_ERROR = 12508;// 请求奖励式广告失败
    private static final int YMNIS_REWARD_DISPLAYED = 12509;// 展示奖励式广告
    private static final int YMNIS_REWARD_CLOSED = 12510;// 关闭奖励式广告
    private static final int YMNIS_REWARD_CLICKED = 12511;// 点击了奖励式广告
    private static final int YMNIS_REWARD_GETREWARD = 12512;// 奖励式广告播放完毕，发放奖励
    private static final int YMNIS_REWARD_READY = 12513;// 奖励广告准备完毕
    private static final int YMNIS_REWARD_UNREADY = 12514;// 奖励广告未准备完毕

    private String applovinAppKey;
    private boolean needLoadedNotify = false;

    @Override
    public String getPluginId() {
        return "203";
    }

    @Override
    public String getPluginName() {
        return "applovin";
    }

    @Override
    public int getPluginVersion() {
        return 1;
    }

    @Override
    public String getSdkVersion() {
        return "1.2.1";
    }

    private List<MaxRewardedAd> rewardedAds = new ArrayList<>();

    @Override
    public void onInit(Context context) {
        super.onInit(context);
        applovinAppKey = getPropertie("applovinAppKey");
        AppLovinSdkInitializationConfiguration initConfig = AppLovinSdkInitializationConfiguration
                .builder( applovinAppKey )
                .setMediationProvider( AppLovinMediationProvider.MAX )
                .build();
        AppLovinSdk.getInstance(getActivity()).initialize(initConfig, sdkConfig -> {
            sendResult(ACTION_RET_INIT_SUCCESS, "applovin 初始化成功");
            preloadRewardedAd();
        });

        List<String> rewardedIds = null;
        String unitIdsString = getPropertie("applovinUnitIds");
        if(unitIdsString != null && unitIdsString.length() > 0) {
            rewardedIds = Arrays.asList(unitIdsString.split("-"));
        } else {
            rewardedIds = new ArrayList<>();
            Logger.e("applovinUnitIds is empty");
        }
        for (String adUnitId : rewardedIds) {
            rewardedAds.add(createRewardedAd(adUnitId));
        }

        if(isDebugMode()) {
            Logger.i("applovinAppKey = " + applovinAppKey);
            AppLovinSdk.getInstance(context).showCreativeDebugger();
        } else {
            AppLovinSdk.getInstance(context).getSettings().setCreativeDebuggerEnabled(false);
        }
    }

    private MaxRewardedAd createRewardedAd(String adUnitId) {
        MaxRewardedAd rewardedAd = MaxRewardedAd.getInstance(adUnitId);
        rewardedAd.setListener( new MaxRewardedAdListener() {

            private int retryAttempt = 0;

            @Override
            public void onAdLoaded(@NonNull MaxAd maxAd) {
                if(needLoadedNotify) {
                    sendResult(YMNIS_REWARD_REQUEST_SUCCESS, "奖励广告请求成功");
                    needLoadedNotify = false;
                }
                retryAttempt = 0;
            }

            @Override
            public void onAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
                if(needLoadedNotify) {
                    sendResult(YMNIS_REWARD_REQUEST_ERROR, "奖励广告请求失败，正在重试");
                    needLoadedNotify = false;
                }
                retryAttempt++;
                long delayMillis = TimeUnit.SECONDS.toMillis( (long) Math.pow( 2, Math.min( 6, retryAttempt ) ) );
                new Handler().postDelayed(() -> rewardedAd.loadAd(), delayMillis );
            }

            @Override
            public void onAdDisplayed(@NonNull MaxAd maxAd) {
                Logger.i(adUnitId + "Applovin onAdDisplayed");
                sendResult(YMNIS_REWARD_DISPLAYED,"展示奖励广告");
            }

            @Override
            public void onAdHidden(@NonNull MaxAd maxAd) {
                Logger.i(adUnitId + "Applovin onAdHidden");
                rewardedAd.loadAd();
            }

            @Override
            public void onAdClicked(@NonNull MaxAd maxAd) {
                Logger.e(adUnitId + "Applovin onAdClicked");
                sendResult(YMNIS_REWARD_CLICKED,"点击了奖励广告");
            }


            @Override
            public void onAdDisplayFailed(@NonNull MaxAd maxAd, @NonNull MaxError maxError) {
                Logger.e(adUnitId + "Applovin onAdDisplayFailed");
                sendResult(YMNIS_REWARD_CLOSED,"奖励广告展示失败，正在重新加载");
                rewardedAd.loadAd();
            }

            @Override
            public void onUserRewarded(@NonNull MaxAd maxAd, @NonNull MaxReward maxReward) {
                Logger.i(adUnitId + "Applovin onUserRewarded");
                sendResult(YMNIS_REWARD_GETREWARD,"看完了奖励广告,发放奖励");
                rewardedAd.loadAd();
            }

        } );
        rewardedAd.loadAd();
        return rewardedAd;
    }


    @YFunction(name = "applovin_request_reward_ad")
    public void preloadRewardedAd() {
        needLoadedNotify = true;
        for (MaxRewardedAd rewardedAd: rewardedAds) {
            if(!rewardedAd.isReady())
                rewardedAd.loadAd();
        }
    }

    @YFunction(name = "applovin_is_reward_ad_ready")
    public void isRewardedVideoAvailable() {
        for (MaxRewardedAd rewardedAd: rewardedAds) {
            if (rewardedAd.isReady()) {
                sendResult(YMNIS_REWARD_READY, "奖励广告准备完毕");
                break;
            }
        }
        sendResult(YMNIS_REWARD_UNREADY, "奖励广告未准备完毕");
    }

    @YFunction(name = "applovin_show_reward_ad")
    public void showRewardedVideo() {
        boolean foundAd = false;
        for (MaxRewardedAd rewardedAd: rewardedAds) {
            if (rewardedAd.isReady()) {
                foundAd = true;
                rewardedAd.showAd( getActivity() );
                break;
            }
        }
        if(!foundAd) {
            this.preloadRewardedAd();
            sendResult(YMNIS_REWARD_UNREADY, "奖励广告未准备完毕");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
