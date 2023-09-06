package com.linxcool.sdkface.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.linxcool.sdkface.YmnSdk;
import com.linxcool.sdkface.feature.protocol.IPaymentFeature;
import com.linxcool.sdkface.util.Logger;

import java.util.LinkedHashMap;

/**
 * Created by huchanghai on 2017/9/5.
 */
public class GameActivity extends SdkWrapperActivity implements Handler.Callback {

    private LinearLayout layoutPlugins;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        handler = new Handler(this);
        layoutPlugins = (LinearLayout) findViewById(R.id.layoutPlugins);
        refreshViews();

        YmnSdk.callFunction("ymnfirebase_check_messaging_permission");

    }

    public void refreshViews() {
        layoutPlugins.removeAllViews();

        FunctionViewFactory.registAdapter(new FunctionViewAdapter() {
            @Override
            public String getFunctionText() {
                return "登录";
            }
            @Override
            public void onClick(View view) {
                //YmnSdk.callFunction("template_login");
                YmnSdk.login();
            }
        });

        FunctionViewFactory.registAdapter(new FunctionViewAdapter() {
            @Override
            public String getFunctionText() {
                return "支付";
            }
            @Override
            public void onClick(View view) {
                LinkedHashMap<String, String> order = new LinkedHashMap<>();
                order.put(IPaymentFeature.ARG_PRODUCT_ID, "1");
                order.put(IPaymentFeature.ARG_TRADE_CODE, "001");
                YmnSdk.pay(order);
            }
        });

        FunctionViewFactory.registAdapter(new FunctionViewAdapter() {
            @Override
            public String getFunctionText() {
                return "广告";
            }
            @Override
            public void onClick(View view) {
                YmnSdk.callFunction("ymnis_show_reward_ad");
                //YmnSdk.callFunction("ymnis_request_reward_ad");
            }
        });

        FunctionViewFactory.registAdapter(new FunctionViewAdapter() {
            @Override
            public String getFunctionText() {
                return "分享";
            }
            @Override
            public void onClick(View view) {
                YmnSdk.callFunction("facebook_share", "1", "http://wwww.baidu.com", "quote");
            }
        });

        FunctionViewFactory.registAdapter(new FunctionViewAdapter() {
            @Override
            public String getFunctionText() {
                return "H5Game";
            }
            @Override
            public void onClick(View view) {
                YmnSdk.callFunction("minigame_load_h5game");
            }
        });

        for (FunctionViewAdapter adapter : FunctionViewFactory.getAdapters()) {
            View v = FunctionViewFactory.newView(this, adapter);
            layoutPlugins.addView(v);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        YmnSdk.hideToolBar();
    }

    @Override
    public boolean handleMessage(Message msg) {
        YmnSdk.showToolBar();
        return false;
    }

    @Override
    public void onCallBack(int code, String msg) {
        switch (code) {
            case 12514://YMNIS_REWARD_UNREADY
                //YmnSdk.callFunction("ymnis_request_reward_ad");
                break;
            case YmnSdk.ACTION_RET_INIT_SUCCESS:
            case YmnSdk.PAYRESULT_INIT_SUCCESS:
                toast("初始化成功");
                break;
            case YmnSdk.ACTION_RET_INIT_FAIL:
            case YmnSdk.PAYRESULT_INIT_FAIL:
                toast("初始化失败 - " + msg);
                break;
            case YmnSdk.ACTION_RET_LOGIN_SUCCESS:
                toast("登录成功");
                handler.sendEmptyMessageDelayed(0, 200);
                break;
            case YmnSdk.ACTION_RET_LOGIN_CANCEL:
                toast("登录取消");
                break;
            case YmnSdk.ACTION_RET_LOGIN_FAIL:
                toast("登录失败");
                break;
            case YmnSdk.PAYRESULT_SUCCESS:
                toast("支付成功");
                break;
            case YmnSdk.PAYRESULT_CANCEL:
                toast("支付取消");
                break;
            case YmnSdk.PAYRESULT_FAIL:
                toast("支付失败");
                break;
            case YmnSdk.ACTION_RET_EXIT_PAGE:
                finish();
                break;
            case YmnSdk.ACTION_RET_LOGOUT_SUCCESS:
            case YmnSdk.ACTION_RET_LOGIN_TIMEOUT:
            case YmnSdk.ACTION_RET_ACCOUNTSWITCH_SUCCESS:
                Logger.d("此时游戏应当切换到登录场景");
                break;

            case 123456:
                toast("Token  = " + msg);
                Logger.d("Token = " + msg);
        }
    }

    public void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        Logger.d(msg);
    }


}
