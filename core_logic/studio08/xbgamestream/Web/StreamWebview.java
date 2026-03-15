package com.studio08.xbgamestream.Web;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import com.studio08.xbgamestream.BuildConfig;
import com.studio08.xbgamestream.Helpers.Helper;
import com.studio08.xbgamestream.Web.StreamWebview;
import java.util.Locale;
/* loaded from: /app/base.apk/classes3.dex */
public class StreamWebview extends WebView {
    public Boolean captureLogs;
    private Context context;
    public StreamWebviewListener listener;
    public Boolean showLoadingDialog;
    private CustomWebClient webClient;

    public StreamWebview(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = null;
        this.showLoadingDialog = true;
        this.captureLogs = false;
        this.context = context;
    }

    public StreamWebview(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.context = null;
        this.showLoadingDialog = true;
        this.captureLogs = false;
        this.context = context;
    }

    public StreamWebview(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.context = null;
        this.showLoadingDialog = true;
        this.captureLogs = false;
        this.context = context;
    }

    public StreamWebview(Context context) {
        super(context);
        this.context = null;
        this.showLoadingDialog = true;
        this.captureLogs = false;
        this.context = context;
    }

    public void setCustomObjectListener(StreamWebviewListener streamWebviewListener) {
        this.listener = streamWebviewListener;
    }

    public void init() {
        resetLogs();
        if (this.context.getSharedPreferences("SettingsSharedPref", 0).getBoolean("capture_debug_logs_gameplay_key", false)) {
            this.captureLogs = true;
        }
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(false);
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportMultipleWindows(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptThirdPartyCookies(this, true);
        cookieManager.setAcceptCookie(true);
        if ("release".equals("debug")) {
            setWebContentsDebuggingEnabled(true);
        }
        setWebChromeClient(new CustomWebChromeClient());
        addJavascriptInterface(new WebAppInterface(this.context), "Android");
        CustomWebClient customWebClient = new CustomWebClient(true);
        this.webClient = customWebClient;
        setWebViewClient(customWebClient);
        setLongClickable(false);
        setOnLongClickListener(new View.OnLongClickListener() { // from class: com.studio08.xbgamestream.Web.StreamWebview.1
            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View view) {
                return true;
            }
        });
    }

    public void disableLoadingDialog() {
        this.showLoadingDialog = false;
        CustomWebClient customWebClient = new CustomWebClient(false);
        this.webClient = customWebClient;
        setWebViewClient(customWebClient);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: /app/base.apk/classes3.dex */
    public class CustomWebChromeClient extends WebChromeClient {
        CustomWebChromeClient() {
        }

        @Override // android.webkit.WebChromeClient
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            if (StreamWebview.this.captureLogs.booleanValue()) {
                Log.e("CONSOLE", String.format("%s @ %d: %s", consoleMessage.message(), Integer.valueOf(consoleMessage.lineNumber()), consoleMessage.sourceId()));
                StreamWebview.this.appendLogs(String.format(Locale.ENGLISH, "%s @ %d: %s", consoleMessage.message(), Integer.valueOf(consoleMessage.lineNumber()), consoleMessage.sourceId()));
                return true;
            }
            return true;
        }

        @Override // android.webkit.WebChromeClient
        public void onPermissionRequest(final PermissionRequest permissionRequest) {
            ((Activity) StreamWebview.this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Web.StreamWebview$CustomWebChromeClient$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    StreamWebview.CustomWebChromeClient.this.m348xb2dc8dc2(permissionRequest);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: lambda$onPermissionRequest$0$com-studio08-xbgamestream-Web-StreamWebview$CustomWebChromeClient  reason: not valid java name */
        public /* synthetic */ void m348xb2dc8dc2(PermissionRequest permissionRequest) {
            if (!Helper.checkIfAlreadyHavePermission("android.permission.RECORD_AUDIO", StreamWebview.this.context)) {
                Toast.makeText(StreamWebview.this.context, "Grant Permissions and Retry", 0).show();
                Helper.requestForSpecificPermission(new String[]{"android.permission.RECORD_AUDIO"}, StreamWebview.this.context);
            } else {
                Log.e("HERE", "Already have audio perm");
            }
            permissionRequest.grant(new String[]{"android.webkit.resource.AUDIO_CAPTURE", "android.webkit.resource.VIDEO_CAPTURE"});
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void appendLogs(String str) {
        Log.w("log_append", str);
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("SettingsSharedPref", 0);
        String string = sharedPreferences.getString("gameplay_logs", "");
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("gameplay_logs", string + "\n" + str);
        edit.apply();
    }

    private void resetLogs() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("SettingsSharedPref", 0);
        if (sharedPreferences.getBoolean("capture_debug_logs_gameplay_key", false)) {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString("gameplay_logs", "");
            edit.apply();
        }
    }

    public void cleanup() {
        CustomWebClient customWebClient = this.webClient;
        if (customWebClient != null) {
            customWebClient.cleanup();
        }
    }

    /* loaded from: /app/base.apk/classes3.dex */
    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context context) {
            this.mContext = context;
        }

        @JavascriptInterface
        public void showToast(String str) {
            if (BuildConfig.FLAVOR.equals("tv") && str.contains("\"refresh\"")) {
                Toast.makeText(this.mContext, "Long press the BACK button to refresh the video if it gets stuck", 1).show();
            } else {
                Toast.makeText(this.mContext, str, 1).show();
            }
        }

        @JavascriptInterface
        public void reLoginRequest() {
            StreamWebview.this.listener.onReLoginRequest();
        }

        @JavascriptInterface
        public void closeScreen() {
            StreamWebview.this.listener.closeScreen();
        }

        @JavascriptInterface
        public void pressButtonWifiRemote(String str, int i) {
            Log.e("StreamWebview", "Caught pressButtonWifiRemote");
            StreamWebview.this.listener.pressButtonWifiRemote(str);
        }

        @JavascriptInterface
        public void setOrientationValue(String str) {
            StreamWebview.this.listener.setOrientationValue(str);
        }

        @JavascriptInterface
        public void vibrate() {
            StreamWebview.this.listener.vibrate();
        }

        @JavascriptInterface
        public String genericMessage(String str, String str2) {
            StreamWebview.this.listener.genericMessage(str, str2);
            if (str.equals("version_code")) {
                Log.e("HERE", "Got version code: 4.46");
                return BuildConfig.VERSION_NAME;
            }
            return null;
        }
    }
}
