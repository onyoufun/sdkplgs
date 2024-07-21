package com.linxcool.sdkface.admob;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.ironsource.mediationsdk.IronSource;
import com.linxcool.sdkface.YmnCode;
import com.linxcool.sdkface.feature.YmnPluginWrapper;
import com.linxcool.sdkface.feature.protocol.YFunction;
import com.linxcool.sdkface.feature.protocol.YPlugin;
import com.linxcool.sdkface.util.Logger;

import java.util.Map;

@YPlugin(strategy = YPlugin.Policy.FORCE, entrance = YPlugin.Entrance.ACTIVITY)
public class AdmobInterface extends YmnPluginWrapper implements YmnCode {

    public static final int AD_INTERSTITIAL_REQUEST_SUCCESS = 12300;// 插屏广告请求成功
    public static final int AD_INTERSTITIAL_REQUEST_FAIL = 12301;// 插屏广告请求失败
    public static final int AD_INTERSTITIAL_CLOSE = 12302;// 关闭插屏广告
    public static final int AD_INTERSTITIAL_DISPLAYED = 12303;// 展示插屏广告
    public static final int AD_INTERSTITIAL_CLICKED = 12304;// 点击了插屏广告
    public static final int AD_INTERSTITIAL_READY = 12305;// 插屏广告准备完毕
    public static final int AD_INTERSTITIAL_UNREADY = 12306;// 插屏广告未准备完毕

    public static final int AD_REWARD_REQUEST_SUCCESS = 12307;// 请求奖励式广告成功
    public static final int AD_REWARD_REQUEST_ERROR = 12308;// 请求奖励式广告失败
    public static final int AD_REWARD_DISPLAYED = 12309;// 展示奖励式广告
    public static final int AD_REWARD_CLOSED = 12310;// 关闭奖励式广告
    public static final int AD_REWARD_CLICKED = 12311;// 点击了奖励式广告
    public static final int AD_REWARD_GETREWARD = 12312;// 奖励式广告播放完毕，发放奖励
    public static final int AD_REWARD_READY = 12313;// 奖励广告准备完毕
    public static final int AD_REWARD_UNREADY = 12314;// 奖励广告未准备完毕
    public static final int YMNADMOB_REWARD_SHOWFAILD = 12315;// 奖励广告观看失败

    private String rewardUnitId = "ca-app-pub-3940256099942544/5224354917";
    private String interstitialUnitId = "ca-app-pub-3940256099942544/1033173712";

    private RewardedAd rewardedAd;
    private InterstitialAd interstitialAd;

    @Override
    public String getPluginId() {
        return "201";
    }

    @Override
    public String getPluginName() {
        return "admob";
    }

    @Override
    public int getPluginVersion() {
        return 1;
    }

    @Override
    public String getSdkVersion() {
        return "";
    }

    @Override
    public void onInit(Context context) {
        super.onInit(context);

        // ironSource 欧盟同意GDPR
        IronSource.setConsent(true);
        // ironSource 添加支持CCPA
        IronSource.setMetaData("do_not_sell", "true");

        MobileAds.initialize(getActivity(), initializationStatus -> {
            sendResult(ACTION_RET_INIT_SUCCESS, "admob 初始化成功");
            Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
            for (String adapterClass : statusMap.keySet()) {
                AdapterStatus status = statusMap.get(adapterClass);
                Logger.d(String.format("Admob adapter name: %s, Description: %s, Latency: %d", adapterClass, status.getDescription(), status.getLatency()));
            }
        });
    }

    @YFunction(name = "admob_request_reward_ad")
    public void preloadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(getActivity(), rewardUnitId, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                // Handle the error.
                Logger.d(loadAdError.toString());
                rewardedAd = null;
                sendResult(AD_REWARD_REQUEST_ERROR, loadAdError.toString());
            }

            @Override
            public void onAdLoaded(RewardedAd ad) {
                rewardedAd = ad;
                Logger.d("Ad was loaded.");
                sendResult(AD_REWARD_REQUEST_SUCCESS, "onAdLoaded " + ad.getAdUnitId());
                sendResult(AD_REWARD_READY, "onAdLoaded");
            }
        });
    }

    @YFunction(name = "admob_show_reward_ad")
    public void showRewardedVideo() {
        if (rewardedAd != null) {
            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                    Logger.d("onAdClicked");
                    sendResult(AD_REWARD_CLICKED, "onAdClicked");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    // Set the ad reference to null so you don't show the ad a second time.
                    Logger.d("Ad dismissed fullscreen content.");
                    rewardedAd = null;
                    sendResult(AD_REWARD_CLOSED, "onAdDismissedFullScreenContent");
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    // Called when ad fails to show.
                    Logger.e("Ad failed to show fullscreen content.");
                    rewardedAd = null;
                    sendResult(YMNADMOB_REWARD_SHOWFAILD, "onAdFailedToShowFullScreenContent");
                }

                @Override
                public void onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    Logger.d("Ad recorded an impression.");
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    Logger.d("Ad showed fullscreen content.");
                    sendResult(AD_REWARD_DISPLAYED, "onAdShowedFullScreenContent");
                }
            });
            rewardedAd.show(getActivity(), new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    Logger.d("The user earned the reward.");
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                    sendResult(AD_REWARD_GETREWARD, "onUserEarnedReward");
                }
            });
        } else {
            Logger.d("The rewarded ad wasn't ready yet.");
            sendResult(AD_REWARD_UNREADY, "rewarded is null");
        }
    }

    @YFunction(name = "admob_request_interstitial_ad")
    public void preloadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(getActivity(),interstitialUnitId, adRequest,
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd rsAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    interstitialAd = rsAd;
                    Logger.i("onAdLoaded");
                    sendResult(AD_INTERSTITIAL_REQUEST_SUCCESS, "onAdLoaded");
                    sendResult(AD_INTERSTITIAL_READY, "onAdLoaded");
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    // Handle the error
                    Logger.d(loadAdError.toString());
                    interstitialAd = null;
                    sendResult(AD_INTERSTITIAL_REQUEST_FAIL, loadAdError.toString());
                }
            });
    }

    @YFunction(name = "admob_show_interstitial_ad")
    public void showInterstitialAd() {
        if (interstitialAd != null) {
            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                    Logger.d("onAdClicked");
                    sendResult(AD_INTERSTITIAL_CLICKED, "onAdClicked");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    // Set the ad reference to null so you don't show the ad a second time.
                    Logger.d("Ad dismissed fullscreen content.");
                    interstitialAd = null;
                    sendResult(AD_INTERSTITIAL_CLOSE, "onAdDismissedFullScreenContent");
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    // Called when ad fails to show.
                    Logger.e("Ad failed to show fullscreen content.");
                    interstitialAd = null;
                    sendResult(AD_INTERSTITIAL_CLOSE, "onAdFailedToShowFullScreenContent");
                }

                @Override
                public void onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    Logger.d("Ad recorded an impression.");
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    Logger.d("Ad showed fullscreen content.");
                    sendResult(AD_INTERSTITIAL_DISPLAYED, "onAdShowedFullScreenContent");
                }
            });
            interstitialAd.show(getActivity());
        } else {
            Logger.d("The interstitial ad wasn't ready yet.");
            sendResult(AD_INTERSTITIAL_UNREADY, "interstitial is null");
        }
    }

    @YFunction(name = "admob_show_banner_ad")
    public void showBannerAd() {
        Logger.e("还未实现Banner广告");
    }

    @YFunction(name = "admob_show_rewardinter_ad")
    public void showRewardinterAd() {
        Logger.e("还未实现激励插屏广告");
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
