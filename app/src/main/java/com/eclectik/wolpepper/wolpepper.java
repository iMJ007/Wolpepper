package com.eclectik.wolpepper;

import static com.android.billingclient.api.BillingClient.BillingResponseCode.OK;
import static com.eclectik.wolpepper.utils.ConstantValues.SKU_PREMIUM;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDex;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.eclectik.wolpepper.utils.NotificationUtils;
import com.eclectik.wolpepper.utils.UtilityMethods;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mj on 14/6/17.
 */

public class wolpepper extends Application implements AcknowledgePurchaseResponseListener, PurchasesUpdatedListener, BillingClientStateListener {

    private String APP_ID = "";

//    private String NapiAuthKey = "";

    private String APP_SECRET = "";

    private int likeUnlikeCount = 0;

    private Map<String, Boolean> ID_LIKE_STATUS_MAP = new HashMap<>();

    private boolean isPortraitOnly = false;

    private BillingClient billingClient;
    private Purchase adFreePurchase = null;
    private boolean isBillingClientFinishedLoading = false;

    @Override
    public void onCreate() {
        super.onCreate();
//        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectNonSdkApiUsage().build());
        NotificationUtils.createNotificationChannel(this);
        MobileAds.initialize(getApplicationContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                MobileAds.setRequestConfiguration(new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("BF03F0E38AA9180F01DD8F9456A9FE6D")).build());
                MobileAds.setAppVolume(0);
                MobileAds.setAppMuted(true);
            }
        });
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(this);
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

    }

    public String getAPP_ID() {
        return APP_ID;
    }

    public void setAPP_ID(String APP_ID) {
        this.APP_ID = APP_ID;
    }

    public String getAPP_SECRET() {
        return APP_SECRET;
    }

    public void setAPP_SECRET(String APP_SECRET) {
        this.APP_SECRET = APP_SECRET;
    }

    public int getLikeUnlikeCount() {
        return likeUnlikeCount;
    }

    public void setLikeUnlikeCount(int likeUnlikeCount) {
        this.likeUnlikeCount = likeUnlikeCount;
    }

    public Purchase getAdFreePurchase() {
        if (!isBillingClientFinishedLoading) {
            return null;
        }
        UtilityMethods.updateFullWidgetUnlocked(this, adFreePurchase != null);
        return adFreePurchase;
    }

    public Map<String, Boolean> getID_LIKE_STATUS_MAP() {
        return ID_LIKE_STATUS_MAP;
    }

    public void setID_LIKE_STATUS_MAP(Map<String, Boolean> ID_LIKE_STATUS_MAP) {
        this.ID_LIKE_STATUS_MAP = ID_LIKE_STATUS_MAP;
    }

    public void clearID_LIKE_STATUS_MAP() {
        this.ID_LIKE_STATUS_MAP.clear();
    }

    public void updateIdLikeStatusMap(String imageId, Boolean isLiked) {
        if (isLiked == null) {
            isLiked = false;
        }
        if (this.ID_LIKE_STATUS_MAP.containsKey(imageId)) {
            this.ID_LIKE_STATUS_MAP.remove(imageId);
        }
        this.ID_LIKE_STATUS_MAP.put(imageId, isLiked);
    }

    public boolean isImageLiked(String imageId) {
        if (imageId == null || this.getID_LIKE_STATUS_MAP() == null) {
            return false;
        }
        if (!ID_LIKE_STATUS_MAP.containsKey(imageId)) {
            return false;
        }
        return this.ID_LIKE_STATUS_MAP.get(imageId);
    }

    public boolean isAppTempPremium() {
        if (getAdFreePurchase() == null) {
            UtilityMethods.updateFullWidgetUnlocked(this, System.currentTimeMillis() < UtilityMethods.getTempPremium(this));
            return System.currentTimeMillis() < UtilityMethods.getTempPremium(this);
        }
        return false;
    }

    public boolean isPortraitOnly() {
        return isPortraitOnly;
    }

    public void setPortraitOnly(boolean portraitOnly) {
        isPortraitOnly = portraitOnly;
    }

    public boolean updateAdFreePurchaseState(Purchase purchase) {
        isBillingClientFinishedLoading = true;
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
            AcknowledgePurchaseParams acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();
            billingClient.acknowledgePurchase(acknowledgePurchaseParams, this);
        }

        if (purchase.getSkus().get(purchase.getSkus().indexOf(SKU_PREMIUM)).equals(SKU_PREMIUM) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            adFreePurchase = purchase;
            return !purchase.getOrderId().isEmpty();
        }

        return purchase.getOrderId().isEmpty();
    }

    @Override
    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {

    }

    @Override
    public void onBillingServiceDisconnected() {

    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        if (billingResult.getResponseCode() == OK) {
            billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, new PurchasesResponseListener() {
                @Override
                public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                    if (list == null) return;

                    for (Purchase purchase : list) {
                        // Acknowledge the purchase if it hasn't already been acknowledged.
                        updateAdFreePurchaseState(purchase);
                    }
                }
            });
        }
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {

    }
}

