package com.studio08.xbgamestream.Helpers;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.applovin.sdk.AppLovinEventTypes;
import com.google.common.collect.ImmutableList;
import com.studio08.xbgamestream.BuildConfig;
import com.studio08.xbgamestream.Web.ApiClient;
import com.studio08.xbgamestream.Web.StreamWebview;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: /app/base.apk/classes3.dex */
public class PurchaseClient {
    private BillingClient billingClient;
    private Context context;
    EncryptClient encryptClient;
    private StreamWebview mSystemWebview;
    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() { // from class: com.studio08.xbgamestream.Helpers.PurchaseClient.1
        @Override // com.android.billingclient.api.PurchasesUpdatedListener
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> list) {
            Log.d("PurchaseClient", "onPurchasesUpdated: " + billingResult.getResponseCode());
            if (billingResult.getResponseCode() == 0 && list != null) {
                for (Purchase purchase : list) {
                    PurchaseClient.this.handlePurchase(purchase);
                }
                return;
            }
            billingResult.getResponseCode();
        }
    };

    public PurchaseClient(Context context, StreamWebview streamWebview) {
        this.encryptClient = null;
        this.context = context;
        this.encryptClient = new EncryptClient(context);
        this.mSystemWebview = streamWebview;
        initBillingClient();
    }

    private void initBillingClient() {
        BillingClient build = BillingClient.newBuilder(this.context).setListener(this.purchasesUpdatedListener).enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).build();
        this.billingClient = build;
        build.startConnection(new BillingClientStateListener() { // from class: com.studio08.xbgamestream.Helpers.PurchaseClient.2
            @Override // com.android.billingclient.api.BillingClientStateListener
            public void onBillingServiceDisconnected() {
            }

            @Override // com.android.billingclient.api.BillingClientStateListener
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == 0) {
                    PurchaseClient.this.queryPurchases();
                }
            }
        });
    }

    public void queryPurchases() {
        this.billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType("inapp").build(), new PurchasesResponseListener() { // from class: com.studio08.xbgamestream.Helpers.PurchaseClient.3
            @Override // com.android.billingclient.api.PurchasesResponseListener
            public void onQueryPurchasesResponse(BillingResult billingResult, List list) {
                if (billingResult.getResponseCode() != 0 || list == null) {
                    return;
                }
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    PurchaseClient.this.handlePurchase((Purchase) it.next());
                }
            }
        });
        this.billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType("subs").build(), new PurchasesResponseListener() { // from class: com.studio08.xbgamestream.Helpers.PurchaseClient.4
            @Override // com.android.billingclient.api.PurchasesResponseListener
            public void onQueryPurchasesResponse(BillingResult billingResult, List<Purchase> list) {
                if (billingResult.getResponseCode() != 0 || list == null) {
                    return;
                }
                for (Purchase purchase : list) {
                    PurchaseClient.this.handlePurchase(purchase);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePurchase(final Purchase purchase) {
        Log.e("PurchaseClient", "handlePurchase: getPurchaseState=" + purchase.getPurchaseState());
        if (purchase.getPurchaseState() == 1) {
            if (!purchase.isAcknowledged()) {
                this.billingClient.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build(), new AcknowledgePurchaseResponseListener() { // from class: com.studio08.xbgamestream.Helpers.PurchaseClient.5
                    @Override // com.android.billingclient.api.AcknowledgePurchaseResponseListener
                    public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                        PurchaseClient.this.updateToken(purchase);
                    }
                });
                return;
            }
            updateToken(purchase);
        } else if (purchase.getPurchaseState() == 2) {
            Toast.makeText(this.context, "Purchase is Pending...", 0).show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateToken(final Purchase purchase) {
        if (!BuildConfig.FLAVOR.equals(BuildConfig.FLAVOR) && !BuildConfig.FLAVOR.equals("tv")) {
            Log.i("Purchase", "Ignoring sendTokens for non full version");
            return;
        }
        final JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("packageName", this.context.getPackageName());
            jSONObject.put(AppLovinEventTypes.USER_VIEWED_PRODUCT, purchase.getProducts().get(0));
            ((Activity) this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Helpers.PurchaseClient.6
                @Override // java.lang.Runnable
                public void run() {
                    ApiClient.callJavaScript(PurchaseClient.this.mSystemWebview, "setIAPTransactionTokens", purchase.getPurchaseToken(), "android", jSONObject.toString());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setFireTvToken(final String str) {
        if (!BuildConfig.FLAVOR.equals("firetv")) {
            Log.i("Purchase", "Ignoring setFireTvToken for non firetv version");
        } else if (this.mSystemWebview == null) {
            Log.i("Purchase", "Ignoring setFireTvToken webview is empty");
        } else {
            final JSONObject jSONObject = new JSONObject();
            try {
                jSONObject.put("packageName", this.context.getPackageName() + ".firetv");
                jSONObject.put(AppLovinEventTypes.USER_VIEWED_PRODUCT, BuildConfig.FLAVOR);
                ((Activity) this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Helpers.PurchaseClient.7
                    @Override // java.lang.Runnable
                    public void run() {
                        ApiClient.callJavaScript(PurchaseClient.this.mSystemWebview, "setIAPTransactionTokens", str, "firetv", jSONObject.toString());
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void purchasesSubscription(final String str) {
        Log.d("PurchaseClient", "showing purchase view for SUB id: " + str);
        this.billingClient.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(ImmutableList.of(QueryProductDetailsParams.Product.newBuilder().setProductId("xbplay_group").setProductType("subs").build())).build(), new ProductDetailsResponseListener() { // from class: com.studio08.xbgamestream.Helpers.PurchaseClient.8
            @Override // com.android.billingclient.api.ProductDetailsResponseListener
            public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> list) {
                for (ProductDetails productDetails : list) {
                    Log.d("PurchaseClient", "Found a sub product: " + productDetails.getProductId());
                    if (productDetails.getProductId().equals("xbplay_group")) {
                        Log.e("PurchaseClient", "Found valid purchase: " + productDetails.getSubscriptionOfferDetails());
                        if (productDetails.getSubscriptionOfferDetails() == null) {
                            Log.e("PurchaseClient", "Invalid getSubscriptionOfferDetails");
                            return;
                        }
                        ProductDetails.SubscriptionOfferDetails subscriptionOfferDetails = null;
                        for (int i = 0; i < productDetails.getSubscriptionOfferDetails().size(); i++) {
                            ProductDetails.SubscriptionOfferDetails subscriptionOfferDetails2 = productDetails.getSubscriptionOfferDetails().get(i);
                            if (subscriptionOfferDetails2.getBasePlanId().equals(str)) {
                                subscriptionOfferDetails = subscriptionOfferDetails2;
                            } else {
                                Log.e("PurchaseClient", "Ignore plan: " + subscriptionOfferDetails2.getBasePlanId());
                            }
                        }
                        if (subscriptionOfferDetails == null) {
                            Log.e("PurchaseClient", "Invalid offerToUse");
                            return;
                        }
                        BillingResult launchBillingFlow = PurchaseClient.this.billingClient.launchBillingFlow((Activity) PurchaseClient.this.context, BillingFlowParams.newBuilder().setProductDetailsParamsList(ImmutableList.of(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails).setOfferToken(subscriptionOfferDetails.getOfferToken()).build())).build());
                        Log.e("PurchaseClient", "Launch flow result" + launchBillingFlow.getResponseCode() + " " + launchBillingFlow.getDebugMessage());
                    } else {
                        Log.e("PurchaseClient", "Ignore purchase: " + productDetails.getName());
                    }
                }
            }
        });
    }

    public void purchaseProduct(final String str) {
        Log.d("PurchaseClient", "showing purchase view for IAP id: " + str);
        this.billingClient.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(ImmutableList.of(QueryProductDetailsParams.Product.newBuilder().setProductId(str).setProductType("inapp").build())).build(), new ProductDetailsResponseListener() { // from class: com.studio08.xbgamestream.Helpers.PurchaseClient.9
            @Override // com.android.billingclient.api.ProductDetailsResponseListener
            public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> list) {
                for (ProductDetails productDetails : list) {
                    if (productDetails.getProductId().equals(str)) {
                        PurchaseClient.this.billingClient.launchBillingFlow((Activity) PurchaseClient.this.context, BillingFlowParams.newBuilder().setProductDetailsParamsList(ImmutableList.of(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails).build())).build());
                    }
                }
            }
        });
    }
}
