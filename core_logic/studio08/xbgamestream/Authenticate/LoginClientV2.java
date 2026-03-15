package com.studio08.xbgamestream.Authenticate;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.amazon.a.a.o.b.f;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.studio08.xbgamestream.BuildConfig;
import com.studio08.xbgamestream.Helpers.EncryptClient;
import com.studio08.xbgamestream.Web.StreamWebview;
import com.studio08.xbgamestream.Web.StreamWebviewListener;
import com.tapjoy.TJAdUnitConstants;
import fi.iki.elonen.NanoHTTPD;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.http.client.config.CookieSpecs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: /app/base.apk/classes3.dex */
public class LoginClientV2 {
    private Context context;
    private EncryptClient encryptClient;
    LoginClientListener listener;
    private StreamWebview loginWebview;
    private String LOGIN_URL = "https://account.xbox.com/account/signin?returnUrl=https%3A%2F%2Fwww.xbox.com%2Fen-US%2Fplay&ru=https%3A%2F%2Fwww.xbox.com%2Fen-US%2Fplay";
    private String LOGIN_COMPLETE_URL = "https://www.xbox.com/en-US/play";
    private String AUTH_COOKIE_KEY = "XBXXtkhttp://xboxlive.com";
    private String STREAM_COOKIE_KEY = "XBXXtkhttp://gssv.xboxlive.com";
    private String LOGIN_ENDPOINT = "https://xhome.gssv-play-prod.xboxlive.com/v2/login/user";
    private String XCLOUD_LOGIN_ENDPOINT = "https://xgpuweb.gssv-play-prod.xboxlive.com/v2/login/user";
    private String LIST_CONSOLES_ENDPOINT = "https://uks.gssv-play-prodxhome.xboxlive.com/v6/servers/home";
    private String SIGN_IN_USER_PAGE = "login.live.com";
    private TokenStatus gsTokenStatus = new TokenStatus();
    private TokenStatus consoleStatus = new TokenStatus();
    private TokenStatus xCloudTokenStatus = new TokenStatus();
    private TokenStatus msalTokenStatus = new TokenStatus();
    private int retryAttempts = 0;

    /* loaded from: /app/base.apk/classes3.dex */
    public interface LoginClientListener {
        void genericMessage(String str, String str2);

        void hideDialog();

        void onLoginComplete(String str);

        void showDialog();

        void statusMessage(String str);
    }

