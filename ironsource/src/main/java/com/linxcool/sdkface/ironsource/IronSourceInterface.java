package com.linxcool.sdkface.ironsource;


import android.content.Context;

import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.integration.IntegrationHelper;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.InitializationListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;
import com.linxcool.sdkface.YmnCode;
import com.linxcool.sdkface.feature.YmnPluginWrapper;
import com.linxcool.sdkface.feature.protocol.YFunction;
import com.linxcool.sdkface.feature.protocol.YPlugin;
import com.linxcool.sdkface.util.Logger;

@YPlugin(strategy = YPlugin.Policy.FORCE, entrance = YPlugin.Entrance.ACTIVITY)
public class IronSourceInterface extends YmnPluginWrapper implements YmnCode {

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

    private String ironsourceAppKey;

    @Override
    public String getPluginId() {
        return "201";
    }

    @Override
    public String getPluginName() {
        return "ironsource";
    }

    @Override
    public int getPluginVersion() {
        return 1;
    }

    @Override
    public String getSdkVersion() {
        return "7.2.1";
    }

    InterstitialListener interstitialListener = new InterstitialListener() {
        @Override
        public void onInterstitialAdReady() {
            Logger.i("IronSource 广告准备完毕");
            sendResult(YMNIS_INTERSTITIAL_REQUEST_SUCCESS,"插屏广告请求成功");
        }

        @Override
        public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
            Logger.e("IronSource 广告加载失败");
            sendResult(YMNIS_INTERSTITIAL_REQUEST_FAIL,"插屏广告请求失败");
        }

        @Override
        public void onInterstitialAdOpened() {
            Logger.e("IronSource onInterstitialAdOpened");
        }

        @Override
        public void onInterstitialAdClosed() {
            Logger.i("IronSource onInterstitialAdClosed");
            sendResult(YMNIS_INTERSTITIAL_CLOSE,"关闭插屏广告");
        }

        @Override
        public void onInterstitialAdShowSucceeded() {
            Logger.i("IronSource onInterstitialAdShowSucceeded");
            sendResult(YMNIS_INTERSTITIAL_DISPLAYED,"成功展示插屏广告");
        }

        @Override
        public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
            Logger.e("IronSource onInterstitialAdShowFailed");
        }

        @Override
        public void onInterstitialAdClicked() {
            Logger.e("IronSource onInterstitialAdClicked");
            sendResult(YMNIS_INTERSTITIAL_CLICKED,"点击了插屏广告");
        }
    };

    RewardedVideoListener rewardedVideoListener = new RewardedVideoListener() {
        @Override
        public void onRewardedVideoAdOpened() {
            Logger.i("IronSource onRewardedVideoAdOpened");
            sendResult(YMNIS_REWARD_DISPLAYED,"展示奖励广告");
        }

        @Override
        public void onRewardedVideoAdClosed() {
            Logger.i("IronSource onRewardedVideoAdClosed");
            sendResult(YMNIS_REWARD_CLOSED,"关闭奖励广告");
        }

        @Override
        public void onRewardedVideoAvailabilityChanged(boolean b) {
            if (b) sendResult(YMNIS_REWARD_REQUEST_SUCCESS,"奖励广告请求完毕");
            Logger.i("IronSource onRewardedVideoAvailabilityChanged");
        }

        @Override
        public void onRewardedVideoAdStarted() {
            Logger.i("IronSource onRewardedVideoAdStarted");
        }

        @Override
        public void onRewardedVideoAdEnded() {
            Logger.i("IronSource onRewardedVideoAdEnded");
            preloadRewardedAd();
        }

        @Override
        public void onRewardedVideoAdRewarded(Placement placement) {
            Logger.i("IronSource onRewardedVideoAdRewarded");
            sendResult(YMNIS_REWARD_GETREWARD,"看完了奖励广告,发放奖励");
        }

        @Override
        public void onRewardedVideoAdShowFailed(IronSourceError ironSourceError) {
            Logger.e("IronSource onRewardedVideoAdShowFailed");
        }

        @Override
        public void onRewardedVideoAdClicked(Placement placement) {
            Logger.e("IronSource onRewardedVideoAdClicked");
            sendResult(YMNIS_REWARD_CLICKED,"点击了奖励广告");
        }
    };

    @Override
    public void onInit(Context context) {
        super.onInit(context);
        ironsourceAppKey = getPropertie("ironsourceAppKey");
        IronSource.setInterstitialListener(interstitialListener);
        IronSource.setRewardedVideoListener(rewardedVideoListener);
        // IntegrationHelper.validateIntegration(getActivity());
        IronSource.init(getActivity(), ironsourceAppKey, new InitializationListener() {
            @Override
            public void onInitializationComplete() {
                sendResult(ACTION_RET_INIT_SUCCESS, "ironsource 初始化成功");
            }
        });
    }

    @YFunction(name = "ymnis_request_interstitial_ad")
    public void preloadInterstitialAd() {
        IronSource.init(getActivity(), ironsourceAppKey, IronSource.AD_UNIT.INTERSTITIAL);
        IronSource.loadInterstitial();
    }

    @YFunction(name = "ymnis_is_interstitial_ad_ready")
    public void isInterstitialReady() {
        if (IronSource.isInterstitialReady()) {
            sendResult(YMNIS_INTERSTITIAL_READY, "插屏广告准备完毕");
        }else {
            sendResult(YMNIS_INTERSTITIAL_UNREADY, "插屏广告未准备完毕");
        }
    }

    @YFunction(name = "ymnis_show_interstitial_ad")
    public void showInterstitial() {
        if (IronSource.isInterstitialReady()) {
            IronSource.showInterstitial();
        }else {
            sendResult(YMNIS_INTERSTITIAL_UNREADY, "插屏广告未准备完毕");
        }
    }

    @YFunction(name = "ymnis_request_reward_ad")
    public void preloadRewardedAd() {
        IronSource.init(getActivity(), ironsourceAppKey, IronSource.AD_UNIT.REWARDED_VIDEO);
    }

    @YFunction(name = "ymnis_is_reward_ad_ready")
    public void isRewardedVideoAvailable() {
        if (IronSource.isRewardedVideoAvailable()) {
            sendResult(YMNIS_REWARD_READY, "奖励广告准备完毕");
        }else {
            sendResult(YMNIS_REWARD_UNREADY, "奖励广告未准备完毕");
        }
    }

    @YFunction(name = "ymnis_show_reward_ad")
    public void showRewardedVideo() {
        if (IronSource.isRewardedVideoAvailable()) {
            IronSource.showRewardedVideo();
        }else {
            sendResult(YMNIS_REWARD_UNREADY, "奖励广告未准备完毕");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IronSource.onResume(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        IronSource.onPause(getActivity());
    }
}
