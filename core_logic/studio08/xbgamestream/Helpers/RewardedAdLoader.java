package com.studio08.xbgamestream.Helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import com.connectsdk.discovery.DiscoveryProvider;
import com.google.android.exoplayer2.source.rtsp.SessionDescription;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.google.common.collect.ImmutableList;
import com.google.firebase.sessions.settings.RemoteSettings;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.integration.IntegrationHelper;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;
import com.pollfish.Pollfish;
import com.pollfish.builder.Params;
import com.pollfish.builder.Position;
import com.pollfish.callback.PollfishClosedListener;
import com.pollfish.callback.PollfishOpenedListener;
import com.pollfish.callback.PollfishSurveyCompletedListener;
import com.pollfish.callback.PollfishSurveyNotAvailableListener;
import com.pollfish.callback.PollfishSurveyReceivedListener;
import com.pollfish.callback.PollfishUserNotEligibleListener;
import com.pollfish.callback.SurveyInfo;
import com.studio08.xbgamestream.BuildConfig;
import com.studio08.xbgamestream.Web.ApiClient;
import java.util.Iterator;
import java.util.List;
/* loaded from: /app/base.apk/classes3.dex */
public class RewardedAdLoader implements PollfishSurveyReceivedListener, PollfishSurveyCompletedListener, PollfishUserNotEligibleListener, PollfishSurveyNotAvailableListener, PollfishClosedListener, PollfishOpenedListener {
    private static int AD_TIMESTAMP_EXTENSION_PERIOD = 900000;
    private static int FAILED_AD_TIMESTAMP_EXTENSION_PERIOD = 900000;
    public static int GET_TOKEN_CACHE_DURATION = 86400000;
    public static int TV_AD_TIMESTAMP_EXTENSION_PERIOD = 540000;
    private BillingClient billingClient;
    private Context context;
    EncryptClient encryptClient;
    private RewardedInterstitialAd rewardedInterstitialAdAdmob;
    private RewardedAd rewardedVideoAd;
    private int MAX_ALLOWED_TRAIL_EXTENSIONS = 3;
    private RewardAdListener listener = null;
    private int attemptedAdShowCount = 0;
    boolean adIsClosed = false;
    boolean adRewardGranted = false;
    boolean isSurveyAvailable = false;
    boolean surveyIsClosed = false;
    boolean surveyRewardGranted = false;
    int surveyUpdateTimestamp = 0;
    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() { // from class: com.studio08.xbgamestream.Helpers.RewardedAdLoader.1
        @Override // com.android.billingclient.api.PurchasesUpdatedListener
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> list) {
            if (billingResult.getResponseCode() == 0 && list != null) {
                for (Purchase purchase : list) {
                    RewardedAdLoader.this.handlePurchase(purchase);
                }
                return;
            }
            billingResult.getResponseCode();
        }
    };

    /* loaded from: /app/base.apk/classes3.dex */
    public interface RewardAdListener {
        void onRewardComplete();
    }

    static /* synthetic */ int access$308(RewardedAdLoader rewardedAdLoader) {
        int i = rewardedAdLoader.attemptedAdShowCount;
        rewardedAdLoader.attemptedAdShowCount = i + 1;
        return i;
    }

    @Override // com.pollfish.callback.PollfishSurveyReceivedListener
    public void onPollfishSurveyReceived(SurveyInfo surveyInfo) {
        Log.e("Survey", "onPollfishSurveyReceived" + surveyInfo);
        if (surveyInfo == null || surveyInfo.getRewardValue() == null || surveyInfo.getRewardValue().intValue() < 30) {
            return;
        }
        this.isSurveyAvailable = true;
    }

    @Override // com.pollfish.callback.PollfishSurveyCompletedListener
    public void onPollfishSurveyCompleted(SurveyInfo surveyInfo) {
        Log.e("Survey", "onPollfishSurveyCompleted" + surveyInfo);
        if (surveyInfo.getRewardValue() != null) {
            Toast.makeText(this.context, "Survey Completed! " + surveyInfo.getRewardValue() + " minutes added.", 0).show();
            this.surveyRewardGranted = true;
            this.surveyUpdateTimestamp = surveyInfo.getRewardValue().intValue() * DiscoveryProvider.TIMEOUT;
            grantSurveyRewardIfClosedAndCompleted();
        }
    }

    @Override // com.pollfish.callback.PollfishUserNotEligibleListener
    public void onUserNotEligible() {
        this.surveyRewardGranted = false;
        Log.e("Survey", "onUserNotEligible");
        new AlertDialog.Builder(this.context).setTitle("User Not Eligible / Screened").setMessage("The survey provider indicated that the user isn't eligible to complete the survey, aborted.").setCancelable(true).setPositiveButton("OK", (DialogInterface.OnClickListener) null).show();
    }

    @Override // com.pollfish.callback.PollfishSurveyNotAvailableListener
    public void onPollfishSurveyNotAvailable() {
        Log.e("Survey", "onPollfishSurveyNotAvailable");
        this.isSurveyAvailable = false;
    }

    @Override // com.pollfish.callback.PollfishClosedListener
    public void onPollfishClosed() {
        Log.e("Survey", "onPollfishClosed");
        this.surveyIsClosed = true;
        grantSurveyRewardIfClosedAndCompleted();
    }

    @Override // com.pollfish.callback.PollfishOpenedListener
    public void onPollfishOpened() {
        Log.e("Survey", "onPollfishOpened");
        this.surveyIsClosed = false;
    }

    public RewardedAdLoader(Context context) {
        this.encryptClient = null;
        this.context = context;
        initAd();
        initBillingClient();
        initSurvey();
        this.encryptClient = new EncryptClient(context);
    }

    public void resume() {
        if (Pollfish.isPollfishPresent()) {
            return;
        }
        initSurvey();
    }

    private void initSurvey() {
        this.isSurveyAvailable = false;
        Pollfish.initWith((Activity) this.context, new Params.Builder(BuildConfig.POLLFISH_APIKEY).pollfishClosedListener(this).pollfishSurveyCompletedListener(this).pollfishSurveyNotAvailableListener(this).pollfishSurveyReceivedListener(this).pollfishUserNotEligibleListener(this).rewardMode(true).indicatorPosition(Position.MIDDLE_RIGHT).releaseMode(true ^ "release".equals("debug")).build());
    }

    public void setCallbackListener(RewardAdListener rewardAdListener) {
        this.listener = rewardAdListener;
    }

    /* JADX WARN: Code restructure failed: missing block: B:15:0x0051, code lost:
        if (r2 >= r5.MAX_ALLOWED_TRAIL_EXTENSIONS) goto L16;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void showRewardedAdDialog() {
        /*
            r5 = this;
            java.lang.String r0 = "Starting show ad attempt!"
            java.lang.String r1 = "HERE"
            android.util.Log.e(r1, r0)
            android.content.Context r0 = r5.context
            boolean r0 = shouldShowAd(r0)
            if (r0 != 0) goto L1c
            java.lang.String r0 = "Not showing ad due to policy"
            android.util.Log.e(r1, r0)
            com.studio08.xbgamestream.Helpers.RewardedAdLoader$RewardAdListener r0 = r5.listener
            if (r0 == 0) goto L1b
            r0.onRewardComplete()
        L1b:
            return
        L1c:
            java.lang.String r0 = "Showing ad dialog box"
            android.util.Log.e(r1, r0)
            java.lang.String r0 = "full"
            java.lang.String r1 = "mediaRemote"
            boolean r0 = r0.equals(r1)
            r1 = 1
            if (r0 == 0) goto L54
            r0 = 0
            com.studio08.xbgamestream.Helpers.EncryptClient r2 = r5.encryptClient     // Catch: java.lang.Exception -> L3a
            java.lang.String r3 = "free_trial_extensions"
            java.lang.String r2 = r2.getValue(r3)     // Catch: java.lang.Exception -> L3a
            int r2 = java.lang.Integer.parseInt(r2)     // Catch: java.lang.Exception -> L3a
            goto L3b
        L3a:
            r2 = r0
        L3b:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            java.lang.String r4 = "currentExtensions = "
            r3.<init>(r4)
            java.lang.StringBuilder r3 = r3.append(r2)
            java.lang.String r3 = r3.toString()
            java.lang.String r4 = "currentExtensions"
            android.util.Log.e(r4, r3)
            int r3 = r5.MAX_ALLOWED_TRAIL_EXTENSIONS
            if (r2 < r3) goto L54
            goto L55
        L54:
            r0 = r1
        L55:
            android.app.AlertDialog$Builder r2 = new android.app.AlertDialog$Builder
            android.content.Context r3 = r5.context
            r2.<init>(r3)
            java.lang.String r3 = "Free Trial Ended"
            android.app.AlertDialog$Builder r2 = r2.setTitle(r3)
            android.app.AlertDialog$Builder r1 = r2.setCancelable(r1)
            com.studio08.xbgamestream.Helpers.RewardedAdLoader$3 r2 = new com.studio08.xbgamestream.Helpers.RewardedAdLoader$3
            r2.<init>()
            java.lang.String r3 = "Purchase"
            android.app.AlertDialog$Builder r1 = r1.setPositiveButton(r3, r2)
            com.studio08.xbgamestream.Helpers.RewardedAdLoader$2 r2 = new com.studio08.xbgamestream.Helpers.RewardedAdLoader$2
            r2.<init>()
            java.lang.String r3 = "Exit App"
            android.app.AlertDialog$Builder r1 = r1.setNegativeButton(r3, r2)
            if (r0 == 0) goto L8e
            java.lang.String r0 = "Thanks for trying out the app! If it worked for you, please purchase the full version. Still not sure? You can extend the trial time by watching an ad."
            r1.setMessage(r0)
            com.studio08.xbgamestream.Helpers.RewardedAdLoader$4 r0 = new com.studio08.xbgamestream.Helpers.RewardedAdLoader$4
            r0.<init>()
            java.lang.String r2 = "Extend Trial"
            r1.setNeutralButton(r2, r0)
            goto L93
        L8e:
            java.lang.String r0 = "Thanks for trying out the app! If it worked for you, please purchase the full version to continue use."
            r1.setMessage(r0)
        L93:
            r1.show()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.studio08.xbgamestream.Helpers.RewardedAdLoader.showRewardedAdDialog():void");
    }

    public static boolean shouldShowAd(Context context) {
        if ("release".equals("debug")) {
            Log.e("HERE", "Not showing ad on debug build");
            return false;
        } else if (BuildConfig.FLAVOR.equals("firetv")) {
            Log.e("HERE", "Not showing ad on fire tv build");
            return false;
        } else if (!isAdTimestampExpired(context)) {
            Log.e("HERE", "Not showing ad due to already showed");
            return false;
        } else if (getPurchaseItem(context)) {
            Log.e("HERE", "Not showing ad due to user purchase");
            return false;
        } else {
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showAnyAvailableAd() {
        if (IronSource.isRewardedVideoAvailable()) {
            Log.e("HERE", "Attempting to show ironsource ad");
            this.adRewardGranted = false;
            this.adIsClosed = false;
            IronSource.showRewardedVideo();
            return;
        }
        Log.e("HERE", "CANT show ironsource ad");
        updateAdTimestamp(FAILED_AD_TIMESTAMP_EXTENSION_PERIOD, this.context);
        RewardAdListener rewardAdListener = this.listener;
        if (rewardAdListener != null) {
            rewardAdListener.onRewardComplete();
            Toast.makeText(this.context, "Can't load ad. Giving you 15 minutes of free use anyway :)", 0).show();
        }
    }

    private void initAd() {
        IronSource.setRewardedVideoListener(new RewardedVideoListener() { // from class: com.studio08.xbgamestream.Helpers.RewardedAdLoader.5
            @Override // com.ironsource.mediationsdk.sdk.RewardedVideoListener
            public void onRewardedVideoAdOpened() {
                Log.e("HERE", "onRewardedVideoAdOpened");
            }

            @Override // com.ironsource.mediationsdk.sdk.RewardedVideoListener
            public void onRewardedVideoAdClosed() {
                Log.e("HERE", "onRewardedVideoAdClosed");
                if (RewardedAdLoader.this.attemptedAdShowCount > 3) {
                    RewardedAdLoader.this.attemptedAdShowCount = 0;
                    RewardedAdLoader.this.listener.onRewardComplete();
                    return;
                }
                RewardedAdLoader.this.adIsClosed = true;
                RewardedAdLoader.this.grantRewardIfClosedAndCompleted();
            }

            @Override // com.ironsource.mediationsdk.sdk.RewardedVideoListener
            public void onRewardedVideoAvailabilityChanged(boolean z) {
                Log.e("HERE", "onRewardedVideoAvailabilityChanged");
            }

            @Override // com.ironsource.mediationsdk.sdk.RewardedVideoListener
            public void onRewardedVideoAdStarted() {
                Log.e("HERE", "onRewardedVideoAdStarted");
                RewardedAdLoader.access$308(RewardedAdLoader.this);
            }

            @Override // com.ironsource.mediationsdk.sdk.RewardedVideoListener
            public void onRewardedVideoAdEnded() {
                Log.e("HERE", "onRewardedVideoAdEnded");
            }

            @Override // com.ironsource.mediationsdk.sdk.RewardedVideoListener
            public void onRewardedVideoAdRewarded(Placement placement) {
                RewardedAdLoader.this.adRewardGranted = true;
                RewardedAdLoader.this.grantRewardIfClosedAndCompleted();
            }

            @Override // com.ironsource.mediationsdk.sdk.RewardedVideoListener
            public void onRewardedVideoAdShowFailed(IronSourceError ironSourceError) {
                if (RewardedAdLoader.this.listener != null) {
                    RewardedAdLoader.this.listener.onRewardComplete();
                }
                Log.e("HERE", "onRewardedVideoAdShowFailed");
            }

            @Override // com.ironsource.mediationsdk.sdk.RewardedVideoListener
            public void onRewardedVideoAdClicked(Placement placement) {
                Log.e("HERE", "onRewardedVideoAdClicked");
            }
        });
        IronSource.init((Activity) this.context, BuildConfig.IRONSOURCE_APP_KEY, IronSource.AD_UNIT.REWARDED_VIDEO);
        IronSource.setConsent(true);
        if ("release".equals("debug")) {
            IntegrationHelper.validateIntegration((Activity) this.context);
        }
    }

    private void grantSurveyRewardIfClosedAndCompleted() {
        int i;
        if (this.surveyIsClosed && this.surveyRewardGranted && (i = this.surveyUpdateTimestamp) != 0) {
            updateAdTimestamp(i, this.context);
            RewardAdListener rewardAdListener = this.listener;
            if (rewardAdListener != null) {
                rewardAdListener.onRewardComplete();
            }
            Log.e("HERE", "grantSurveyRewardIfClosedAndCompleted");
            this.attemptedAdShowCount = 0;
            this.surveyUpdateTimestamp = 0;
            this.adIsClosed = false;
            this.adRewardGranted = false;
            this.surveyRewardGranted = false;
            this.surveyIsClosed = false;
            return;
        }
        Log.e("HERE", "Not granting reward. ClosedSur:" + this.surveyIsClosed + " GrantedSur:" + this.surveyRewardGranted + "surveyUpdateTimestamp: " + this.surveyUpdateTimestamp);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void grantRewardIfClosedAndCompleted() {
        if (this.adIsClosed && this.adRewardGranted) {
            updateAdTimestamp(AD_TIMESTAMP_EXTENSION_PERIOD, this.context);
            RewardAdListener rewardAdListener = this.listener;
            if (rewardAdListener != null) {
                rewardAdListener.onRewardComplete();
            }
            Log.e("HERE", "onRewardedVideoAdRewarded");
            this.attemptedAdShowCount = 0;
            this.adIsClosed = false;
            this.adRewardGranted = false;
            this.surveyRewardGranted = false;
            this.surveyIsClosed = false;
            return;
        }
        Log.e("HERE", "Not granting reward. Closed:" + this.adIsClosed + " Granted:" + this.adRewardGranted);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void loadRewardedVideoAd() {
        RewardedAd.load(this.context, "release".equals("debug") ? "ca-app-pub-5718214549980942/6015423364" : BuildConfig.ADMOB_UNIT_ID_REWARDED_INTERSTITIAL_1, new AdRequest.Builder().build(), new RewardedAdLoadCallback() { // from class: com.studio08.xbgamestream.Helpers.RewardedAdLoader.6
            @Override // com.google.android.gms.ads.AdLoadCallback
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                Log.d("HERE", loadAdError.toString());
                RewardedAdLoader.this.rewardedVideoAd = null;
            }

            @Override // com.google.android.gms.ads.AdLoadCallback
            public void onAdLoaded(RewardedAd rewardedAd) {
                RewardedAdLoader.this.rewardedVideoAd = rewardedAd;
                Log.d("HERE", "Ad was loaded.");
                RewardedAdLoader.this.rewardedVideoAd.setFullScreenContentCallback(new FullScreenContentCallback() { // from class: com.studio08.xbgamestream.Helpers.RewardedAdLoader.6.1
                    @Override // com.google.android.gms.ads.FullScreenContentCallback
                    public void onAdClicked() {
                        Log.d("HERE", "Ad was clicked.");
                    }

                    @Override // com.google.android.gms.ads.FullScreenContentCallback
                    public void onAdDismissedFullScreenContent() {
                        Log.d("HERE", "Ad dismissed fullscreen content.");
                        RewardedAdLoader.this.rewardedVideoAd = null;
                        RewardedAdLoader.this.loadRewardedVideoAd();
                    }

                    @Override // com.google.android.gms.ads.FullScreenContentCallback
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        Log.e("HERE", "Ad failed to show fullscreen content.");
                        RewardedAdLoader.this.rewardedVideoAd = null;
                    }

                    @Override // com.google.android.gms.ads.FullScreenContentCallback
                    public void onAdImpression() {
                        Log.d("HERE", "Ad recorded an impression.");
                    }

                    @Override // com.google.android.gms.ads.FullScreenContentCallback
                    public void onAdShowedFullScreenContent() {
                        Log.d("HERE", "Ad showed fullscreen content.");
                    }
                });
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void loadRewardedInterstitial() {
        RewardedInterstitialAd.load(this.context, "release".equals("debug") ? "ca-app-pub-3940256099942544/5354046379" : BuildConfig.ADMOB_UNIT_ID_REWARDED_INTERSTITIAL_1, new AdRequest.Builder().build(), new RewardedInterstitialAdLoadCallback() { // from class: com.studio08.xbgamestream.Helpers.RewardedAdLoader.7
            @Override // com.google.android.gms.ads.AdLoadCallback
            public void onAdLoaded(RewardedInterstitialAd rewardedInterstitialAd) {
                Log.d("HERE", "Ad was loaded.");
                RewardedAdLoader.this.rewardedInterstitialAdAdmob = rewardedInterstitialAd;
                RewardedAdLoader.this.rewardedInterstitialAdAdmob.setFullScreenContentCallback(new FullScreenContentCallback() { // from class: com.studio08.xbgamestream.Helpers.RewardedAdLoader.7.1
                    @Override // com.google.android.gms.ads.FullScreenContentCallback
                    public void onAdClicked() {
                        Log.e("HERE", "Ad was clicked.");
                    }

                    @Override // com.google.android.gms.ads.FullScreenContentCallback
                    public void onAdDismissedFullScreenContent() {
                        Log.e("HERE", "Ad dismissed fullscreen content.");
                        RewardedAdLoader.this.rewardedInterstitialAdAdmob = null;
                        RewardedAdLoader.this.loadRewardedInterstitial();
                    }

                    @Override // com.google.android.gms.ads.FullScreenContentCallback
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        Log.e("HERE", "Ad failed to show fullscreen content.");
                        RewardedAdLoader.this.rewardedInterstitialAdAdmob = null;
                    }

                    @Override // com.google.android.gms.ads.FullScreenContentCallback
                    public void onAdImpression() {
                        Log.e("HERE", "Ad recorded an impression.");
                    }

                    @Override // com.google.android.gms.ads.FullScreenContentCallback
                    public void onAdShowedFullScreenContent() {
                        Log.e("HERE", "Ad showed fullscreen content.");
                    }
                });
            }

            @Override // com.google.android.gms.ads.AdLoadCallback
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                Log.e("HERE", loadAdError.toString());
                RewardedAdLoader.this.rewardedInterstitialAdAdmob = null;
            }
        });
    }

    public static void updateAdTimestamp(int i, Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences("SettingsSharedPref", 0).edit();
        edit.putLong("nextAdShowtime", System.currentTimeMillis() + i);
        edit.apply();
    }

    private static boolean isAdTimestampExpired(Context context) {
        long j = context.getSharedPreferences("SettingsSharedPref", 0).getLong("nextAdShowtime", 0L);
        long currentTimeMillis = System.currentTimeMillis();
        Log.e("HERE", "Ad Timestamp: " + j + " Current Timestamp: " + currentTimeMillis);
        if (j != 0) {
            if (j < currentTimeMillis) {
                Log.e("HERE", "Ad timestamp expired. Should show new ad");
                return true;
            }
            Log.e("HERE", "Ad timestamp still in future. Don't show new ad. Min Remaining: " + ((j - currentTimeMillis) / 60000));
            return false;
        }
        Log.e("HERE", "Ad Timestamp not expired due to first usage.");
        if (BuildConfig.FLAVOR.equals("tv") || BuildConfig.FLAVOR.equals("vr")) {
            updateAdTimestamp(TV_AD_TIMESTAMP_EXTENSION_PERIOD, context);
        } else {
            updateAdTimestamp(AD_TIMESTAMP_EXTENSION_PERIOD, context);
        }
        return false;
    }

    private void initBillingClient() {
        BillingClient build = BillingClient.newBuilder(this.context).setListener(this.purchasesUpdatedListener).enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).build();
        this.billingClient = build;
        build.startConnection(new BillingClientStateListener() { // from class: com.studio08.xbgamestream.Helpers.RewardedAdLoader.8
            @Override // com.android.billingclient.api.BillingClientStateListener
            public void onBillingServiceDisconnected() {
            }

            @Override // com.android.billingclient.api.BillingClientStateListener
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == 0) {
                    RewardedAdLoader.this.queryPurchases();
                }
            }
        });
    }

    public void queryPurchases() {
        this.billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType("inapp").build(), new PurchasesResponseListener() { // from class: com.studio08.xbgamestream.Helpers.RewardedAdLoader.9
            @Override // com.android.billingclient.api.PurchasesResponseListener
            public void onQueryPurchasesResponse(BillingResult billingResult, List list) {
                if (billingResult.getResponseCode() != 0 || list == null) {
                    return;
                }
                Iterator it = list.iterator();
                boolean z = false;
                while (it.hasNext()) {
                    if (RewardedAdLoader.this.handlePurchase((Purchase) it.next())) {
                        z = true;
                    }
                }
                if (z) {
                    return;
                }
                RewardedAdLoader.this.checkCrossRestoreStatus();
            }
        });
    }

    public static boolean getPurchaseItem(Context context) {
        return context.getSharedPreferences("SettingsSharedPref", 0).getBoolean("boughtAdRemoval", false);
    }

    public static boolean shouldCheckNewToken(Context context) {
        long j = context.getSharedPreferences("SettingsSharedPref", 0).getLong("nextMakeGetTokenRequest", 0L);
        long currentTimeMillis = System.currentTimeMillis();
        if (j == 0) {
            Log.i("shouldCheckNewToken", SessionDescription.SUPPORTED_SDP_VERSION);
            return true;
        } else if (j < currentTimeMillis) {
            Log.i("shouldCheckNewToken", j + RemoteSettings.FORWARD_SLASH_STRING + currentTimeMillis);
            return true;
        } else {
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkCrossRestoreStatus() {
        if (!BuildConfig.FLAVOR.equals(BuildConfig.FLAVOR) && !BuildConfig.FLAVOR.equals("tv")) {
            Log.i("Purchase", "Ignore cross restore for non full/tv versions");
            setPurchaseItem(false, this.context);
        } else if (!shouldCheckNewToken(this.context)) {
            Log.e("Purchase", "Not revoking access yet. Token response cached");
        } else {
            new ApiClient(this.context).getToken();
        }
    }

    public static void setPurchaseItem(boolean z, Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences("SettingsSharedPref", 0).edit();
        edit.putBoolean("boughtAdRemoval", z);
        edit.apply();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean handlePurchase(final Purchase purchase) {
        if (purchase.getPurchaseState() == 1) {
            if (!purchase.isAcknowledged()) {
                this.billingClient.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build(), new AcknowledgePurchaseResponseListener() { // from class: com.studio08.xbgamestream.Helpers.RewardedAdLoader.10
                    @Override // com.android.billingclient.api.AcknowledgePurchaseResponseListener
                    public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                        if (purchase.getProducts().get(0).equals("remove_ads")) {
                            RewardedAdLoader.setPurchaseItem(true, RewardedAdLoader.this.context);
                            RewardedAdLoader.this.sendToken(purchase);
                        }
                    }
                });
                return true;
            } else if (purchase.getProducts().get(0).equals("remove_ads")) {
                setPurchaseItem(true, this.context);
                sendToken(purchase);
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendToken(Purchase purchase) {
        if (!BuildConfig.FLAVOR.equals(BuildConfig.FLAVOR)) {
            Log.i("Purchase", "Ignoring sendTokens for non full version");
            return;
        }
        try {
            if (this.encryptClient.getValue("purchaseToken").equals(purchase.getPurchaseToken())) {
                Log.e("Purchase", "Purchase token unchanged. Don't resend");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        new ApiClient(this.context).sendToken(purchase);
    }

    public void buyAdRemoval() {
        this.billingClient.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(ImmutableList.of(QueryProductDetailsParams.Product.newBuilder().setProductId("remove_ads").setProductType("inapp").build())).build(), new ProductDetailsResponseListener() { // from class: com.studio08.xbgamestream.Helpers.RewardedAdLoader.11
            @Override // com.android.billingclient.api.ProductDetailsResponseListener
            public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> list) {
                for (ProductDetails productDetails : list) {
                    if (productDetails.getProductId().equals("remove_ads")) {
                        RewardedAdLoader.this.billingClient.launchBillingFlow((Activity) RewardedAdLoader.this.context, BillingFlowParams.newBuilder().setProductDetailsParamsList(ImmutableList.of(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails).build())).build());
                    }
                }
            }
        });
    }
}
