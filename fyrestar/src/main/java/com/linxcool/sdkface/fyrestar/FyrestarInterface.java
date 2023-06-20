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
import com.foyoent.vjpsdk.agent.listener.FYCallback;
import com.foyoent.vjpsdk.agent.model.FoyoOrderParam;
import com.foyoent.vjpsdk.agent.statistics.FYEvent;
import com.foyoent.vjpsdk.agent.statistics.FYEventCommontParam;
import com.foyoent.vjpsdk.agent.statistics.FYEventName;
import com.foyoent.vjpsdk.agent.statistics.FYEventParam;
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

    private Boolean isLoginRetry = false;
    private String serverId = "0";

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
                FYEvent.INSTANCE.logEvent(FYEventName.opengame);
                //广告配置初始化
                List<String> adIds = Arrays.asList("ca-app-pub-4770705390206387/6285601589");
                TreeMap treeMap = new TreeMap();
                treeMap.put("adIds",adIds);
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
                } else if(result.optInt("code") == -2209 || result.optInt("code") == -2218){
                    FYSDK.getInstance().clearUserCache();
                    if(isLoginRetry)
                        sendResult(ACTION_RET_LOGIN_FAIL, result.optString("message"));
                    else {
                        onLogin(loginType);
                        isLoginRetry = true;
                    }
                } else {
                    sendResult(ACTION_RET_LOGIN_FAIL, result.optString("message"));
                }
            }
        });
    }

    @YFunction(name = "fyrestar_set_serverid")
    public void setServerId(String serverId) {
        this.serverId = serverId;
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
        op.setSId(serverId);//区服ID
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
                JSONObject data = result.optJSONObject("data");
                sendResult(PAYRESULT_SUCCESS, data instanceof JSONObject ? data.toString() : "");
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
            FYEvent.INSTANCE.customEvent(name, eventValues);
        } else {
            FYEvent.INSTANCE.customEvent(name);
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
        if(isAdReady)
        {
            //展示广告
            TreeMap treeMap = new TreeMap();
            treeMap.put("server_id",serverId);
            treeMap.put("role_id",roleId);
            treeMap.put("cp_oid",System.currentTimeMillis() + "");
            FYSDK.getInstance().showAd(treeMap,result->{
                if(result.optInt("code") == 1) {
                    sendResult(YMNADMOB_REWARD_GETREWARD, result.optString("message"));
                } else {
                    sendResult(YMNADMOB_REWARD_SHOWFAILD, result.optString("message"));
                }
            });
        } else {
            sendResult(YMNADMOB_REWARD_SHOWFAILD, "REWARD_UNREADY");
        }

    }
    @YFunction(name = "fyrestar_role_login")
    public void OnRoleLogin(String roleId, String roleName) {
        HashMap map = new HashMap<FYEventCommontParam, String>();
        map.put(FYEventCommontParam.sid, serverId);//区服id
        map.put(FYEventCommontParam.roleid, roleId);//角色id
        map.put(FYEventCommontParam.sname, serverId);//区服名称
        map.put(FYEventCommontParam.rolename, roleName);//角色名称
        FYEvent.INSTANCE.setCommontParam(map);
    }

    @YFunction(name = "fyrestar_role_create")
    public void OnRoleCreate(String roleId, String roleName, String level, String vipLevel) {
        HashMap map = new HashMap<String, String>();
        map.put(FYEventParam.role_create_time, System.currentTimeMillis()/1000 + "");//创角时间，UNIX时间戳，如：1498810807
        map.put(FYEventParam.level, level);//角色等级
        FYEvent.INSTANCE.logEvent(FYEventName.createrole,map);
    }

    @YFunction(name = "fyrestar_enter_game")
    public void OnEnterGame(String roleId, String roleName, String level, String coinNum, String vipLevel) {
        HashMap map = new HashMap<FYEventParam, String>();
        map.put(FYEventParam.role_create_time, System.currentTimeMillis()/1000 + "");//创角时间，UNIX时间戳，如：1498810807
        map.put(FYEventParam.level, level);//角色等级
        FYEvent.INSTANCE.logEvent(FYEventName.entergame,map);
    }

    @YFunction(name = "fyrestar_role_levelup")
    public void OnRoleLevelUp(String roleId, String roleName, String levelBefore, String level, String vipLevel) {
        HashMap map = new HashMap<FYEventParam, String>();
        map.put(FYEventParam.level_before, levelBefore);//升级前的等级
        map.put(FYEventParam.level, level);//升级后的等级
        FYEvent.INSTANCE.logEvent(FYEventName.levelup,map);
    }

    @YFunction(name = "fyrestar_tutorial")
    public void OnTutorial(String level, String step) {
        FYEvent.INSTANCE.logEvent(FYEventName.completetutorial);
    }

    @YFunction(name = "fyrestar_compelete_load")
    public void OnCompleteLoadRes() {
        FYEvent.INSTANCE.logEvent(FYEventName.loadingcomplete);
    }

    @YFunction(name = "fyrestar_active_app")
    public void OnActiveApp() {
        FYEvent.INSTANCE.logEvent(FYEventName.install);
    }

    @YFunction(name = "fyrestar_stage_start")
    public void OnStageStart(String stageId) {
        HashMap map = new HashMap<FYEventParam, String>();
        map.put(FYEventParam.stage_id , stageId);//关卡ID
        FYEvent.INSTANCE.logEvent(FYEventName.stagestart,map);
    }

    @YFunction(name = "fyrestar_stage_success")
    public void OnStageSuccss(String stageId, String duration) {
        HashMap map = new HashMap<FYEventParam, String>();
        map.put(FYEventParam.stage_id, stageId);//关卡ID
        map.put(FYEventParam.duration, duration);//时长，单位：秒
        FYEvent.INSTANCE.logEvent(FYEventName.stagesuc,map);
    }

    @YFunction(name = "fyrestar_stage_fail")
    public void OnStageFail(String stageId, String duration) {
        HashMap map = new HashMap<FYEventParam, String>();
        map.put(FYEventParam.stage_id, stageId);//关卡ID
        map.put(FYEventParam.duration, duration);//时长，单位：秒
        FYEvent.INSTANCE.logEvent(FYEventName.stagefailed,map);
    }

    @YFunction(name = "fyrestar_adview")
    public void OnAdview() {
        FYEvent.INSTANCE.logEvent(FYEventName.adview);
    }

    @YFunction(name = "fyrestar_adview_count")
    public void OnAdviewCount(int count) {
        if(count == 10)
            FYEvent.INSTANCE.logEvent(FYEventName.adview_10);//观看广告10次
        else if(count == 30)
            FYEvent.INSTANCE.logEvent(FYEventName.adview_30);//观看广告30次
        else if(count == 50)
            FYEvent.INSTANCE.logEvent(FYEventName.adview_50);//观看广告50次
    }

    @YFunction(name = "fyrestar_purchase_click")
    public void OnPurchaseClick(String goodsId) {
        HashMap map = new HashMap<FYEventParam, String>();
        map.put(FYEventParam.goods_id , goodsId);//商品ID
        FYEvent.INSTANCE.logEvent(FYEventName.purchaseclick,map);
    }

    @YFunction(name = "fyrestar_purchase_fail")
    public void OnPurchaseFail(String goodsId) {
        HashMap map = new HashMap<FYEventParam, String>();
        map.put(FYEventParam.goods_id , goodsId);//商品ID
        FYEvent.INSTANCE.logEvent(FYEventName.purchasefailed,map);
    }

    @YFunction(name = "fyrestar_app_rate")
    public void OnAppRate() {
        FYEvent.INSTANCE.logEvent(FYEventName.af_rate);
    }

    @YFunction(name = "fyrestar_shop_open")
    public void OnShopOpen() {
        FYEvent.INSTANCE.logEvent(FYEventName.shopopen);
    }

    @YFunction(name = "fyrestar_shop_close")
    public void OnShopClose() {
        FYEvent.INSTANCE.logEvent(FYEventName.shopclose);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        FYSDK.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
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
