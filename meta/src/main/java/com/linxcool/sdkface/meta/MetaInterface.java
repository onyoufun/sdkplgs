package com.linxcool.sdkface.meta;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.GameRequestContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;
import com.linxcool.sdkface.feature.YmnDataBuilder;
import com.linxcool.sdkface.feature.plugin.YmnUserInterface;
import com.linxcool.sdkface.feature.protocol.YFunction;
import com.linxcool.sdkface.feature.protocol.YPlugin;
import com.linxcool.sdkface.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@YPlugin(strategy = YPlugin.Policy.FORCE, entrance = YPlugin.Entrance.ACTIVITY)
public class MetaInterface extends YmnUserInterface {

    @Override
    public String getPluginId() {
        return "102";
    }

    @Override
    public String getPluginName() {
        return "meta";
    }

    @Override
    public int getPluginVersion() {
        return 1;
    }

    @Override
    public String getSdkVersion() {
        return "13.1.0";
    }


    private static final int CODE_GET_FRIENDLIST_SUCCESS = 2001;
    private static final int CODE_GET_FRIENDLIST_FAILED = 2002;

    private static final int CODE_SHARE_SUCCESS = 2111;
    private static final int CODE_SHARE_FAILED = 2112;
    private static final int CODE_SHARE_CANCEL = 2113;

    private static final int CODE_GET_DEEPLINK = 2114;

    private ShareDialog shareDialog;
    private AppEventsLogger eventLogger;

    private static CallbackManager fbCallbackManger;


