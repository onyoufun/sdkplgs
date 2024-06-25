package com.linxcool.sdkface.googleplay;

import android.content.Context;

import androidx.annotation.NonNull;

import com.linxcool.sdkface.YmnCallback;
import com.linxcool.sdkface.feature.YmnDataBuilder;
import com.linxcool.sdkface.feature.plugin.YmnPaymentInterface;
import com.linxcool.sdkface.feature.protocol.YPlugin;
import com.linxcool.sdkface.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import games.moisoni.google_iab.BillingConnector;
import games.moisoni.google_iab.BillingEventListener;
import games.moisoni.google_iab.enums.ProductType;
import games.moisoni.google_iab.models.BillingResponse;
import games.moisoni.google_iab.models.ProductInfo;
import games.moisoni.google_iab.models.PurchaseInfo;


/**
 * Created by huchanghai on 2018/1/16.
 */
@YPlugin(strategy = YPlugin.Policy.FORCE, entrance = YPlugin.Entrance.ACTIVITY)
public class GooglePlayInterface extends YmnPaymentInterface implements YmnCallback {

    @Override
    public String getPluginId() {
        return "101";
    }

    @Override
    public String getPluginName() {
        return "googleplay";
    }

    @Override
    public int getPluginVersion() {
        return 2;
    }

    @Override
    public String getSdkVersion() {
        return "6.0.1";
    }

    private BillingConnector billingConnector;
    private List<String> consumableIds;
    private List<String> subscriptionIds;
    private String currentOrderId;
    private String currentSku;

    public void formatSkus(String skus, String subSkus) {
        if(skus != null && skus.length() > 0) {
            consumableIds = Arrays.asList(skus.split("-"));
        } else {
            consumableIds = new ArrayList<>();
            Logger.e("google play consumableIds is empty");
        }

        if(subSkus != null && subSkus.length() > 0) {
            subscriptionIds = Arrays.asList(subSkus.split("-"));
        } else {
            subscriptionIds = new ArrayList<>();
            Logger.e("google play subscriptionIds is empty");
        }
    }

    @Override
    public void onInit(final Context context) {
        super.onInit(context);
        String base64EncodedPublicKey = getPropertie("googleplayBase64EncodedPublicKey");
        String skus = getPropertie("googleplayInAppSkus");
        String subSkus = getPropertie("googleplaySubscribeSku");
        this.formatSkus(skus, subSkus);

        this.billingConnector = new BillingConnector(context, base64EncodedPublicKey)
                .setConsumableIds(consumableIds)
                .setSubscriptionIds(subscriptionIds)
                .autoAcknowledge()
                .autoConsume()
                .enableLogging()
                .connect();

        this.billingConnector.setBillingEventListener(new BillingEventListener() {
            @Override
            public void onProductsFetched(@NonNull List<ProductInfo> productDetails) {
                Logger.i("onProductsFetched");
            }

            @Override
            public void onPurchasedProductsFetched(@NonNull ProductType productType, @NonNull List<PurchaseInfo> purchases) {
                Logger.i("onPurchasedProductsFetched");
            }

            @Override
            public void onProductsPurchased(@NonNull List<PurchaseInfo> purchases) {
                Logger.i("onProductsPurchased");
            }

            @Override
            public void onPurchaseAcknowledged(@NonNull PurchaseInfo purchase) {
                Logger.i("onPurchaseAcknowledged");
            }

            @Override
            public void onPurchaseConsumed(@NonNull PurchaseInfo purchase) {
                String sku = purchase.getOrderId();
                sendResult(PAYRESULT_SUCCESS,YmnDataBuilder.createJson(null).
                        append("order_id", currentOrderId).
                        append("sku", sku).
                        append("token", purchase.getPurchaseToken()).
                        append("google_order_id", purchase.getOrderId()).
                        append("msg", "Pay and consumed purchases success!").
                        toString());
            }

            @Override
            public void onBillingError(@NonNull BillingConnector billingConnector, @NonNull BillingResponse response) {
                Logger.i("onBillingError " + response.getDebugMessage());

                switch (response.getErrorType()) {
                    case BILLING_ERROR:
                        break;
                    case USER_CANCELED:
                        sendResult(PAYRESULT_CANCEL, "user pressed back or canceled a dialog");
                        break;
                    case ITEM_ALREADY_OWNED:
                        sendResult(30016, currentSku);
                        break;
                    default:
                        sendResult(PAYRESULT_FAIL, "errorType = " + response.getErrorType());
                        break;
                }
            }
        });
    }

    @Override
    public void onCallBack(int code, String msg) {
        sendResult(code, msg);
    }

    @Override
    public void pay(final Map<String, String> map) {
        this.currentSku = map.get(ARG_PRODUCT_ID);
        this.currentOrderId = map.get(ARG_CP_ORDER_ID);
        if(consumableIds.contains(this.currentSku)) billingConnector.purchase(getActivity(), this.currentSku);
        else billingConnector.subscribe(getActivity(), this.currentSku);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (billingConnector != null) {
            billingConnector.release();
        }
    }

}
