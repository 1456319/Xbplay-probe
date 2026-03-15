package com.studio08.xbgamestream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PictureInPictureParams;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebStorage;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.amazon.device.drm.LicensingListener;
import com.amazon.device.drm.LicensingService;
import com.amazon.device.drm.model.LicenseResponse;
import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.UserDataResponse;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.studio08.xbgamestream.Authenticate.LoginActivityV4;
import com.studio08.xbgamestream.Authenticate.LoginClientV4;
import com.studio08.xbgamestream.Helpers.EncryptClient;
import com.studio08.xbgamestream.Helpers.FirebaseAnalyticsClient;
import com.studio08.xbgamestream.Helpers.Helper;
import com.studio08.xbgamestream.Helpers.IAPPricesManager;
import com.studio08.xbgamestream.Helpers.PWASettingsActivity;
import com.studio08.xbgamestream.Helpers.PWAWebviewHandler;
import com.studio08.xbgamestream.Helpers.PurchaseClient;
import com.studio08.xbgamestream.Servers.Server;
import com.studio08.xbgamestream.Web.ApiClient;
import com.studio08.xbgamestream.Web.SmartglassClient;
import com.studio08.xbgamestream.Web.StreamWebview;
import com.studio08.xbgamestream.Web.WifiClient;
import com.studio08.xbgamestream.databinding.ActivityFullscreenBinding;
import com.tapjoy.TJAdUnitConstants;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.geckoview.GeckoView;
/* loaded from: /app/base.apk/classes3.dex */
public class PWAMainMenuActivity extends AppCompatActivity {
    private FirebaseAnalyticsClient analyticsClient;
    private ActivityFullscreenBinding binding;
    private ConstraintLayout constraintLayout;
    private GeckoView mGeckoview;
    private StreamWebview mSystemWebview;
    private PWAScreenCastClient pWAScreenCastClient;
    private PWAWebviewHandler pwaWebviewHandler;
    private Server server;
    private WifiClient wifiClient;
    private PurchaseClient purchaseClient = null;
    private SmartglassClient smartglass = null;
    private boolean isInPip = false;
    private int RELOAD_WEBVIEW_ON_SUCCESS_ACTIVITY_RESULT = 555;
    private boolean initializedViews = false;
    private String fireTvId = null;
    private boolean fireTvDrmPassed = false;
    private boolean startedByShortcut = false;
    ApiClient.StreamingClientListener webviewEventListener = new ApiClient.StreamingClientListener() { // from class: com.studio08.xbgamestream.PWAMainMenuActivity.1
        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void onCloseScreenDetected() {
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void onReLoginDetected() {
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void pressButtonWifiRemote(String str) {
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void setOrientationValue(String str) {
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void vibrate() {
            Helper.vibrate(PWAMainMenuActivity.this);
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void genericMessage(final String str, final String str2) {
            PWAMainMenuActivity.this.runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.PWAMainMenuActivity.1.1
                @Override // java.lang.Runnable
                public void run() {
                    PWAMainMenuActivity.this.handleGenericMessage(str, str2);
                }
            });
        }
    };

    /* loaded from: /app/base.apk/classes3.dex */
    public class FireTvPurchaseListener implements PurchasingListener {
        public FireTvPurchaseListener() {
        }

        @Override // com.amazon.device.iap.PurchasingListener
        public void onUserDataResponse(UserDataResponse userDataResponse) {
            Log.e("PWAMM", "onUserDataResponse: " + userDataResponse.toString());
            if (userDataResponse.getRequestStatus() == UserDataResponse.RequestStatus.SUCCESSFUL) {
                String userId = userDataResponse.getUserData().getUserId();
                Log.e("PWAMM", "onUserDataResponse: " + userId);
                PWAMainMenuActivity.this.fireTvId = userId;
                if (PWAMainMenuActivity.this.fireTvDrmPassed) {
                    PWAMainMenuActivity.this.purchaseClient.setFireTvToken(PWAMainMenuActivity.this.fireTvId);
                }
            }
        }

        @Override // com.amazon.device.iap.PurchasingListener
        public void onProductDataResponse(ProductDataResponse productDataResponse) {
            Log.e("PWAMM", "onProductDataResponse: " + productDataResponse.toString());
        }

        @Override // com.amazon.device.iap.PurchasingListener
        public void onPurchaseResponse(PurchaseResponse purchaseResponse) {
            Log.e("PWAMM", "onProductDataResponse: " + purchaseResponse.toString());
        }

        @Override // com.amazon.device.iap.PurchasingListener
        public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse purchaseUpdatesResponse) {
            Log.e("PWAMM", "onProductDataResponse: " + purchaseUpdatesResponse.toString());
        }
    }

    /* loaded from: /app/base.apk/classes3.dex */
    public class LicenseVerificationCallback implements LicensingListener {
        public LicenseVerificationCallback() {
        }

        @Override // com.amazon.device.drm.LicensingListener
        public void onLicenseCommandResponse(LicenseResponse licenseResponse) {
            LicenseResponse.RequestStatus requestStatus = licenseResponse.getRequestStatus();
            try {
                Log.e("PWAMM", "onLicenseCommandResponse: RequestStatus (" + requestStatus + ")" + licenseResponse.toJSON().toString());
                if (requestStatus == LicenseResponse.RequestStatus.LICENSED) {
                    PWAMainMenuActivity.this.fireTvDrmPassed = true;
                    if (PWAMainMenuActivity.this.fireTvId != null) {
                        PWAMainMenuActivity.this.purchaseClient.setFireTvToken(PWAMainMenuActivity.this.fireTvId);
                        return;
                    }
                    return;
                }
                new AlertDialog.Builder(PWAMainMenuActivity.this).setTitle("License Verification Failed").setMessage("We couldn't verify your license for this app. This might happen if the app wasn't purchased correctly from the Amazon Appstore. Please ensure that you bought this app using a valid Amazon account. Contact support or try reinstalling the app from the official store to resolve this issue. Error: " + requestStatus).setCancelable(false).setPositiveButton("Exit App", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.PWAMainMenuActivity.LicenseVerificationCallback.1
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PWAMainMenuActivity.this.finishAffinity();
                    }
                }).show();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleGenericMessage(String str, String str2) {
        PWAScreenCastClient pWAScreenCastClient;
        Log.e("PWAMM", "handleGenericMessage: " + str + str2);
        if (str.equals("pwa_show_login")) {
            try {
                EncryptClient encryptClient = new EncryptClient(getApplicationContext());
                if (!str2.isEmpty()) {
                    encryptClient.saveValue("loginRegionIp", str2);
                } else {
                    encryptClient.deleteValue("loginRegionIp");
                }
                startActivityForResult(new Intent(this, LoginActivityV4.class), this.RELOAD_WEBVIEW_ON_SUCCESS_ACTIVITY_RESULT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (str.equals("pwa_show_logout")) {
            clearCache();
        } else if (str.equals("set_orientation")) {
            setOrientationType(str2);
        } else if (str.equals("pwa_show_settings")) {
            startActivityForResult(new Intent(this, PWASettingsActivity.class), this.RELOAD_WEBVIEW_ON_SUCCESS_ACTIVITY_RESULT);
        } else if (str.equals("pwa_toggle_mirrorcast")) {
            toggleMirrorCast(str2, false);
        } else if (str.equals("smartglass")) {
            this.smartglass.sendSmartglassCommand(str2);
        } else if (str.equals("pwa_show_restore_purchase")) {
            this.purchaseClient.queryPurchases();
        } else if (str.equals("pwa_show_iap")) {
            if (str2.contains("yearly") || str2.contains("monthly")) {
                this.purchaseClient.purchasesSubscription(str2);
            } else {
                this.purchaseClient.purchaseProduct(str2);
            }
        } else if (str.equals("open_link")) {
            if (str2.isEmpty()) {
                return;
            }
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str2)));
        } else if (str.equals("xalTokenUpdateRequest")) {
            try {
                JSONObject jSONObject = new JSONObject(str2).getJSONObject("data");
                Log.e("PWAMM", "xalTokenUpdateRequest: " + jSONObject);
                LoginClientV4.saveXalTokenData(jSONObject, this, null);
                this.pwaWebviewHandler.alreadyCalledPwaConfig = false;
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
        } else if (str.equals("pwaInitialLoadComplete")) {
            handleXCloudShortcutStart(getIntent());
            handleDeepLinkRedirect(getIntent());
            handleXHomeShortcutStart(getIntent());
            sendFireTvToken();
        } else if (str.equals("webviewPageLoadComplete")) {
            if (str2.contains("#media_screen") && (pWAScreenCastClient = this.pWAScreenCastClient) != null) {
                pWAScreenCastClient.updateInfoText();
            }
            new Handler().postDelayed(new Runnable() { // from class: com.studio08.xbgamestream.PWAMainMenuActivity$$ExternalSyntheticLambda5
                @Override // java.lang.Runnable
                public final void run() {
                    PWAMainMenuActivity.this.m342x36aae190();
                }
            }, 1000L);
        } else if (str.equals("pwa_prompt_for_shortcut_creation")) {
            createShortcut(str2);
        } else if (str.equals("pwa_media_cast_action")) {
            if (str2.equals("selectDevice")) {
                this.pWAScreenCastClient.init();
                this.pWAScreenCastClient.selectDevice();
            } else if (str2.equals("selectAudioFile")) {
                this.pWAScreenCastClient.init();
                this.pWAScreenCastClient.AUDIO_CAST_MODE = true;
                this.pWAScreenCastClient.selectFile();
            } else if (str2.equals("selectVideoFile")) {
                this.pWAScreenCastClient.init();
                this.pWAScreenCastClient.AUDIO_CAST_MODE = false;
                this.pWAScreenCastClient.selectFile();
            } else if (str2.equals("doCast")) {
                if (this.pWAScreenCastClient.isPlaying) {
                    this.pWAScreenCastClient.cleanUp();
                    Toast.makeText(this, "Stopping cast", 0).show();
                    return;
                }
                this.pWAScreenCastClient.init();
                this.pWAScreenCastClient.castToConsole(true);
            } else {
                Log.e("PWAMM", "Invalid pwa_media_cast_action msg sent" + str2);
            }
        } else if (str.equals("pwa_media_cast_button_press_action")) {
            this.pWAScreenCastClient.sendCastRemoteCommand(str2);
        } else if (str.equals("pwa_exit_main_menu")) {
            if (Helper.getRenderEngine(this).equals("webview")) {
                return;
            }
            this.pwaWebviewHandler.handleRenderEngineSwitch(str2);
        } else if (str.equals("pwa_return_main_menu")) {
            if (Helper.getRenderEngine(this).equals("webview")) {
                return;
            }
            this.pwaWebviewHandler.handleRenderEngineReturn(Objects.equals(str2, "true"));
        } else if (str.equals("quitGameUseForShortcuts")) {
            if (this.startedByShortcut) {
                Log.e("PWAMM", "Handle QuitGame");
                finishAffinity();
            }
        } else {
            Log.e("PWAMM", "Invalid PWA generic message type: " + str);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$handleGenericMessage$0$com-studio08-xbgamestream-PWAMainMenuActivity  reason: not valid java name */
    public /* synthetic */ void m342x36aae190() {
        hideSystemUI(this);
    }

    private void setupGoogleAnalytics() {
        try {
            this.analyticsClient = new FirebaseAnalyticsClient(FirebaseAnalytics.getInstance(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleMirrorCast(String str, boolean z) {
        try {
            Log.e("PWAMM", "toggleMirrorCast");
            JSONObject jSONObject = new JSONObject(str);
            String string = jSONObject.getString("serverId");
            String string2 = jSONObject.getString("gsToken");
            Log.e("PWAMM", jSONObject.toString());
            if (this.server != null) {
                addMirrorCastLog("", "Stopped");
                this.server.stop();
                this.server = null;
                return;
            }
            Server server = new Server(!z ? Server.PORT : 0, this, string2, string);
            this.server = server;
            server.start();
            addMirrorCastLog("Running on http://" + Helper.getLocalIpAddress() + ":" + this.server.getListeningPort(), "Started http://" + Helper.getLocalIpAddress() + ":" + this.server.getListeningPort());
        } catch (IOException | Error | JSONException e) {
            e.printStackTrace();
            if (!z) {
                Server server2 = this.server;
                if (server2 != null) {
                    server2.stop();
                    this.server = null;
                }
                toggleMirrorCast(str, true);
                return;
            }
            Toast.makeText(this, "Error: " + e.getMessage(), 0).show();
        }
    }

    private void addMirrorCastLog(String str, String str2) {
        ApiClient.callJavaScript(this.mSystemWebview, "setMirrorcastData", str, str2);
    }

    private void setOrientationType(String str) {
        if (!getSharedPreferences("SettingsSharedPref", 0).getBoolean("lock_orientation_key", true)) {
            setRequestedOrientation(10);
        } else if (str.equals(TJAdUnitConstants.String.LANDSCAPE)) {
            setRequestedOrientation(6);
        } else if (str.equals(TJAdUnitConstants.String.PORTRAIT)) {
            setRequestedOrientation(7);
        } else if (str.equals("unlock")) {
            setRequestedOrientation(10);
        } else {
            Log.e("PWAMM", "Invalid orientation value: " + str);
        }
    }

    private void clearCache() {
        Toast.makeText(this, "Clearing cache", 1).show();
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        new EncryptClient(getApplicationContext()).deleteAll();
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        Log.e("PWAMM", "onCreate");
        configureTheme();
        super.onCreate(bundle);
        if (!this.initializedViews) {
            handleLegacyTheme();
            getProductPrices();
        }
        ActivityFullscreenBinding inflate = ActivityFullscreenBinding.inflate(getLayoutInflater());
        this.binding = inflate;
        setContentView(inflate.getRoot());
        this.smartglass = new SmartglassClient(getApplicationContext());
        if (!this.initializedViews) {
            setupWebviews();
            this.purchaseClient = new PurchaseClient(this, this.mSystemWebview);
            loadPwaMainMenu();
            Helper.checkIfUpdateAvailable(this);
            setupGoogleAnalytics();
            PWAScreenCastClient pWAScreenCastClient = this.pWAScreenCastClient;
            if (pWAScreenCastClient != null) {
                pWAScreenCastClient.cleanUp();
            }
            Helper.showRatingApiMaybe(this);
            if (BuildConfig.FLAVOR.equals("firetv")) {
                PurchasingService.registerListener(getApplicationContext(), new FireTvPurchaseListener());
                LicensingService.verifyLicense(getApplicationContext(), new LicenseVerificationCallback());
                PurchasingService.getUserData();
            } else if (BuildConfig.FLAVOR.equals(BuildConfig.FLAVOR)) {
                this.pWAScreenCastClient = new PWAScreenCastClient(this, this.mSystemWebview);
            }
            handleWifiLowLatencyMode();
            handleAudioLowLatencyMode();
        }
        hideSystemUI(this);
        this.initializedViews = true;
    }

    private void handleAudioLowLatencyMode() {
        if (BuildConfig.FLAVOR.equals("mediaRemote") || BuildConfig.FLAVOR.equals("gamepadController") || !getSharedPreferences("SettingsSharedPref", 0).getBoolean("use_audio_low_latency_mode_key", true)) {
            return;
        }
        enableMinimalPostProcessing();
    }

    private void handleWifiLowLatencyMode() {
        if (BuildConfig.FLAVOR.equals("mediaRemote") || BuildConfig.FLAVOR.equals("gamepadController")) {
            return;
        }
        this.wifiClient = new WifiClient(this);
        if (getSharedPreferences("SettingsSharedPref", 0).getBoolean("use_wifi_low_latency_mode_key", true)) {
            this.wifiClient.acquireWifiLock();
        }
    }

    private void getProductPrices() {
        if (BuildConfig.FLAVOR.equals(BuildConfig.FLAVOR)) {
            new IAPPricesManager(this);
        }
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onUserLeaveHint() {
        PWAWebviewHandler pWAWebviewHandler;
        super.onUserLeaveHint();
        if (!BuildConfig.FLAVOR.equals(BuildConfig.FLAVOR) || (pWAWebviewHandler = this.pwaWebviewHandler) == null || pWAWebviewHandler.getCurrentUrl() == null || !this.pwaWebviewHandler.getCurrentUrl().contains("android_stream") || Build.VERSION.SDK_INT < 26 || !getPackageManager().hasSystemFeature("android.software.picture_in_picture")) {
            return;
        }
        Rational rational = new Rational(16, 9);
        PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
        builder.setAspectRatio(rational).build();
        enterPictureInPictureMode(builder.build());
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onPictureInPictureModeChanged(boolean z, Configuration configuration) {
        super.onPictureInPictureModeChanged(z, configuration);
        if (BuildConfig.FLAVOR.equals(BuildConfig.FLAVOR)) {
            if (z) {
                Log.d("PiPMode", "Entered PiP mode");
                this.pwaWebviewHandler.togglePip(true);
                this.isInPip = true;
                return;
            }
            Log.d("PiPMode", "Exited PiP mode");
            this.pwaWebviewHandler.togglePip(false);
            this.isInPip = false;
        }
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        PWAWebviewHandler pWAWebviewHandler = this.pwaWebviewHandler;
        if (pWAWebviewHandler != null) {
            pWAWebviewHandler.cleanUpBeforeDestroy();
        }
        PWAScreenCastClient pWAScreenCastClient = this.pWAScreenCastClient;
        if (pWAScreenCastClient != null) {
            pWAScreenCastClient.cleanUp();
        }
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        if (this.isInPip) {
            finishAffinity();
            return;
        }
        WifiClient wifiClient = this.wifiClient;
        if (wifiClient != null) {
            wifiClient.releaseWifiLock();
        }
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
        PWAWebviewHandler pWAWebviewHandler;
        if ((BuildConfig.FLAVOR.equals("tv") || BuildConfig.FLAVOR.equals("vr") || BuildConfig.FLAVOR.equals("firetv")) && (pWAWebviewHandler = this.pwaWebviewHandler) != null && pWAWebviewHandler.getCurrentUrl() != null && this.pwaWebviewHandler.getCurrentUrl().contains("android_stream")) {
            this.pwaWebviewHandler.sendToggleTVMenuCommand();
        } else {
            new AlertDialog.Builder(this).setTitle("Exit").setMessage("Are you sure you want to exit?").setCancelable(true).setPositiveButton("Exit App", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.PWAMainMenuActivity.3
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    PWAMainMenuActivity.this.finishAffinity();
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.PWAMainMenuActivity.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            }).show();
        }
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        StreamWebview streamWebview = this.mSystemWebview;
        if (streamWebview != null) {
            streamWebview.saveState(bundle);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        if (Helper.getRenderEngine(this).equals("chrome")) {
            setOrientationType("unlock");
        }
        if (BuildConfig.FLAVOR.equals("fireTv")) {
            PurchasingService.getUserData();
        }
        if (getSharedPreferences("SettingsSharedPref", 0).getBoolean("use_wifi_low_latency_mode_key", true)) {
            this.wifiClient.acquireWifiLock();
        }
    }

    @Override // android.app.Activity
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        StreamWebview streamWebview = this.mSystemWebview;
        if (streamWebview != null) {
            streamWebview.restoreState(bundle);
        }
    }

    private void handleLegacyTheme() {
        if (getSharedPreferences("SettingsSharedPref", 0).getBoolean("pwa_use_legacy_theme_key", false)) {
            Intent intent = getIntent();
            Intent intent2 = new Intent(this, MainActivity.class);
            if (BuildConfig.FLAVOR.equals("tv") || BuildConfig.FLAVOR.equals("firetv") || BuildConfig.FLAVOR.equals("vr")) {
                try {
                    intent2 = new Intent(this, Class.forName("com.studio08.xbgamestream.MainTvActivity"));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return;
                }
            }
            String action = intent.getAction();
            if (action != null) {
                intent2.setAction(action);
            }
            Uri data = intent.getData();
            if (data != null) {
                intent2.setData(data);
            }
            String type = intent.getType();
            if (type != null) {
                intent2.setType(type);
            }
            Set<String> categories = intent.getCategories();
            if (categories != null) {
                for (String str : categories) {
                    intent2.addCategory(str);
                }
            }
            intent2.setFlags(intent.getFlags());
            Bundle extras = intent.getExtras();
            if (extras != null) {
                intent2.putExtras(extras);
            }
            ClipData clipData = intent.getClipData();
            if (clipData != null) {
                intent2.setClipData(clipData);
            }
            startActivity(intent2);
            finish();
            return;
        }
        SharedPreferences sharedPreferences = getSharedPreferences("rate", 0);
        int i = sharedPreferences.getInt("appOpens", 0);
        if (sharedPreferences.getBoolean("alreadyShowedPopup", false) || i <= 0) {
            return;
        }
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("alreadyShowedPopup", true);
        edit.apply();
        new AlertDialog.Builder(this).setTitle("New Theme").setMessage("Welcome to the new app theme! If you prefer the old theme, you can revert to the legacy theme in the settings.").setCancelable(false).setPositiveButton("Ok", (DialogInterface.OnClickListener) null).show();
    }

    public void loadPwaMainMenu() {
        this.pwaWebviewHandler.doPwaMainMenu();
        this.mSystemWebview.requestFocus();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i2 != -1) {
            return;
        }
        if (i == this.RELOAD_WEBVIEW_ON_SUCCESS_ACTIVITY_RESULT) {
            this.pwaWebviewHandler.setRenderEngineDependentListeners();
            ApiClient.callJavaScript(this.mSystemWebview, "setPWAConfigData", PWAWebviewHandler.getPwaConfigSettings(this));
            sendFireTvToken();
        } else if (i == PWAScreenCastClient.PICKFILE_RESULT_CODE) {
            this.pWAScreenCastClient.handleFileSelectCallback(intent);
        }
    }

    private void sendFireTvToken() {
        PurchaseClient purchaseClient;
        String str;
        Log.e("PWAMM", "called sendFireTvToken. ID: " + this.fireTvId + " Passed: " + this.fireTvDrmPassed);
        if (!BuildConfig.FLAVOR.equals("firetv") || (purchaseClient = this.purchaseClient) == null || (str = this.fireTvId) == null || !this.fireTvDrmPassed) {
            return;
        }
        purchaseClient.setFireTvToken(str);
    }

    private void createShortcut(String str) {
        try {
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(this)) {
                JSONObject jSONObject = new JSONObject(str);
                String string = jSONObject.getString("type");
                if (string.equals("xcloud")) {
                    String string2 = jSONObject.getString("titleId");
                    String string3 = jSONObject.getString("image");
                    String string4 = jSONObject.getString("title");
                    Log.e("PWAMM", jSONObject.toString());
                    Helper.addShortcutToHomeScreen(this, string2, string4, string3, string);
                } else if (string.equals("xhome")) {
                    String string5 = jSONObject.getString("titleId");
                    String string6 = jSONObject.getString("image");
                    String string7 = jSONObject.getString("title");
                    Log.e("PWAMM", jSONObject.toString());
                    Helper.addShortcutToHomeScreen(this, string5, string7, string6, string);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("HERE", "Failed to decode json message");
        }
    }

    private void handleXCloudShortcutStart(Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        final String stringExtra = intent.getStringExtra("titleId");
        Log.e("PWAMM", "handleXCloudShortcutStart: " + action + " " + stringExtra);
        if (TextUtils.isEmpty(stringExtra) || action == null || TextUtils.isEmpty(action) || !action.equals("xcloudstart")) {
            return;
        }
        Toast.makeText(this, "Launching: " + stringExtra, 0).show();
        this.startedByShortcut = true;
        setIntent(new Intent());
        runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.PWAMainMenuActivity$$ExternalSyntheticLambda6
            @Override // java.lang.Runnable
            public final void run() {
                PWAMainMenuActivity.this.m343xb42be6d6(stringExtra);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$handleXCloudShortcutStart$1$com-studio08-xbgamestream-PWAMainMenuActivity  reason: not valid java name */
    public /* synthetic */ void m343xb42be6d6(String str) {
        ApiClient.callJavaScript(this.mSystemWebview, "redirectToCloudPlay", str);
    }

    private void handleXHomeShortcutStart(Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        final String stringExtra = intent.getStringExtra("titleId");
        Log.e("PWAMM", "handleXHomeShortcutStart: " + action + " " + stringExtra);
        if (TextUtils.isEmpty(stringExtra) || action == null || TextUtils.isEmpty(action) || !action.equals("xhomestart")) {
            return;
        }
        Toast.makeText(this, "Launching: " + stringExtra, 0).show();
        this.startedByShortcut = true;
        setIntent(new Intent());
        runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.PWAMainMenuActivity$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                PWAMainMenuActivity.this.m344x1ac176e7(stringExtra);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$handleXHomeShortcutStart$2$com-studio08-xbgamestream-PWAMainMenuActivity  reason: not valid java name */
    public /* synthetic */ void m344x1ac176e7(String str) {
        ApiClient.callJavaScript(this.mSystemWebview, "redirectToRemotePlay", "false", "false", str);
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onStart() {
        super.onStart();
    }

    private void handleDeepLinkRedirect(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            List<String> pathSegments = data.getPathSegments();
            Log.e("LaunchIntent", "Params: " + pathSegments + data);
            if (data.getHost().equals("launch")) {
                String str = pathSegments.get(0);
                str.hashCode();
                char c = 65535;
                switch (str.hashCode()) {
                    case -764712771:
                        if (str.equals("xcloud")) {
                            c = 0;
                            break;
                        }
                        break;
                    case 3714:
                        if (str.equals("tv")) {
                            c = 1;
                            break;
                        }
                        break;
                    case 114030935:
                        if (str.equals("xhome")) {
                            c = 2;
                            break;
                        }
                        break;
                    case 637428636:
                        if (str.equals("controller")) {
                            c = 3;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                    case 2:
                        runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.PWAMainMenuActivity$$ExternalSyntheticLambda2
                            @Override // java.lang.Runnable
                            public final void run() {
                                PWAMainMenuActivity.this.m338x30e99de5();
                            }
                        });
                        setIntent(new Intent());
                        return;
                    case 1:
                        if (pathSegments.size() >= 2) {
                            final String str2 = pathSegments.get(1);
                            Toast.makeText(this, "TV CODE: " + str2, 0).show();
                            new Handler().postDelayed(new Runnable() { // from class: com.studio08.xbgamestream.PWAMainMenuActivity$$ExternalSyntheticLambda4
                                @Override // java.lang.Runnable
                                public final void run() {
                                    PWAMainMenuActivity.this.m341x915810a8(str2);
                                }
                            }, 1000L);
                            setIntent(new Intent());
                            return;
                        }
                        return;
                    case 3:
                        runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.PWAMainMenuActivity$$ExternalSyntheticLambda3
                            @Override // java.lang.Runnable
                            public final void run() {
                                PWAMainMenuActivity.this.m339xa663c426();
                            }
                        });
                        setIntent(new Intent());
                        return;
                    default:
                        Log.e("LaunchIntent", "Unknown launch activity: " + str);
                        return;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$handleDeepLinkRedirect$3$com-studio08-xbgamestream-PWAMainMenuActivity  reason: not valid java name */
    public /* synthetic */ void m338x30e99de5() {
        ApiClient.callJavaScript(this.mSystemWebview, "showScreen", "play_screen");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$handleDeepLinkRedirect$4$com-studio08-xbgamestream-PWAMainMenuActivity  reason: not valid java name */
    public /* synthetic */ void m339xa663c426() {
        ApiClient.callJavaScript(this.mSystemWebview, "showScreen", "controller_screen");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$handleDeepLinkRedirect$5$com-studio08-xbgamestream-PWAMainMenuActivity  reason: not valid java name */
    public /* synthetic */ void m340x1bddea67(String str) {
        ApiClient.callJavaScript(this.mSystemWebview, "setTvCodeFromDeepLink", str);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$handleDeepLinkRedirect$6$com-studio08-xbgamestream-PWAMainMenuActivity  reason: not valid java name */
    public /* synthetic */ void m341x915810a8(final String str) {
        runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.PWAMainMenuActivity$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                PWAMainMenuActivity.this.m340x1bddea67(str);
            }
        });
    }

    public void setupWebviews() {
        this.constraintLayout = this.binding.constrainedLayout;
        GeckoView geckoView = new GeckoView(this);
        this.mGeckoview = geckoView;
        geckoView.setId(View.generateViewId());
        StreamWebview streamWebview = this.binding.systemwebview;
        this.mSystemWebview = streamWebview;
        streamWebview.setBackgroundColor(0);
        PWAWebviewHandler pWAWebviewHandler = new PWAWebviewHandler(this, this.mSystemWebview, this.mGeckoview, this.webviewEventListener, this.constraintLayout);
        this.pwaWebviewHandler = pWAWebviewHandler;
        pWAWebviewHandler.initWebviews();
    }

    public void configureTheme() {
        AppCompatDelegate.setDefaultNightMode(2);
        getWindow().addFlags(128);
        requestWindowFeature(1);
        if (getSharedPreferences("SettingsSharedPref", 0).getBoolean("use_notch_key", true)) {
            return;
        }
        setTheme(R.style.Theme_MyApplication3);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e("PWAMM", "handle new intent" + intent.getAction());
        handleXCloudShortcutStart(intent);
        handleDeepLinkRedirect(intent);
        handleXHomeShortcutStart(intent);
    }

    public static void hideSystemUI(Context context) {
        if (Build.VERSION.SDK_INT < 30) {
            ((Activity) context).getWindow().getDecorView().setSystemUiVisibility(5894);
            return;
        }
        Activity activity = (Activity) context;
        WindowInsetsControllerCompat windowInsetsController = ViewCompat.getWindowInsetsController(activity.getWindow().getDecorView());
        if (windowInsetsController == null) {
            return;
        }
        windowInsetsController.setSystemBarsBehavior(2);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        if (activity.getSharedPreferences("SettingsSharedPref", 0).getBoolean("use_notch_key", true)) {
            activity.getWindow().setFlags(1024, 1024);
            activity.getWindow().setFlags(512, 512);
            activity.getWindow().getAttributes().layoutInDisplayCutoutMode = 1;
        }
    }

    public void enableMinimalPostProcessing() {
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                getWindow().addFlags(33554432);
            } else {
                Log.w("PWAMM", "Minimal post-processing requires Android 11 or higher.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
