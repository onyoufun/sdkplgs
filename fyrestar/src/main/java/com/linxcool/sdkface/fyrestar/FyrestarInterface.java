package com.linxcool.sdkface.fyrestar;

import static com.linxcool.sdkface.YmnPaymentCode.PAYRESULT_FAIL;
import static com.linxcool.sdkface.YmnPaymentCode.PAYRESULT_SUCCESS;
import static com.linxcool.sdkface.YmnUserCode.ACTION_RET_INIT_FAIL;
import static com.linxcool.sdkface.YmnUserCode.ACTION_RET_INIT_SUCCESS;
import static com.linxcool.sdkface.YmnUserCode.ACTION_RET_LOGIN_FAIL;
import static com.linxcool.sdkface.YmnUserCode.ACTION_RET_LOGIN_SUCCESS;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.foyoent.vjpsdk.FYSDK;
import com.foyoent.vjpsdk.agent.common.FYPayPlatform;
import com.foyoent.vjpsdk.agent.common.FYPlatform;
import com.foyoent.vjpsdk.agent.listener.FYCallback;
import com.foyoent.vjpsdk.agent.model.FoyoOrderParam;
import com.foyoent.vjpsdk.agent.pay.PayType;
import com.foyoent.vjpsdk.agent.statistics.FoyoJPStatistic;
import com.linxcool.sdkface.feature.YmnPluginWrapper;
import com.linxcool.sdkface.feature.protocol.YFunction;
import com.linxcool.sdkface.feature.protocol.YPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@YPlugin(strategy = YPlugin.Policy.FORCE, entrance = YPlugin.Entrance.ACTIVITY)
public class FyrestarInterface extends YmnPluginWrapper {

    private static final int YMNADMOB_REWARD_REQUEST_SUCCESS = 12307;// 请求奖励式广告成功
    private static final int YMNADMOB_REWARD_REQUEST_ERROR = 12308;// 请求奖励式广告失败
    private static final int YMNADMOB_REWARD_DISPLAYED = 12309;// 展示奖励式广告
    private static final int YMNADMOB_REWARD_CLOSED = 12310;// 关闭奖励式广告
    private static final int YMNADMOB_REWARD_CLICKED = 12311;// 点击了奖励式广告
    private static final int YMNADMOB_REWARD_GETREWARD = 12312;// 奖励式广告播放完毕，发放奖励
    private static final int YMNADMOB_REWARD_READY = 12313;// 奖励广告准备完毕
    private static final int YMNADMOB_REWARD_UNREADY = 12314;// 奖励广告未准备完毕
    private static final int YMNADMOB_REWARD_SHOWFAILD = 12315;// 奖励广告未准备完毕

    @Override
    public String getPluginId() {
        return "601";
    }

    @Override
    public String getPluginName() {
        return "fyrestar";
    }

    @Override
    public int getPluginVersion() {
        return 1;
    }

    @Override
    public String getSdkVersion() {
        return "1.0.0";
    }

    @Override
    public void onInit(Context context) {
        super.onInit(context);
        FYSDK.getInstance().onCreate(getActivity());
        //activity是游戏的主activity
        //v是一个JSONObject对象
        FYSDK.getInstance().init(getActivity(), v -> {
            if(v.optInt("code") == 1) {
                sendResult(ACTION_RET_INIT_SUCCESS, "onSuccess");
                //广告配置初始化
                String placementIds = getPropertie("admobPlacementId");
                String[] array = placementIds.split(";");
                TreeMap treeMap = new TreeMap();
                treeMap.put("adIds",Arrays.asList(array));
                FYSDK.getInstance().setAdConfig(treeMap);
                OnLoadRewardAd();
            } else {
                sendResult(ACTION_RET_INIT_FAIL, v.optString("message"));
            }
        });
    }

    @YFunction(name = "fyrestar_login")
    public void onLogin(String loginType) {
        //FYPlatform.Guest:游客登录
        //FYPlatform.Google:Google登录
        //FYPlatform.Facebook:Facebook登录
        //FYPlatform.Line:Line登录
        //FYPlatform.LineGame:LineGame登录
        //FYPlatform.Twitter:Twitter登录
        FYSDK.getInstance().login(Integer.parseInt(loginType), new FYCallback<JSONObject>() {
            @Override
            public void onResult(JSONObject result) {
                if(result.optInt("code") == 1) {
                    sendResult(ACTION_RET_LOGIN_SUCCESS, result.optJSONObject("data").toString());
                } else {
                    sendResult(ACTION_RET_LOGIN_FAIL, result.optString("message"));
                }
            }
        });
    }

