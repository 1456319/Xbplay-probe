package com.studio08.xbgamestream.Helpers;

import android.content.Context;
import android.util.Log;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
/* loaded from: /app/base.apk/classes3.dex */
public class IAPPricesManager implements PurchasesUpdatedListener {
    private static final String TAG = "IAPPricesManager";
    private BillingClient billingClient;
    private final EncryptClient encryptClient;
    private final Map<String, String> productPrices = new HashMap();
    private final String iapProductId = "remove_ads";
    private final String subscriptionProductId = "xbplay_group";
    private boolean iapFetchComplete = false;
    private boolean subsFetchComplete = false;

    @Override // com.android.billingclient.api.PurchasesUpdatedListener
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> list) {
    }

    public IAPPricesManager(Context context) {
        this.encryptClient = new EncryptClient(context);
        this.billingClient = BillingClient.newBuilder(context).setListener(this).enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).build();
        startConnection();
    }

    private void startConnection() {
        Log.d(TAG, "startConnection");
        this.billingClient.startConnection(new BillingClientStateListener() { // from class: com.studio08.xbgamestream.Helpers.IAPPricesManager.1
            @Override // com.android.billingclient.api.BillingClientStateListener
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == 0) {
                    Log.d(IAPPricesManager.TAG, "onBillingSetupFinished");
                    IAPPricesManager.this.fetchSubscriptions();
                    IAPPricesManager.this.fetchIAPs();
                    return;
                }
                Log.e(IAPPricesManager.TAG, "Billing setup failed: " + billingResult.getDebugMessage());
            }

            @Override // com.android.billingclient.api.BillingClientStateListener
            public void onBillingServiceDisconnected() {
                Log.e(IAPPricesManager.TAG, "Billing service disconnected");
            }
        });
    }

    public void fetchSubscriptions() {
        Log.d(TAG, "fetchSubscriptions");
        this.billingClient.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(ImmutableList.of(QueryProductDetailsParams.Product.newBuilder().setProductId("xbplay_group").setProductType("subs").build())).build(), new ProductDetailsResponseListener() { // from class: com.studio08.xbgamestream.Helpers.IAPPricesManager.2
            @Override // com.android.billingclient.api.ProductDetailsResponseListener
            public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> list) {
                if (billingResult.getResponseCode() != 0 || list == null) {
                    Log.e(IAPPricesManager.TAG, "Failed to fetch subscriptions: " + billingResult.getDebugMessage());
                    IAPPricesManager.this.subsFetchComplete = true;
                    IAPPricesManager.this.checkAndSavePrices();
                    return;
                }
                for (ProductDetails productDetails : list) {
                    if (productDetails.getProductId().equals("xbplay_group")) {
                        Log.d(IAPPricesManager.TAG, "Found valid subscription: " + productDetails.getSubscriptionOfferDetails());
                        if (productDetails.getSubscriptionOfferDetails() == null) {
                            Log.e(IAPPricesManager.TAG, "Invalid getSubscriptionOfferDetails");
                            IAPPricesManager.this.subsFetchComplete = true;
                            IAPPricesManager.this.checkAndSavePrices();
                            return;
                        }
                        for (ProductDetails.SubscriptionOfferDetails subscriptionOfferDetails : productDetails.getSubscriptionOfferDetails()) {
                            ProductDetails.PricingPhase pricingPhase = subscriptionOfferDetails.getPricingPhases().getPricingPhaseList().get(0);
                            IAPPricesManager.this.productPrices.put(subscriptionOfferDetails.getBasePlanId(), pricingPhase.getFormattedPrice() + (pricingPhase.getBillingPeriod().equals("P1M") ? " / month" : " / year") + (!pricingPhase.getPriceCurrencyCode().isEmpty() ? " (" + pricingPhase.getPriceCurrencyCode() + ")" : ""));
                        }
                    } else {
                        Log.e(IAPPricesManager.TAG, "Ignore product: " + productDetails.getName());
                    }
                }
                IAPPricesManager.this.subsFetchComplete = true;
                IAPPricesManager.this.checkAndSavePrices();
            }
        });
    }

    public void fetchIAPs() {
        Log.d(TAG, "fetchRemoveAdsIAPs");
        this.billingClient.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(ImmutableList.of(QueryProductDetailsParams.Product.newBuilder().setProductId("remove_ads").setProductType("inapp").build())).build(), new ProductDetailsResponseListener() { // from class: com.studio08.xbgamestream.Helpers.IAPPricesManager.3
            @Override // com.android.billingclient.api.ProductDetailsResponseListener
            public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> list) {
                if (billingResult.getResponseCode() != 0 || list == null) {
                    Log.e(IAPPricesManager.TAG, "Failed to fetch IAPs: " + billingResult.getDebugMessage());
                    IAPPricesManager.this.iapFetchComplete = true;
                    IAPPricesManager.this.checkAndSavePrices();
                    return;
                }
                for (ProductDetails productDetails : list) {
                    if (productDetails.getProductId().equals("remove_ads")) {
                        Log.d(IAPPricesManager.TAG, "Found valid IAP: " + productDetails.getProductId());
                        ProductDetails.OneTimePurchaseOfferDetails oneTimePurchaseOfferDetails = productDetails.getOneTimePurchaseOfferDetails();
                        if (oneTimePurchaseOfferDetails != null) {
                            String formattedPrice = oneTimePurchaseOfferDetails.getFormattedPrice();
                            String priceCurrencyCode = oneTimePurchaseOfferDetails.getPriceCurrencyCode();
                            IAPPricesManager.this.productPrices.put(productDetails.getProductId(), formattedPrice + " once" + (!priceCurrencyCode.isEmpty() ? " (" + priceCurrencyCode + ")" : ""));
                        } else {
                            Log.e(IAPPricesManager.TAG, "No price details available for IAP: " + productDetails.getProductId());
                        }
                    } else {
                        Log.e(IAPPricesManager.TAG, "Ignore product: " + productDetails.getProductId());
                    }
                }
                IAPPricesManager.this.iapFetchComplete = true;
                IAPPricesManager.this.checkAndSavePrices();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkAndSavePrices() {
        if (this.iapFetchComplete && this.subsFetchComplete) {
            Log.d(TAG, "All products fetched. Saving to EncryptClient.");
            this.encryptClient.saveJSONObject("productPriceData", new JSONObject(this.productPrices));
            Log.d(TAG, "Saved productPrices: " + new JSONObject(this.productPrices));
        }
    }
}