    @Override
    public void onInit(Context context) {
        super.onInit(context);
        eventLogger = AppEventsLogger.newLogger(getActivity());
        fbCallbackManger = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(fbCallbackManger, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult result) {
                getLogindata(result.getAccessToken());
            }

            @Override
            public void onError(FacebookException error) {
                sendResult(ACTION_RET_LOGIN_FAIL, "onFail");
            }

            @Override
            public void onCancel() {
                sendResult(ACTION_RET_LOGIN_CANCEL, "onCancel");
            }
        });

        shareDialog = new ShareDialog(getActivity());
        shareDialog.registerCallback(fbCallbackManger, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                sendResult(CODE_SHARE_SUCCESS, result.getPostId());
            }

            @Override
            public void onCancel() {
                sendResult(CODE_SHARE_CANCEL, "share cancel");
            }

            @Override
            public void onError(FacebookException error) {
                sendResult(CODE_SHARE_FAILED, "share error | " + error.getMessage());
            }
        });
        setInited(true);
        setIniting(false);
    }


    @Override
    public void login() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null || accessToken.isExpired()) {
            doLogin();
        } else { //自动登录
            getLogindata(accessToken);
        }
    }

    protected void doLogin() {
        LoginManager loginManager = LoginManager.getInstance();
        loginManager.setDefaultAudience(loginManager.getDefaultAudience());
        loginManager.setLoginBehavior(loginManager.getLoginBehavior());
        loginManager.logInWithReadPermissions(getActivity(), Arrays.asList("public_profile", "email", "user_friends"));
    }

    private void getLogindata(final AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, (JSONObject object, GraphResponse response) -> {
            if (object != null) {
                YmnDataBuilder.createJson(MetaInterface.this)
                        .append(LOGIN_SUC_RS_SESSION, accessToken.getToken())
                        .append(LOGIN_SUC_RS_UID, accessToken.getUserId())
                        .append(LOGIN_SUC_RS_UNAME, object.optString("name"))
                        .append("username", object.optString("name"))
                        .append("email", object.optString("email"))
                        .append("gender", object.optString("gender"))
                        .append("picture", object.optJSONObject("picture"))
                        .sendResult(ACTION_RET_LOGIN_SUCCESS);
            } else {
                YmnDataBuilder.createJson(MetaInterface.this)
                        .append(LOGIN_SUC_RS_SESSION, accessToken.getToken())
                        .append(LOGIN_SUC_RS_UID, accessToken.getUserId())
                        .sendResult(ACTION_RET_LOGIN_SUCCESS);
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,gender,birthday,email,picture.type(normal),locale,updated_time,timezone,age_range,first_name,last_name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fbCallbackManger.onActivityResult(requestCode, resultCode, data);
    }

    @YFunction(name = FUNCTION_LOGOUT)
    @Override
    public void logout() {
        super.logout();
        LoginManager.getInstance().logOut();
        sendResult(ACTION_RET_LOGOUT_SUCCESS, "logout success");
    }

    @YFunction(name = "facebook_logevent")
    public void facebook_logevent(String eventName, String eventParams) {
        if (eventName.equals("purchase")) {
            try {
                JSONObject json = new JSONObject(eventParams);
                Double price = json.optDouble("price") / 2;
                eventLogger.logPurchase(new BigDecimal(price), Currency.getInstance("USD"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            if (!TextUtils.isEmpty(eventParams)) {
                Map map = getMap(eventParams);
                Bundle params = new Bundle();

                for (Object key : map.keySet()) {
                    String type = String.valueOf(key);
                    String value = String.valueOf(map.get(type));
                    params.putString(type, value);
                }
                Logger.d("eventName ===  " + eventName + "eventParams  ====   " + eventParams);
                eventLogger.logEvent(eventName, params);
            } else {
                Logger.d("eventName ===  " + eventName);
                eventLogger.logEvent(eventName);
            }
        }
    }

    public static Map<String, Object> getMap(String jsonString) {
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

    @YFunction(name = "facebook_get_friendlist")
    public void facebookGetFriendlist() {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,picture.type(normal)");
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        if (response != null) {
                            sendResult(CODE_GET_FRIENDLIST_SUCCESS, response.getRawResponse());
                        } else {
                            sendResult(CODE_GET_FRIENDLIST_FAILED, "response is null");
                        }
                    }
                }).executeAsync();
    }


    @YFunction(name = "facebook_share")
    public void facebookShare(String shareType, String url, String quote) {
        switch (Integer.valueOf(shareType)) {
            case 1:
                ShareLinkContent linkContent = new ShareLinkContent.Builder().setContentUrl(Uri.parse(url)).build();
                shareDialog.show(linkContent, ShareDialog.Mode.AUTOMATIC);
                break;
            case 2:
                Bitmap image = decodeUriAsBitmap(getActivity(), url);
                SharePhoto photo = new SharePhoto.Builder().setBitmap(image).build();
                SharePhotoContent photoContent = new SharePhotoContent.Builder().addPhoto(photo).build();
                shareDialog.show(photoContent, ShareDialog.Mode.AUTOMATIC);
                break;
            case 3:
                Uri videoFileUri = Uri.parse(url);
                ShareVideo video = new ShareVideo.Builder().setLocalUrl(videoFileUri).build();
                ShareVideoContent shareVideoContent = new ShareVideoContent.Builder().setVideo(video).build();
                shareDialog.show(shareVideoContent, ShareDialog.Mode.AUTOMATIC);
                break;
            case 4:
                ShareLinkContent linkContentWithQuote = new ShareLinkContent.Builder().setContentUrl(Uri.parse(url)).setQuote(quote).build();
                shareDialog.show(linkContentWithQuote, ShareDialog.Mode.AUTOMATIC);
            default:
                Logger.e("shareType is not exist");
                break;
        }
    }

    @YFunction(name = "facebook_request")
    private void facebookRequest(String uid, String msg) {
        try {
            JSONArray array = new JSONArray(uid);
            List<String> uidList = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                uidList.add(array.getString(i));
            }
            GameRequestContent content = new GameRequestContent.Builder()
                    .setMessage(msg)
                    .setActionType(GameRequestContent.ActionType.TURN)
                    .build();
            // requestDialog.show(content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static Bitmap decodeUriAsBitmap(Activity activity, String url) {
        URL fileUrl = null;
        Bitmap bitmap = null;

        if (!url.startsWith("http")) {
            try {
                bitmap = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(Uri.parse(url)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        try {
            fileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            HttpURLConnection conn = (HttpURLConnection) fileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}