    @YFunction(name = "fyrestar_pay")
    public void onPay(String productId, String productName, String orderId, String roleId, String roleName, String payType, String ext) {
        FoyoOrderParam op = new FoyoOrderParam();
        op.setExt(ext);
        op.setGoodsId(productId);//内购id
        op.setGoodsName(productName);
        op.setRoleId(roleId);
        op.setRoleName(roleName);
        op.setRolelevel(1);//玩家角色等级
        op.setSId("0");//区服ID
        op.setCpOrderId(orderId);

        //商品类型包含:PayType.INAPP->内购支付 int INAPP = 1;
        //商品类型包含:PayType.SUB->订阅支付 int SUB = 2;
        //如果支付平台为paypal,则这个参数无意义
        op.setPayType(Integer.parseInt(payType));

        //设置支付平台
        //支付平台包含:FYPayPlatform.Google->google支付
        //支付平台包含:FYPayPlatform.Paypal->paypal支付
        op.setPlatform(FYPayPlatform.Google);

        FYSDK.getInstance().foyoPay(op, result->{
            if(result.optInt("code") == 1) {
                sendResult(PAYRESULT_SUCCESS, result.optJSONObject("data").toString());
            } else {
                sendResult(PAYRESULT_FAIL, result.optString("message"));
            }
        });
    }

    @YFunction(name = "fyrestar_log_event")
    public void onLogEvent(String name, String eventParams) {
        if (!TextUtils.isEmpty(eventParams)) {
            HashMap<String, String> eventValues = new HashMap<String, String>();
            eventValues = getMap(eventParams);
            FoyoJPStatistic.getInstance().customEvent(name, eventValues);
        } else {
            FoyoJPStatistic.getInstance().customEvent(name);
        }
    }

    private boolean isAdReady = false;
    private boolean isAdLoading = false;
    @YFunction(name = "fyrestar_load_rewardad")
    public void OnLoadRewardAd() {
        if(isAdLoading) return;
        isAdReady = false;
        isAdLoading = true;
        FYSDK.getInstance().loadAd(result->{
            isAdLoading = false;
            if(result.optInt("code") == 1) {
                isAdReady = true;
                sendResult(YMNADMOB_REWARD_READY, result.optString("message"));
            } else {
                sendResult(YMNADMOB_REWARD_UNREADY, result.optString("message"));
            }
        });
    }

    @YFunction(name = "fyrestar_show_rewardad")
    public void OnShowRewardAd(String roleId) {
        if(isAdReady){
            //展示广告
            TreeMap treeMap = new TreeMap();
            treeMap.put("server_id","0");
            treeMap.put("role_id",roleId);
            treeMap.put("cp_oid",System.currentTimeMillis() + "");
            FYSDK.getInstance().showAd(treeMap,result->{
                if(result.optInt("code") == 1) {
                    sendResult(YMNADMOB_REWARD_GETREWARD, result.optString("message"));
                } else {
                    sendResult(YMNADMOB_REWARD_SHOWFAILD, result.optString("message"));
                }
                OnLoadRewardAd();
            });
        } else {
            sendResult(YMNADMOB_REWARD_UNREADY, "Ad is not load");
            OnLoadRewardAd();
        }
        isAdReady = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(!FYSDK.getInstance().onActivityResult(requestCode,resultCode,data)){
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onPause() {
        FYSDK.getInstance().onPause(getActivity());
    }

    @Override
    public void onResume() {
        FYSDK.getInstance().onResume(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FYSDK.getInstance().onDestory();
    }


    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        FYSDK.getInstance().onNewIntent(getActivity(),intent);
    }

    public HashMap<String, String> getMap(String jsonString) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);
            Iterator<String> keyIter = jsonObject.keys();
            String key;
            String value;
            HashMap<String, String> valueMap = new HashMap<String, String>();
            while (keyIter.hasNext()) {
                key = (String) keyIter.next();
                value = jsonObject.optString(key);
                valueMap.put(key, value);
            }
            return valueMap;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}