    static /* synthetic */ int access$408(LoginClientV2 loginClientV2) {
        int i = loginClientV2.retryAttempts;
        loginClientV2.retryAttempts = i + 1;
        return i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: /app/base.apk/classes3.dex */
    public class TokenStatus {
        public Boolean waiting = true;
        public Boolean completed = false;
        public Boolean failed = false;

        TokenStatus() {
        }

        public void setCompleted() {
            this.completed = true;
            this.failed = false;
            this.waiting = false;
            LoginClientV2.this.emitLoginCompleteIfReady();
        }

        public void setFailed() {
            this.completed = false;
            this.failed = true;
            this.waiting = false;
            LoginClientV2.this.emitLoginCompleteIfReady();
        }

        public String getAllStatuses() {
            return "completed: " + this.completed + " failed: " + this.failed + " waiting: " + this.waiting + "|| ";
        }

        public String getStatus() {
            if (this.waiting.booleanValue()) {
                return "waiting";
            }
            if (this.failed.booleanValue()) {
                return "failed";
            }
            if (this.completed.booleanValue()) {
                return TJAdUnitConstants.String.VIDEO_COMPLETE;
            }
            return "unknown";
        }
    }

    public LoginClientV2(Context context, StreamWebview streamWebview) {
        this.loginWebview = null;
        this.listener = null;
        this.encryptClient = null;
        this.context = context;
        this.listener = null;
        this.loginWebview = streamWebview;
        this.encryptClient = new EncryptClient(this.context);
        setupWebviewListeners();
        resetLogs();
    }

    public void setCustomObjectListener(LoginClientListener loginClientListener) {
        this.listener = loginClientListener;
    }

    public void doLogin() {
        appendLogs("Starting doLogin");
        this.loginWebview.loadUrl(this.LOGIN_URL);
    }

    private void setupWebviewListeners() {
        this.loginWebview.setCustomObjectListener(new StreamWebviewListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV2.1
            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void closeScreen() {
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void onReLoginRequest() {
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void pressButtonWifiRemote(String str) {
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void setOrientationValue(String str) {
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void vibrate() {
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void genericMessage(String str, String str2) {
                if (str.equals("msalTokenSet")) {
                    LoginClientV2.this.appendLogs("Found MSAL Token: " + str2);
                    LoginClientV2.this.encryptClient.saveValue("msalAccessToken", Base64.encodeToString(str2.getBytes(StandardCharsets.UTF_8), 0));
                    LoginClientV2.this.msalTokenStatus.setCompleted();
                }
            }
        });
        this.loginWebview.setWebChromeClient(new AnonymousClass2());
        this.loginWebview.setWebViewClient(new WebViewClient() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV2.3
            @Override // android.webkit.WebViewClient
            public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
                super.onPageStarted(webView, str, bitmap);
                LoginClientV2.this.listener.showDialog();
                LoginClientV2.this.listener.statusMessage("Waiting for: " + str.substring(0, str.indexOf(".com") + 4));
                if (str.toLowerCase().contains(LoginClientV2.this.SIGN_IN_USER_PAGE.toLowerCase())) {
                    LoginClientV2.this.listener.hideDialog();
                }
                LoginClientV2.this.listenForAccessTokenSet();
            }

            @Override // android.webkit.WebViewClient
            public void onPageFinished(WebView webView, String str) {
                super.onPageFinished(webView, str);
                if (!str.toLowerCase().contains(LoginClientV2.this.LOGIN_COMPLETE_URL.toLowerCase())) {
                    if (str.toLowerCase().contains(LoginClientV2.this.SIGN_IN_USER_PAGE.toLowerCase()) || str.startsWith("file")) {
                        LoginClientV2.this.listener.hideDialog();
                        return;
                    }
                    LoginClientV2.this.listener.showDialog();
                    LoginClientV2.this.listener.statusMessage("Waiting for: " + str.substring(0, str.indexOf(".com") + 4));
                    return;
                }
                LoginClientV2 loginClientV2 = LoginClientV2.this;
                String cookie = loginClientV2.getCookie(str, loginClientV2.STREAM_COOKIE_KEY);
                LoginClientV2.this.encryptClient.saveValue("streamCookieRaw", cookie);
                LoginClientV2.this.startPageCompleteTokenExtraction(cookie);
            }

            @Override // android.webkit.WebViewClient
            public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
                LoginClientV2.this.appendLogs("Error: " + webResourceRequest.getUrl());
                if (webResourceError.getErrorCode() != -9) {
                    LoginClientV2.this.appendLogs("Possible Error: " + webResourceError.getErrorCode() + f.c + ((Object) webResourceError.getDescription()));
                } else {
                    Toast.makeText(webView.getContext(), "Network Error. Try again", 0).show();
                    CookieManager.getInstance().removeAllCookies(null);
                    CookieManager.getInstance().flush();
                }
                LoginClientV2.this.listener.hideDialog();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.studio08.xbgamestream.Authenticate.LoginClientV2$2  reason: invalid class name */
    /* loaded from: /app/base.apk/classes3.dex */
    public class AnonymousClass2 extends WebChromeClient {
        AnonymousClass2() {
        }

        @Override // android.webkit.WebChromeClient
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            if (consoleMessage.message().contains("InvalidCountry")) {
                LoginClientV2.this.listener.statusMessage("Invalid location detected. XCloud feature will not work without using a VPN to login...");
            }
            if (consoleMessage.message().contains("Uncaught Error: Script error for \"jquery\"") || consoleMessage.message().contains("Uncaught TypeError: Cannot read properties of undefined (reading 'trim')") || consoleMessage.message().contains("Uncaught TypeError: Cannot read property 'trim' of undefined")) {
                LoginClientV2.this.appendLogs("Microsoft website had an error. Retrying: " + LoginClientV2.this.retryAttempts);
                LoginClientV2.this.listener.statusMessage("Microsoft website had an error. Retrying: " + LoginClientV2.this.retryAttempts + ".\n\nIf this keeps happening you may need to clear the cache.");
                ((Activity) LoginClientV2.this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV2.2.1
                    @Override // java.lang.Runnable
                    public void run() {
                        new Handler().postDelayed(new Runnable() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV2.2.1.1
                            @Override // java.lang.Runnable
                            public void run() {
                                LoginClientV2.access$408(LoginClientV2.this);
                                if (LoginClientV2.this.loginWebview != null) {
                                    LoginClientV2.this.loginWebview.reload();
                                }
                            }
                        }, 2000L);
                    }
                });
            }
            LoginClientV2.this.appendLogs(String.format(Locale.ENGLISH, "%s @ %d: %s", consoleMessage.message(), Integer.valueOf(consoleMessage.lineNumber()), consoleMessage.sourceId()));
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startPageCompleteTokenExtraction(String str) {
        appendLogs("startPageCompleteTokenExtraction");
        exchangeCookieForXcloudToken(str);
        exchangeCookieForGsToken(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void listenForAccessTokenSet() {
        if (getSkipXcloud()) {
            appendLogs("Ignoring msal retrieval because user set skipXcloudLogin to true");
        } else {
            callJavaScriptCode(this.loginWebview, "try{ localStorage.clear(); } catch(err){ console.log(err); } localStorage.setItem = function(key, value) {   console.log('setItem called!', key, value);   try {       let jsonData = JSON.parse(value);       if(jsonData &&           jsonData['credentialType'] &&           jsonData['credentialType'] === 'RefreshToken' &&           jsonData['environment'] == 'login.windows.net' &&           jsonData['secret']){               console.log('msalTokenFound!', key, value);               Android.genericMessage('msalTokenSet', jsonData['secret']);       }   } catch(err) {       console.log('Error', err);   }}");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void emitLoginCompleteIfReady() {
        boolean skipXcloud = getSkipXcloud();
        appendLogs("Attempting to emit onLoginComplete event: " + skipXcloud + " | " + this.gsTokenStatus.getAllStatuses() + this.consoleStatus.getAllStatuses() + this.xCloudTokenStatus.getAllStatuses() + this.msalTokenStatus.getAllStatuses());
        this.listener.statusMessage("GSToken: " + this.gsTokenStatus.getStatus() + "\nxCloudToken: " + this.xCloudTokenStatus.getStatus() + "\nMSALToken: " + this.msalTokenStatus.getStatus() + "\nConsole: " + this.consoleStatus.getStatus());
        if (skipXcloud && this.gsTokenStatus.completed.booleanValue() && this.consoleStatus.completed.booleanValue()) {
            appendLogs("Considering complete because skipXcloudLogin = false");
            this.listener.onLoginComplete(this.encryptClient.getValue("consoles"));
        } else if (this.gsTokenStatus.waiting.booleanValue() || this.consoleStatus.waiting.booleanValue() || this.msalTokenStatus.waiting.booleanValue() || this.xCloudTokenStatus.waiting.booleanValue()) {
            appendLogs("Ignoring onComplete event due to waiting...");
        } else if (this.gsTokenStatus.failed.booleanValue() || this.consoleStatus.failed.booleanValue()) {
            appendLogs("Login Failed!");
            clearTokensAndReLogin(true);
        } else if (this.xCloudTokenStatus.failed.booleanValue() || this.msalTokenStatus.failed.booleanValue()) {
            appendLogs("Login worked but no xCloud!");
            Toast.makeText(this.context, "xCloud feature disabled due to no GamePass subscription", 1).show();
            this.listener.onLoginComplete(this.encryptClient.getValue("consoles"));
        } else {
            appendLogs("Login worked 100%");
            this.listener.onLoginComplete(this.encryptClient.getValue("consoles"));
        }
    }

    private void exchangeCookieForGsToken(final String str) {
        if (this.gsTokenStatus.completed.booleanValue()) {
            appendLogs("Ignoring gsToken retrieval because we already have it");
            return;
        }
        RequestQueue newRequestQueue = Volley.newRequestQueue(this.context);
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("token", new JSONObject(URLDecoder.decode(str, "UTF-8")).getString("Token"));
            jSONObject.put("offeringId", "xhome");
        } catch (Exception e) {
            this.listener.statusMessage("Cannot exchange StreamCookie for GameStream token. AuthError");
            e.printStackTrace();
            this.gsTokenStatus.setFailed();
            this.consoleStatus.setFailed();
        }
        newRequestQueue.add(new JsonObjectRequest(1, this.LOGIN_ENDPOINT, jSONObject, new Response.Listener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV2$$ExternalSyntheticLambda0
            @Override // com.android.volley.Response.Listener
            public final void onResponse(Object obj) {
                LoginClientV2.this.m322xa3042056(str, (JSONObject) obj);
            }
        }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV2$$ExternalSyntheticLambda1
            @Override // com.android.volley.Response.ErrorListener
            public final void onErrorResponse(VolleyError volleyError) {
                LoginClientV2.this.m323xbd1f9ef5(volleyError);
            }
        }));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$exchangeCookieForGsToken$0$com-studio08-xbgamestream-Authenticate-LoginClientV2  reason: not valid java name */
    public /* synthetic */ void m322xa3042056(String str, JSONObject jSONObject) {
        try {
            String string = jSONObject.getString("gsToken");
            this.encryptClient.saveValue("gsToken", string);
            this.encryptClient.saveValue("streamCookieRaw", str);
            this.listener.statusMessage("Created new GameStream token from existing StreamCookie. Validating...");
            this.gsTokenStatus.setCompleted();
            getConsoles(string);
        } catch (JSONException e) {
            this.listener.statusMessage("Cannot exchange StreamCookie for GameStream token. JSONException");
            e.printStackTrace();
            this.gsTokenStatus.setFailed();
            this.consoleStatus.setFailed();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$exchangeCookieForGsToken$1$com-studio08-xbgamestream-Authenticate-LoginClientV2  reason: not valid java name */
    public /* synthetic */ void m323xbd1f9ef5(VolleyError volleyError) {
        this.listener.statusMessage("Cannot exchange StreamCookie for GameStream token. Attempting to regenerate.");
        this.gsTokenStatus.setFailed();
        this.consoleStatus.setFailed();
    }

    private void exchangeCookieForXcloudToken(String str) {
        boolean skipXcloud = getSkipXcloud();
        if (this.xCloudTokenStatus.completed.booleanValue()) {
            appendLogs("Ignoring xcloud retrieval because we already have it");
        } else if (skipXcloud) {
            appendLogs("Ignoring xcloud retrieval because user set skipXcloudLogin to true");
        } else {
            RequestQueue newRequestQueue = Volley.newRequestQueue(this.context);
            JSONObject jSONObject = new JSONObject();
            try {
                jSONObject.put("token", new JSONObject(URLDecoder.decode(str, "UTF-8")).getString("Token"));
                jSONObject.put("offeringId", "xgpuweb");
                newRequestQueue.add(new JsonObjectRequest(1, this.XCLOUD_LOGIN_ENDPOINT, jSONObject, new Response.Listener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV2$$ExternalSyntheticLambda2
                    @Override // com.android.volley.Response.Listener
                    public final void onResponse(Object obj) {
                        LoginClientV2.this.m324xd2b62765((JSONObject) obj);
                    }
                }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV2$$ExternalSyntheticLambda3
                    @Override // com.android.volley.Response.ErrorListener
                    public final void onErrorResponse(VolleyError volleyError) {
                        LoginClientV2.this.m325xecd1a604(volleyError);
                    }
                }) { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV2.4
                    @Override // com.android.volley.Request
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap hashMap = new HashMap();
                        hashMap.put("Content-Type", "application/json; charset=utf-8");
                        hashMap.put("x-gssv-client", "XboxComBrowser");
                        return hashMap;
                    }
                });
            } catch (Exception e) {
                this.listener.statusMessage("Error pulling streamCookieRaw... Exploding...");
                e.printStackTrace();
                this.xCloudTokenStatus.setFailed();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$exchangeCookieForXcloudToken$2$com-studio08-xbgamestream-Authenticate-LoginClientV2  reason: not valid java name */
    public /* synthetic */ void m324xd2b62765(JSONObject jSONObject) {
        try {
            String string = this.context.getSharedPreferences("SettingsSharedPref", 0).getString("region_key", CookieSpecs.DEFAULT);
            String string2 = jSONObject.getString("gsToken");
            this.encryptClient.saveValue("xcloudToken", string2);
            JSONArray jSONArray = jSONObject.getJSONObject("offeringSettings").getJSONArray("regions");
            String str = "";
            int i = 0;
            while (true) {
                if (i >= jSONArray.length()) {
                    break;
                }
                JSONObject jSONObject2 = jSONArray.getJSONObject(i);
                if (!string.equals(CookieSpecs.DEFAULT) && jSONObject2.getString("name").equals(string)) {
                    appendLogs("Saving custom region");
                    this.encryptClient.saveValue("xcloudRegion", jSONObject2.getString("baseUri").substring(8));
                    str = jSONObject2.getString("name");
                    Toast.makeText(this.context, "Using custom region: " + str, 0).show();
                    break;
                }
                if (jSONObject2.getBoolean("isDefault")) {
                    appendLogs("Saving default region");
                    this.encryptClient.saveValue("xcloudRegion", jSONObject2.getString("baseUri").substring(8));
                    str = jSONObject2.getString("name");
                }
                i++;
            }
            appendLogs("Got xCloud token: " + string2 + " - " + str);
            this.listener.statusMessage("Got xCloud token! Using region" + str);
            this.xCloudTokenStatus.setCompleted();
        } catch (JSONException e) {
            this.listener.statusMessage("Cannot exchange StreamCookie for xCloud token. JSONException");
            e.printStackTrace();
            this.xCloudTokenStatus.setFailed();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$exchangeCookieForXcloudToken$3$com-studio08-xbgamestream-Authenticate-LoginClientV2  reason: not valid java name */
    public /* synthetic */ void m325xecd1a604(VolleyError volleyError) {
        this.listener.statusMessage("Cannot exchange StreamCookie for xCloud token... Attempting to regenerate.");
        this.encryptClient.saveValue("xcloudToken", null);
        this.encryptClient.saveValue("xcloudRegion", null);
        this.xCloudTokenStatus.setFailed();
        if ((!BuildConfig.FLAVOR.equals(BuildConfig.FLAVOR) && !BuildConfig.FLAVOR.equals("legacy") && !BuildConfig.FLAVOR.equals("stream") && !BuildConfig.FLAVOR.equals("tv")) || volleyError == null || volleyError.networkResponse == null || volleyError.networkResponse.data == null) {
            return;
        }
        try {
            String str = new String(volleyError.networkResponse.data, "UTF-8");
            appendLogs(str);
            if (str.contains("InvalidCountry")) {
                this.listener.statusMessage("Invalid country code detected. XCloud feature will not work.");
                Toast.makeText(this.context, "Invalid location detected. XCloud feature will not work. Consider using a VPN to login if you plan to use XCloud.", 1).show();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void getConsoles(final String str) {
        if (this.consoleStatus.completed.booleanValue()) {
            appendLogs("Ignoring console retrieval because we already have it");
        } else if (TextUtils.isEmpty(str)) {
            appendLogs("Ignoring validateGsToken because string null");
            this.consoleStatus.setFailed();
        } else {
            Volley.newRequestQueue(this.context).add(new StringRequest(0, this.LIST_CONSOLES_ENDPOINT, new Response.Listener<String>() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV2.5
                @Override // com.android.volley.Response.Listener
                public void onResponse(String str2) {
                    LoginClientV2.this.listener.statusMessage("Got console data... Please wait.");
                    LoginClientV2.this.encryptClient.saveValue("gsToken", str);
                    LoginClientV2.this.encryptClient.saveValue("consoles", str2);
                    LoginClientV2.this.appendLogs("console response is: " + str2);
                    LoginClientV2.this.appendLogs("gsToken is: " + str);
                    LoginClientV2.this.consoleStatus.setCompleted();
                }
            }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV2.6
                @Override // com.android.volley.Response.ErrorListener
                public void onErrorResponse(VolleyError volleyError) {
                    LoginClientV2.this.listener.statusMessage("Failed to get console data");
                    LoginClientV2.this.consoleStatus.setFailed();
                }
            }) { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV2.7
                @Override // com.android.volley.Request
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap hashMap = new HashMap();
                    hashMap.put("Authorization", "Bearer " + str);
                    hashMap.put("Content-Type", "application/json");
                    return hashMap;
                }
            });
        }
    }

    private void clearTokensAndReLogin(Boolean bool) {
        this.listener.statusMessage("Clearing cached tokens and prompting user for ReLogin");
        appendLogs("clearTokensAndReLogin " + bool);
        if (bool.booleanValue()) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
            this.loginWebview.loadDataWithBaseURL("https://www.xbox.com?custom_refresh", "<script type='text/javascript'>localStorage.clear();window.location.href = 'https://google.com';</script>", NanoHTTPD.MIME_HTML, "utf-8", null);
        }
        this.encryptClient.saveValue("gsToken", "");
        this.encryptClient.saveValue("streamCookieRaw", "");
        this.encryptClient.saveValue("xcloudToken", "");
        this.encryptClient.saveValue("xcloudRegion", "");
        this.encryptClient.saveValue("msalAccessToken", "");
        this.encryptClient.saveValue("msalRefreshToken", "");
        this.encryptClient.saveValue("clientId", "");
        this.encryptClient.saveValue("consoles", "");
        if (bool.booleanValue()) {
            return;
        }
        ((Activity) this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV2.8
            @Override // java.lang.Runnable
            public void run() {
                LoginClientV2.this.doLogin();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void appendLogs(String str) {
        Log.w("HERE!", str);
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("SettingsSharedPref", 0);
        if (sharedPreferences.getBoolean("capture_debug_logs_key", false)) {
            String string = sharedPreferences.getString("login_logs", "");
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString("login_logs", string + "\n" + str);
            edit.apply();
        }
    }

    private void resetLogs() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("SettingsSharedPref", 0);
        if (sharedPreferences.getBoolean("capture_debug_logs_key", false)) {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString("login_logs", "");
            edit.apply();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getCookie(String str, String str2) {
        String[] split;
        for (String str3 : CookieManager.getInstance().getCookie(str).split(";")) {
            if (str3.contains(str2)) {
                return str3.split(f.b)[1];
            }
        }
        return null;
    }

    boolean getSkipXcloud() {
        if (BuildConfig.FLAVOR.equals("gamepadController")) {
            appendLogs("Skipping xcloud login because using gamepadController flavor");
            return true;
        }
        return this.context.getSharedPreferences("SettingsSharedPref", 0).getBoolean("skip_xcloud_login_key", false);
    }

    private void callJavaScriptCode(WebView webView, String str) {
        webView.evaluateJavascript(str, new ValueCallback<String>() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV2.9
            @Override // android.webkit.ValueCallback
            public void onReceiveValue(String str2) {
            }
        });
    }
}
