package com.eclectik.wolpepper.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.utils.UtilityMethods;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.snackbar.Snackbar;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;
import java.util.List;

public class UpgradeAppActivity extends AppCompatActivity implements PurchasesUpdatedListener, BillingClientStateListener {

    // Debug tag, for logging
    static final String TAG = "Wolpepper";

    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    public static final String SKU_PREMIUM = "premium";

    private TextView adFreePurchaseButton;

    private Button viewAdButton;

    private RewardedAd mRewardedAd;
    private int adRetryCount = 0;

    // IAB v4
    private BillingClient billingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_upgrade_app_activity));
        setContentView(R.layout.activity_upgrade_app_purchase);
        getSupportActionBar().setElevation(0);

        initializeAllViews();

        setUpRewardedAds();

        setAllViewClickListeners();

        billingClient = BillingClient.newBuilder(this).setListener(this).enablePendingPurchases().build();
        billingClient.startConnection(this);
    }

    private void initializeAllViews() {
        adFreePurchaseButton = findViewById(R.id.ad_free_buy_button);
        viewAdButton = findViewById(R.id.view_ad_button);
        viewAdButton.setEnabled(false);
        viewAdButton.setText("Loading");
        adFreePurchaseButton.setVisibility(View.GONE);
    }

    private String reward = "0";
    private void setUpRewardedAds(){
        RewardedAd.load(this, getString(R.string.rewarded_ad_unit_id), new AdRequest.Builder().build(), new RewardedAdLoadCallback(){
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                super.onAdLoaded(rewardedAd);
                viewAdButton.setText("View AD");
                viewAdButton.setEnabled(true);
                mRewardedAd = rewardedAd;
                adRetryCount = 0;
                mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mRewardedAd = null;
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                if (adRetryCount > 3) {
                    mRewardedAd = null;
                    viewAdButton.setText("Unavailable! Try Later!");
                    viewAdButton.setEnabled(false);
                } else {
                    adRetryCount++;
                    setUpRewardedAds();
                }
            }
        });
    }

    private void setAllViewClickListeners() {
        adFreePurchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseAdFreeAddOn();
            }
        });

        viewAdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UtilityMethods.getAppInstance(UpgradeAppActivity.this).isAppTempPremium()){
                    Toast.makeText(UpgradeAppActivity.this, "App is already premium.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (mRewardedAd != null) {
                    mRewardedAd.show(UpgradeAppActivity.this, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            // Handle the reward.
                            String rewardType = rewardItem.getType();
                            reward = rewardType;
                            Toast.makeText(UpgradeAppActivity.this, "App upgraded to premium for a day.", Toast.LENGTH_LONG).show();
                            onRewarded(rewardItem);
                        }
                    });
                } else {
                    Toast.makeText(UpgradeAppActivity.this, "Failed to load ad! Please try again later!", Toast.LENGTH_LONG).show();
                    setUpRewardedAds();
                }
            }
        });
    }


    private void purchaseAdFreeAddOn() {
        // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(adFreeSku)
                .build();
        int responseCode = billingClient.launchBillingFlow(this, billingFlowParams).getResponseCode();

    }

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        billingClient.endConnection();
        super.onDestroy();
    }

    public void onRewarded(RewardItem reward) {
        // Reward the user.
        if (reward != null) {
            UtilityMethods.grantTempPremium(this);
        }

        showAlert("Yayy!!", "You've upgraded your app to premium for 24 Hrs.\nRestart app for changes to take effect.", true);
    }

    private void showAlert(String title, String message,boolean isSuccess) {
        Alerter.create(this)
                .setTitle(title)
                .setText(message)
                .setTitleTypeface(ResourcesCompat.getFont(this, R.font.spacemono_bold))
                .setTextTypeface(ResourcesCompat.getFont(this, R.font.spacemono_regular))
                .setBackgroundColorInt(ContextCompat.getColor(this, isSuccess ? R.color.alerter_default_success_background : R.color.alert_default_error_background))
                .setTitleAppearance(R.style.Alerter_title_color)
                .setTextAppearance(R.style.alerter_overall_text_with_color)
                .enableInfiniteDuration(true)
                .enableSwipeToDismiss()
                .show();
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
            for (Purchase purchase : list) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            showAlert("Purchase Cancelled!", "Thank you for considering!", false);
        } else {
            // Handle any other error codes.
            Snackbar
                    .make(findViewById(android.R.id.content), "An unexpected error occurred. Please try again later.", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onBillingServiceDisconnected() {

    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            // The BillingClient is ready. You can query purchases here.
            if (UtilityMethods.getAppInstance(this).getAdFreePurchase() != null){
                adFreePurchaseButton.setEnabled(false);
                adFreePurchaseButton.setText(getString(R.string.purchased));
            } else {
                adFreePurchaseButton.setEnabled(true);
                adFreePurchaseButton.setText(getString(R.string.buy));
            }
            getProductList();

        }
    }

    private SkuDetails adFreeSku;
    private void getProductList() {
        List<String> skuList = new ArrayList<>();
        skuList.add(SKU_PREMIUM);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        // Process the result.
                        for (SkuDetails sku : skuDetailsList) {
                            adFreeSku = sku;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adFreePurchaseButton.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
    }

    /**
     * Updates the purchase in app class ionstance too
     * @param purchase
     */
    private void handlePurchase(Purchase purchase) {
        if (UtilityMethods.getAppInstance(this).updateAdFreePurchaseState(purchase)) {

            showAlert("Purchase Complete!", "Thank you for your support and upgrading to an ad-free version of Wolpepper!", true);
            adFreePurchaseButton.setEnabled(false);
            adFreePurchaseButton.setText(getString(R.string.purchased));
        }
    }
}
