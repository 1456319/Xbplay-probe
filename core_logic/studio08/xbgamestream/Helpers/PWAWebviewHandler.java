package com.studio08.xbgamestream.Helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.pm.ShortcutManagerCompat;
import com.studio08.xbgamestream.BuildConfig;
import com.studio08.xbgamestream.Controller.ControllerHandler;
import com.studio08.xbgamestream.Web.ApiClient;
import com.studio08.xbgamestream.Web.CustomWebClient;
import com.studio08.xbgamestream.Web.GeckoWebviewClient;
import com.studio08.xbgamestream.Web.StreamWebview;
import com.studio08.xbgamestream.Web.StreamWebviewListener;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.geckoview.GeckoView;
/* loaded from: /app/base.apk/classes3.dex */
public class PWAWebviewHandler {
    public static String PWA_MAIN_MENU;
    private ConstraintLayout constraintLayout;
    ControllerHandler controllerHandler;
    GeckoWebviewClient geckoWebviewClient;
    public Boolean isGeckoViewRenderEngine;
    private String lastUrl;
    ApiClient.StreamingClientListener listener;
    Context mContext;
    GeckoView mGeckoView;
    StreamWebview mSystemWebview;
    private final int MAX_CALLS_PER_SECOND = 60;
    private final long MIN_TIME_BETWEEN_CALLS_MS = 16;
    private long lastCallTime = 0;
    private float movementXValue = 0.0f;
    private float movementYValue = 0.0f;
    public boolean alreadyCalledPwaConfig = false;
    ControllerHandler.ControllerHandlerListener controllerHandlerListener = new ControllerHandler.ControllerHandlerListener() { // from class: com.studio08.xbgamestream.Helpers.PWAWebviewHandler.1
        @Override // com.studio08.xbgamestream.Controller.ControllerHandler.ControllerHandlerListener
        public void controllerData(JSONObject jSONObject) {
            if (PWAWebviewHandler.this.isGeckoViewRenderEngine.booleanValue()) {
                PWAWebviewHandler.this.geckoWebviewClient.sendControllerInput(jSONObject);
            }
        }
    };
    CustomWebClient webviewPageFinishedListener = new CustomWebClient(false) { // from class: com.studio08.xbgamestream.Helpers.PWAWebviewHandler.2
        @Override // com.studio08.xbgamestream.Web.CustomWebClient, android.webkit.WebViewClient
        public void onPageFinished(WebView webView, String str) {
            super.onPageFinished(webView, str);
            if (str.contains("pwa")) {
                if (!PWAWebviewHandler.this.alreadyCalledPwaConfig) {
                    ApiClient.callJavaScript(PWAWebviewHandler.this.mSystemWebview, "setPWAConfigData", PWAWebviewHandler.getPwaConfigSettings(PWAWebviewHandler.this.mContext));
                    PWAWebviewHandler.this.listener.genericMessage("pwaInitialLoadComplete", "");
                    PWAWebviewHandler.this.alreadyCalledPwaConfig = true;
                }
                PWAWebviewHandler.this.listener.genericMessage("webviewPageLoadComplete", str);
            }
        }
    };
    StreamWebviewListener webviewListener = new StreamWebviewListener() { // from class: com.studio08.xbgamestream.Helpers.PWAWebviewHandler.3
        @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
        public void onReLoginRequest() {
            ((Activity) PWAWebviewHandler.this.mContext).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Helpers.PWAWebviewHandler.3.1
                @Override // java.lang.Runnable
                public void run() {
                    PWAWebviewHandler.this.listener.onReLoginDetected();
                }
            });
        }

        @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
        public void closeScreen() {
            PWAWebviewHandler.this.listener.onCloseScreenDetected();
        }

        @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
        public void pressButtonWifiRemote(String str) {
            PWAWebviewHandler.this.listener.pressButtonWifiRemote(str);
        }

        @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
        public void setOrientationValue(String str) {
            PWAWebviewHandler.this.listener.setOrientationValue(str);
        }

        @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
        public void vibrate() {
            PWAWebviewHandler.this.listener.vibrate();
        }

        @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
        public void genericMessage(String str, final String str2) {
            if (str.equals("rumble")) {
                ((Activity) PWAWebviewHandler.this.mContext).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Helpers.PWAWebviewHandler.3.2
                    @Override // java.lang.Runnable
                    public void run() {
                        if (PWAWebviewHandler.this.controllerHandler != null) {
                            try {
                                PWAWebviewHandler.this.controllerHandler.handleRumble(str2);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            } else if (str.equals("pointerLock")) {
                PWAWebviewHandler.this.togglePointerLock(str2);
            } else {
                PWAWebviewHandler.this.listener.genericMessage(str, str2);
            }
        }
    };

    static {
        StringBuilder sb;
        String str;
        if (ApiClient.USE_DEV) {
            sb = new StringBuilder();
            str = ApiClient.BASE_URL_DEV;
        } else {
            sb = new StringBuilder();
            str = ApiClient.BASE_URL_PROD;
        }
        PWA_MAIN_MENU = sb.append(str).append("pwa/main.html").toString();
    }

    public PWAWebviewHandler(Context context, StreamWebview streamWebview, GeckoView geckoView, ApiClient.StreamingClientListener streamingClientListener, ConstraintLayout constraintLayout) {
        this.mGeckoView = geckoView;
        this.mSystemWebview = streamWebview;
        this.mContext = context;
        this.isGeckoViewRenderEngine = Boolean.valueOf(Helper.getRenderEngine(context).equals("geckoview"));
        this.controllerHandler = new ControllerHandler(this.mContext);
        this.listener = streamingClientListener;
        this.constraintLayout = constraintLayout;
    }

    public void initWebviews() {
        this.mSystemWebview.init();
        this.geckoWebviewClient = new GeckoWebviewClient(this.mContext, this.mGeckoView, true);
        setRenderEngineDependentListeners();
        this.controllerHandler.setListener(this.controllerHandlerListener);
        this.mSystemWebview.setWebViewClient(this.webviewPageFinishedListener);
        this.mSystemWebview.setCustomObjectListener(this.webviewListener);
        this.geckoWebviewClient.setCustomObjectListener(this.webviewListener);
    }

    public void setRenderEngineDependentListeners() {
        setPointerCaptureListener();
        setControllerHandler();
    }

    public void doPwaMainMenu() {
        this.mSystemWebview.loadUrl(PWA_MAIN_MENU + "?pwaPlatform=" + getPwaPlatform());
    }

    public String getCurrentUrl() {
        StreamWebview streamWebview;
        if (Helper.getRenderEngine(this.mContext).equals("geckoview")) {
            GeckoWebviewClient geckoWebviewClient = this.geckoWebviewClient;
            if (geckoWebviewClient != null) {
                return geckoWebviewClient.getCurrentUrl();
            }
        } else if (!Helper.getRenderEngine(this.mContext).equals("chrome") && (streamWebview = this.mSystemWebview) != null && streamWebview.getUrl() != null) {
            return this.mSystemWebview.getUrl();
        }
        return null;
    }

    private String getPwaPlatform() {
        if (BuildConfig.FLAVOR.equals("tv")) {
            return "androidtv";
        }
        return BuildConfig.FLAVOR.equals(BuildConfig.FLAVOR) ? "android" : BuildConfig.FLAVOR;
    }

    public void setControllerHandler() {
        if (this.isGeckoViewRenderEngine.booleanValue()) {
            this.controllerHandler.setSourceView(this.mGeckoView);
        } else {
            this.controllerHandler.setPassthroughView(this.mSystemWebview);
        }
    }

    public void handleRenderEngineSwitch(String str) {
        String renderEngine = Helper.getRenderEngine(this.mContext);
        if (renderEngine.equals("webview") || renderEngine.equals("empty")) {
            return;
        }
        try {
            this.lastUrl = this.mSystemWebview.getUrl();
            Log.e("PWAWH", "Saved current URL Before render engine switch" + this.lastUrl);
            JSONObject jSONObject = new JSONObject(str);
            Uri parse = Uri.parse(this.lastUrl);
            String str2 = (parse.getScheme() + "://" + parse.getAuthority() + parse.getPath()) + "/../" + jSONObject.getString("url");
            if (renderEngine.equals("geckoview")) {
                if (ShortcutManagerCompat.isRequestPinShortcutSupported(this.mContext)) {
                    jSONObject.put("steam_game_list_shortcuts", true);
                }
                jSONObject.put("native_app_version", 75);
                this.geckoWebviewClient.pwaConfigData = jSONObject;
                switchToGeckoView();
                this.geckoWebviewClient.loadUrl(str2);
            } else if (renderEngine.equals("chrome")) {
                new TWAClient(this.mContext, str).launchTWSA(str2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleRenderEngineReturn(boolean z) {
        String renderEngine = Helper.getRenderEngine(this.mContext);
        if (renderEngine.equals("webview") || renderEngine.equals("empty") || !Helper.getRenderEngine(this.mContext).equals("geckoview")) {
            return;
        }
        Log.e("PWAWH", this.lastUrl);
        switchToWebView();
        if (z) {
            this.mSystemWebview.loadUrl(PWA_MAIN_MENU + "?pcheckRedirect=1");
        } else {
            this.mSystemWebview.loadUrl(this.lastUrl);
        }
    }

    public void togglePointerLock(String str) {
        Log.e("HERE", "handle pointer lock" + str);
        if (Build.VERSION.SDK_INT >= 26) {
            if (str.equals("true")) {
                if (!this.isGeckoViewRenderEngine.booleanValue()) {
                    this.mSystemWebview.requestPointerCapture();
                } else {
                    this.mGeckoView.requestPointerCapture();
                }
            } else if (!this.isGeckoViewRenderEngine.booleanValue()) {
                this.mSystemWebview.releasePointerCapture();
            } else {
                this.mGeckoView.releasePointerCapture();
            }
        }
    }

    private void setPointerCaptureListener() {
        if (Build.VERSION.SDK_INT >= 26) {
            if (!this.isGeckoViewRenderEngine.booleanValue()) {
                this.mSystemWebview.setOnCapturedPointerListener(new View.OnCapturedPointerListener() { // from class: com.studio08.xbgamestream.Helpers.PWAWebviewHandler.4
                    @Override // android.view.View.OnCapturedPointerListener
                    public boolean onCapturedPointer(View view, MotionEvent motionEvent) {
                        return PWAWebviewHandler.this.handleMouseMotionEvent(motionEvent);
                    }
                });
            } else {
                this.mGeckoView.setOnCapturedPointerListener(new View.OnCapturedPointerListener() { // from class: com.studio08.xbgamestream.Helpers.PWAWebviewHandler.5
                    @Override // android.view.View.OnCapturedPointerListener
                    public boolean onCapturedPointer(View view, MotionEvent motionEvent) {
                        return PWAWebviewHandler.this.handleMouseMotionEvent(motionEvent);
                    }
                });
            }
        }
    }

    public boolean handleMouseMotionEvent(MotionEvent motionEvent) {
        if (Build.VERSION.SDK_INT >= 26) {
            JSONObject jSONObject = new JSONObject();
            int action = motionEvent.getAction();
            if (action == 12 || action == 11 || action == 0 || action == 1) {
                try {
                    jSONObject.put("buttonState", motionEvent.getButtonState());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (action == 2) {
                try {
                    if (shouldDebounceMotionEvent()) {
                        this.movementXValue += motionEvent.getX();
                        this.movementYValue += motionEvent.getY();
                        return true;
                    }
                    jSONObject.put("movementX", (motionEvent.getX() + this.movementXValue) * 2.0f);
                    jSONObject.put("movementY", (motionEvent.getY() + this.movementYValue) * 2.0f);
                    this.movementXValue = 0.0f;
                    this.movementYValue = 0.0f;
                } catch (JSONException e2) {
                    e2.printStackTrace();
                }
            } else if (action == 8) {
                try {
                    jSONObject.put("deltaY", motionEvent.getAxisValue(9));
                } catch (JSONException e3) {
                    e3.printStackTrace();
                }
            } else {
                Log.e("HERE", "unknown mouse event called" + motionEvent.getAction());
                return true;
            }
            if (!this.isGeckoViewRenderEngine.booleanValue()) {
                ApiClient.callJavaScript(this.mSystemWebview, "setMousePayload", jSONObject.toString());
            } else {
                this.geckoWebviewClient.sendMouseInput(jSONObject);
            }
            return true;
        }
        return false;
    }

    public boolean shouldDebounceMotionEvent() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - this.lastCallTime < 16) {
            return true;
        }
        this.lastCallTime = currentTimeMillis;
        return false;
    }

    public static String getPwaConfigSettings(Context context) {
        boolean z;
        SharedPreferences sharedPreferences = context.getSharedPreferences("SettingsSharedPref", 0);
        EncryptClient encryptClient = new EncryptClient(context);
        JSONObject jSONObject = new JSONObject();
        String string = sharedPreferences.getString("video_fit_key", "cover");
        Integer valueOf = Integer.valueOf(sharedPreferences.getInt("video_vertical_offset_key", 50));
        String string2 = sharedPreferences.getString("emulate_client_key", "windows");
        if (BuildConfig.FLAVOR.equals("tv") || BuildConfig.FLAVOR.equals("firetv")) {
            string2 = sharedPreferences.getString("emulate_client_key", "android");
        }
        String string3 = sharedPreferences.getString("controller_refresh_key", "32");
        String string4 = sharedPreferences.getString("max_bitrate_key", "");
        Boolean valueOf2 = Boolean.valueOf(sharedPreferences.getBoolean("enable_audio_default_key", true));
        String string5 = sharedPreferences.getString("mini_gamepad_size_key", "30");
        int i = sharedPreferences.getInt("tilt_sensitivity_key", 2);
        int i2 = sharedPreferences.getInt("tilt_deadzone_key", 3);
        boolean z2 = sharedPreferences.getBoolean("tilt_invert_x_key", false);
        boolean z3 = sharedPreferences.getBoolean("tilt_invert_y_key", false);
        boolean z4 = sharedPreferences.getBoolean("rumble_controller_key", true);
        int i3 = sharedPreferences.getInt("rumble_intensity_key", 1);
        try {
            jSONObject.put("video-fit", string);
            jSONObject.put("video-vertical-offset", valueOf);
            if (Helper.getActiveCustomPhysicalGamepadMappings(context) != null) {
                jSONObject.put("physical-controller-button-mappings", Helper.getActiveCustomPhysicalGamepadMappings(context));
            }
            jSONObject.put("gamepadRefreshRateMs", string3);
            jSONObject.put("userAgentType", string2);
            jSONObject.put("miniGamepadSize", string5);
            if (!valueOf2.booleanValue()) {
                jSONObject.put("disable-audio", true);
            }
            jSONObject.put("tiltSensitivity", i);
            jSONObject.put("tiltDeadzone", i2);
            if (z2) {
                z = true;
                jSONObject.put("tiltInvertX", true);
            } else {
                z = true;
            }
            if (z3) {
                jSONObject.put("tiltInvertY", z);
            }
            if (!TextUtils.isEmpty(string4)) {
                jSONObject.put("maxBitrate", string4);
            }
            jSONObject.put("rumble_controller", z4);
            jSONObject.put("rumble_intensity", i3);
            jSONObject.put("render_engine", Helper.getRenderEngine(context));
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                jSONObject.put("pwa_prompt_for_xcloud_shortcuts", true);
                jSONObject.put("pwa_prompt_for_xhome_shortcuts", true);
            }
            jSONObject.put("native_app_version", 75);
        } catch (Exception unused) {
        }
        JSONObject jSONObject2 = encryptClient.getJSONObject("xalData");
        JSONObject jSONObject3 = encryptClient.getJSONObject("productPriceData");
        JSONObject jSONObject4 = new JSONObject();
        if (jSONObject2 != null) {
            try {
                jSONObject4.put("xalData", jSONObject2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jSONObject3 != null) {
            jSONObject4.put("productPriceData", jSONObject3);
        }
        jSONObject4.put("configData", jSONObject);
        return jSONObject4.toString();
    }

    public void switchToGeckoView() {
        if (this.mSystemWebview.getParent() != null) {
            this.constraintLayout.removeView(this.mSystemWebview);
        }
        if (this.mGeckoView.getParent() == null) {
            this.constraintLayout.addView(this.mGeckoView, new ConstraintLayout.LayoutParams(-1, -1));
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this.constraintLayout);
            constraintSet.connect(this.mGeckoView.getId(), 3, 0, 3);
            constraintSet.connect(this.mGeckoView.getId(), 4, 0, 4);
            constraintSet.connect(this.mGeckoView.getId(), 6, 0, 6);
            constraintSet.connect(this.mGeckoView.getId(), 7, 0, 7);
            constraintSet.applyTo(this.constraintLayout);
        }
        setPointerCaptureListener();
        this.mGeckoView.requestFocus();
    }

    public void switchToWebView() {
        GeckoWebviewClient geckoWebviewClient = this.geckoWebviewClient;
        if (geckoWebviewClient != null) {
            geckoWebviewClient.loadUrl("about:blank");
        }
        if (this.mGeckoView.getParent() != null) {
            this.constraintLayout.removeView(this.mGeckoView);
        }
        if (this.mSystemWebview.getParent() == null) {
            this.constraintLayout.addView(this.mSystemWebview, new ConstraintLayout.LayoutParams(-1, -1));
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this.constraintLayout);
            constraintSet.connect(this.mSystemWebview.getId(), 3, 0, 3);
            constraintSet.connect(this.mSystemWebview.getId(), 4, 0, 4);
            constraintSet.connect(this.mSystemWebview.getId(), 6, 0, 6);
            constraintSet.connect(this.mSystemWebview.getId(), 7, 0, 7);
            constraintSet.applyTo(this.constraintLayout);
        }
        this.mSystemWebview.requestFocus();
    }

    public void togglePip(boolean z) {
        if (Helper.getRenderEngine(this.mContext).equals("geckoview")) {
            this.geckoWebviewClient.togglePip(z);
        } else {
            ApiClient.callJavaScript(this.mSystemWebview, "togglePip", Boolean.valueOf(z));
        }
    }

    public void sendToggleTVMenuCommand() {
        if (Helper.getRenderEngine(this.mContext).equals("geckoview")) {
            this.geckoWebviewClient.sendToggleTVMenu(new JSONObject());
            return;
        }
        ApiClient.callJavaScript(this.mSystemWebview, "toggleTVMenu", new Object[0]);
    }

    public void cleanUpBeforeDestroy() {
        try {
            StreamWebview streamWebview = this.mSystemWebview;
            if (streamWebview != null) {
                streamWebview.clearHistory();
                this.mSystemWebview.clearCache(false);
                this.mSystemWebview.loadUrl("about:blank");
                this.mSystemWebview.onPause();
                this.mSystemWebview.removeAllViews();
                this.mSystemWebview.destroy();
                this.mSystemWebview.cleanup();
                this.mSystemWebview = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.mSystemWebview = null;
        }
        try {
            GeckoWebviewClient geckoWebviewClient = this.geckoWebviewClient;
            if (geckoWebviewClient != null) {
                geckoWebviewClient.destroy();
                this.geckoWebviewClient = null;
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        this.controllerHandler.destroy();
    }
}
