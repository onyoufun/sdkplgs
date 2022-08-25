/*
 * Copyright (C) 2021 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sample.android.billing;

import android.app.Activity;

import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;

import com.linxcool.sdkface.YmnCallback;
import com.linxcool.sdkface.YmnCode;
import com.linxcool.sdkface.feature.YmnDataBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The repository uses data from the Billing data source and the game state model together to give a
 * unified version of the state of the game to the ViewModel. It works closely with the
 * BillingDataSource to implement consumable items, premium items, etc.
 */
public class BillingRepository {

    static final String TAG = "BillingRepository";

    //The following SKU strings must match the ones we have in the Google Play developer console.
    // SKUs for non-subscription purchases
    static final public String SKU_PREMIUM = "premium";
    static final public String SKU_GAS = "gas";
    // SKU for subscription purchases (infinite gas)
    static final public String SKU_INFINITE_GAS_MONTHLY = "infinite_gas_monthly";
    static final public String SKU_INFINITE_GAS_YEARLY = "infinite_gas_yearly";

    public static final String[] INAPP_SKUS = new String[]{ SKU_PREMIUM, SKU_GAS };
    public static final String[] SUBSCRIPTION_SKUS = new String[]{SKU_INFINITE_GAS_MONTHLY, SKU_INFINITE_GAS_YEARLY};
    public static final String[] AUTO_CONSUME_SKUS = new String[]{SKU_GAS};

    final BillingDataSource billingDataSource;
    final SingleMediatorLiveEvent<Integer> allMessages = new SingleMediatorLiveEvent<>();
    final ExecutorService driveExecutor = Executors.newSingleThreadExecutor();

    private YmnCallback ymnCallback;
    private String productId;

    public static String[] formatSkus(String skus){
        String[] array = skus.split("-");
        String[] result = new String[array.length + INAPP_SKUS.length];
        System.arraycopy(array, 0, result, 0, array.length);
        System.arraycopy(INAPP_SKUS, 0, result, array.length, INAPP_SKUS.length);
        return result;
    }

    public BillingRepository(BillingDataSource billingDataSource, YmnCallback ymnCallback) {
        this.billingDataSource = billingDataSource;
        this.ymnCallback = ymnCallback;

        setupMessagesSingleMediatorLiveEvent();

        // Since both are tied to application lifecycle
        billingDataSource.observeConsumedPurchases().observeForever(skuList -> {
            for ( String sku: skuList ) {
                // TODO send result 发道具
                ymnCallback.onCallBack(YmnCode.PAYRESULT_SUCCESS,
                        YmnDataBuilder.createJson(null).
                                append("product_id", productId).
                                append("sku", sku).
                                append("msg", "Pay and consumed purchases success!").
                                toString());
            }
        });
    }

    /**
     * Sets up the event that we can use to send messages up to the UI to be used in Snackbars. This
     * SingleMediatorLiveEvent observes changes in SingleLiveEvents coming from the rest of the game
     * and combines them into a single source with new purchase events from the BillingDataSource.
     * Since the billing data source doesn't know about our SKUs, it also transforms the known SKU
     * strings into useful String messages.
     */
    void setupMessagesSingleMediatorLiveEvent() {
        final LiveData<List<String>> billingMessages = billingDataSource.observeNewPurchases();
        allMessages.addSource(billingMessages, stringList -> {
            // TODO: Handle multi-line purchases better
            for (String s: stringList) {
                switch (s) {
                    case SKU_GAS:
                        // More gas acquired!
                        // TODO send result
                        ymnCallback.onCallBack(YmnCode.PAYRESULT_SUCCESS, "More gas acquired!");
                        break;
                    case SKU_PREMIUM:
                        // You're now a premium driver!
                        // TODO send result
                        ymnCallback.onCallBack(YmnCode.PAYRESULT_SUCCESS, "You're now a premium user!");
                        break;
                    case SKU_INFINITE_GAS_MONTHLY:
                    case SKU_INFINITE_GAS_YEARLY:
                        // this makes sure that upgraded and downgraded subscriptions are
                        // reflected correctly in the app UI
                        billingDataSource.refreshPurchasesAsync();
                        // Thank you for subscribing! You have infinite gas!
                        // TODO send result
                        ymnCallback.onCallBack(YmnCode.PAYRESULT_SUCCESS, "hank you for subscribing! You have infinite gas!");
                        break;
                }
            }
        });
    }

    /**
     * Automatic support for upgrading/downgrading subscription.
     *
     * @param activity Needed by billing library to start the Google Play billing activity
     * @param productId 透传
     * @param sku the product ID to purchase
     */
    public void buySku(Activity activity, String productId, String sku) {
        this.productId = productId;
        String oldSku = null;
        switch (sku) {
            case SKU_INFINITE_GAS_MONTHLY:
                oldSku = SKU_INFINITE_GAS_YEARLY;
                break;
            case SKU_INFINITE_GAS_YEARLY:
                oldSku = SKU_INFINITE_GAS_MONTHLY;
                break;
        }
        if ( null != oldSku ) {
            billingDataSource.launchBillingFlow(activity, sku, oldSku);
        } else {
            billingDataSource.launchBillingFlow(activity, sku);
        }
    }

    /**
     * Return LiveData that indicates whether the sku is currently purchased.
     *
     * @param sku the SKU to get and observe the value for
     * @return LiveData that returns true if the sku is purchased.
     */
    public LiveData<Boolean> isPurchased(String sku) {
        return billingDataSource.isPurchased(sku);
    }

    public final void refreshPurchases() {
        billingDataSource.refreshPurchasesAsync();
    }

    public final LifecycleObserver getBillingLifecycleObserver() {
        return billingDataSource;
    }

    // There's lots of information in SkuDetails, but our app only needs a few things, since our
    // goods never go on sale, have introductory pricing, etc.
    public final LiveData<String> getSkuTitle(String sku) {
        return billingDataSource.getSkuTitle(sku);
    }

    public final LiveData<String> getSkuPrice(String sku) {
        return billingDataSource.getSkuPrice(sku);
    }

    public final LiveData<String> getSkuDescription(String sku) {
        return billingDataSource.getSkuDescription(sku);
    }

    public final LiveData<Integer> getMessages() {
        return allMessages;
    }

    public final LiveData<Boolean> getBillingFlowInProcess() {
        return billingDataSource.getBillingFlowInProcess();
    }

    public final void debugConsumePremium() {
        billingDataSource.consumeInappPurchase(SKU_PREMIUM);
    }
}
