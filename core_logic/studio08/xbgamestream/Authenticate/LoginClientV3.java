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
import com.google.common.net.HttpHeaders;
import com.studio08.xbgamestream.BuildConfig;
import com.studio08.xbgamestream.Helpers.EncryptClient;
import com.studio08.xbgamestream.Web.StreamWebview;
import com.studio08.xbgamestream.Web.StreamWebviewListener;
import com.tapjoy.TJAdUnitConstants;
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
public class LoginClientV3 {
    private Context context;
    private EncryptClient encryptClient;
    LoginClientListener listener;
    private StreamWebview loginWebview;
    private String LOGIN_URL = "https://www.xbox.com/en-US/auth/msa?action=logIn&returnUrl=https%3A%2F%2Fwww.xbox.com%2Fen-US%2Fplay";
    private String AUTH_COOKIE_KEY = "XBXXtkhttp://xboxlive.com";
    private String STREAM_COOKIE_KEY = "XBXXtkhttp://gssv.xboxlive.com";
    private String LOGIN_ENDPOINT = "https://xhome.gssv-play-prod.xboxlive.com/v2/login/user";
    private String XCLOUD_LOGIN_ENDPOINT = "https://xgpuweb.gssv-play-prod.xboxlive.com/v2/login/user";
    private String LIST_CONSOLES_ENDPOINT = "https://uks.core.gssv-play-prodxhome.xboxlive.com/v6/servers/home";
    private String SIGN_IN_USER_PAGE = "login.live.com";
    private String AUTH_LOADING_PAGE = "https://www.xbox.com/en-US/auth/msa";
    private TokenStatus gsTokenStatus = new TokenStatus();
    private TokenStatus consoleStatus = new TokenStatus();
    private TokenStatus xCloudTokenStatus = new TokenStatus();
    private TokenStatus msalTokenStatus = new TokenStatus();
    private int retryAttempts = 0;
    public boolean pollerRunning = false;

    /* loaded from: /app/base.apk/classes3.dex */
    public interface LoginClientListener {
        void genericMessage(String str, String str2);

        void hideDialog();

        void onLoginComplete(String str);

        void showDialog();

        void statusMessage(String str);
    }

    static /* synthetic */ int access$108(LoginClientV3 loginClientV3) {
        int i = loginClientV3.retryAttempts;
        loginClientV3.retryAttempts = i + 1;
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
            LoginClientV3.this.emitLoginCompleteIfReady();
        }

        public void setFailed() {
            this.completed = false;
            this.failed = true;
            this.waiting = false;
            LoginClientV3.this.emitLoginCompleteIfReady();
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

    public LoginClientV3(Context context, StreamWebview streamWebview) {
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
        this.loginWebview.setCustomObjectListener(new StreamWebviewListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV3.1
            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void closeScreen() {
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void genericMessage(String str, String str2) {
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
        });
        this.loginWebview.setWebChromeClient(new AnonymousClass2());
        this.loginWebview.setWebViewClient(new WebViewClient() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV3.3
            @Override // android.webkit.WebViewClient
            public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
                super.onPageStarted(webView, str, bitmap);
                Log.e("HERE", "Page started:" + str);
                LoginClientV3.this.listener.showDialog();
                LoginClientV3.this.listener.statusMessage("Waiting for: " + str.substring(0, str.indexOf(".com") + 4));
                if (str.toLowerCase().contains(LoginClientV3.this.SIGN_IN_USER_PAGE.toLowerCase()) || (str.toLowerCase().contains(LoginClientV3.this.AUTH_LOADING_PAGE.toLowerCase()) && !str.toLowerCase().contains("https://www.xbox.com/en-us/auth/msa?action=loggedin&locale_hint=en-us"))) {
                    LoginClientV3.this.listener.hideDialog();
                }
            }

            @Override // android.webkit.WebViewClient
            public void onPageFinished(WebView webView, String str) {
                super.onPageFinished(webView, str);
                Log.e("HERE", "Page Finished:" + str);
                if (str.toLowerCase().contains(LoginClientV3.this.SIGN_IN_USER_PAGE.toLowerCase()) || (str.toLowerCase().contains(LoginClientV3.this.AUTH_LOADING_PAGE.toLowerCase()) && !str.toLowerCase().contains("https://www.xbox.com/en-us/auth/msa?action=loggedin&locale_hint=en-us"))) {
                    LoginClientV3.this.listener.hideDialog();
                } else {
                    LoginClientV3.this.listener.showDialog();
                    LoginClientV3.this.listener.statusMessage("Waiting for: " + str.substring(0, str.indexOf(".com") + 4));
                }
                LoginClientV3.this.startPollingForTokens();
                LoginClientV3.this.setupPrefillHelper();
            }

            @Override // android.webkit.WebViewClient
            public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
                LoginClientV3.this.appendLogs("Error: " + webResourceRequest.getUrl());
                LoginClientV3.this.appendLogs("Error: " + LoginClientV3.this.loginWebview.getUrl());
                if (webResourceError.getErrorCode() != -9) {
                    LoginClientV3.this.appendLogs("Possible Error: " + webResourceError.getErrorCode() + f.c + ((Object) webResourceError.getDescription()));
                } else {
                    Toast.makeText(webView.getContext(), "Network Error. Try again", 0).show();
                    CookieManager.getInstance().removeAllCookies(null);
                    CookieManager.getInstance().flush();
                }
                LoginClientV3.this.listener.hideDialog();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.studio08.xbgamestream.Authenticate.LoginClientV3$2  reason: invalid class name */
    /* loaded from: /app/base.apk/classes3.dex */
    public class AnonymousClass2 extends WebChromeClient {
        AnonymousClass2() {
        }

        @Override // android.webkit.WebChromeClient
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            if (consoleMessage.message().contains("InvalidCountry")) {
                LoginClientV3.this.listener.statusMessage("Invalid location detected. XCloud feature will not work without using a VPN to login...");
            }
            if (consoleMessage.message().contains("Uncaught Error: Script error for \"jquery\"") || consoleMessage.message().contains("Uncaught TypeError: Cannot read properties of undefined (reading 'trim')") || consoleMessage.message().contains("Uncaught TypeError: Cannot read property 'trim' of undefined")) {
                LoginClientV3.this.appendLogs("Microsoft website had an error. Retrying: " + LoginClientV3.this.retryAttempts);
                LoginClientV3.this.listener.statusMessage("Microsoft website had an error. Retrying: " + LoginClientV3.this.retryAttempts + ".\n\nIf this keeps happening you may need to clear the cache.");
                ((Activity) LoginClientV3.this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV3.2.1
                    @Override // java.lang.Runnable
                    public void run() {
                        new Handler().postDelayed(new Runnable() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV3.2.1.1
                            @Override // java.lang.Runnable
                            public void run() {
                                LoginClientV3.access$108(LoginClientV3.this);
                                if (LoginClientV3.this.loginWebview != null) {
                                    LoginClientV3.this.loginWebview.reload();
                                }
                            }
                        }, 2000L);
                    }
                });
            }
            LoginClientV3.this.appendLogs(String.format(Locale.ENGLISH, "%s @ %d: %s", consoleMessage.message(), Integer.valueOf(consoleMessage.lineNumber()), consoleMessage.sourceId()));
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startPollingForTokens() {
        if (this.pollerRunning) {
            return;
        }
        this.pollerRunning = true;
        final Handler handler = new Handler();
        handler.post(new Runnable() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV3.4
            @Override // java.lang.Runnable
            public void run() {
                if (!LoginClientV3.this.pollerRunning || LoginClientV3.this.loginWebview == null) {
                    LoginClientV3.this.appendLogs("killing pollerRunning");
                    LoginClientV3.this.pollerRunning = false;
                    return;
                }
                if (LoginClientV3.this.gsTokenStatus.getStatus().equals(TJAdUnitConstants.String.VIDEO_COMPLETE)) {
                    LoginClientV3.this.appendLogs("gsToken complete, attempt to pull msal token");
                    LoginClientV3.this.getMsalToken();
                } else {
                    LoginClientV3.this.appendLogs("gsToken not complete, attempt to pull cookie token");
                    LoginClientV3.this.startPageCompleteTokenExtraction();
                }
                handler.postDelayed(this, 3000L);
            }
        });
    }

    public void startPageCompleteTokenExtraction() {
        appendLogs("startPageCompleteTokenExtraction");
        StreamWebview streamWebview = this.loginWebview;
        if (streamWebview != null && streamWebview.getUrl() != null) {
            String cookie = getCookie(this.loginWebview.getUrl(), this.STREAM_COOKIE_KEY);
            this.encryptClient.saveValue("streamCookieRaw", cookie);
            exchangeCookieForGsToken(cookie);
            return;
        }
        appendLogs("loginWebview or getUrl null");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void emitLoginCompleteIfReady() {
        boolean skipXcloud = getSkipXcloud();
        appendLogs("Attempting to emit onLoginComplete event: " + skipXcloud + " | " + this.gsTokenStatus.getAllStatuses() + this.consoleStatus.getAllStatuses() + this.xCloudTokenStatus.getAllStatuses() + this.msalTokenStatus.getAllStatuses());
        StreamWebview streamWebview = this.loginWebview;
        if (streamWebview != null && streamWebview.getUrl() != null && this.loginWebview.getUrl().toLowerCase().contains(this.SIGN_IN_USER_PAGE.toLowerCase())) {
            appendLogs("not showing status update while on 2fa page");
        } else {
            this.listener.statusMessage("GSToken: " + this.gsTokenStatus.getStatus() + "\nxCloudToken: " + this.xCloudTokenStatus.getStatus() + "\nMSALToken: " + this.msalTokenStatus.getStatus() + "\nConsole: " + this.consoleStatus.getStatus());
        }
        if (skipXcloud && this.gsTokenStatus.completed.booleanValue() && this.consoleStatus.completed.booleanValue()) {
            appendLogs("Considering complete because skipXcloudLogin = false");
            this.listener.onLoginComplete(this.encryptClient.getValue("consoles"));
        } else if (this.gsTokenStatus.waiting.booleanValue() || this.consoleStatus.waiting.booleanValue() || this.msalTokenStatus.waiting.booleanValue() || this.xCloudTokenStatus.waiting.booleanValue()) {
            appendLogs("Ignoring onComplete event due to waiting...");
        } else if (this.gsTokenStatus.failed.booleanValue() || this.consoleStatus.failed.booleanValue()) {
            appendLogs("Login gsTokenStatus or consoleStatus failed");
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
        String str2;
        if (this.gsTokenStatus.completed.booleanValue()) {
            appendLogs("Ignoring gsToken retrieval because we already have it");
        } else if (str == null) {
            appendLogs("Ignoring gsToken retrieval because streamCookieRaw is null");
        } else {
            RequestQueue newRequestQueue = Volley.newRequestQueue(this.context);
            JSONObject jSONObject = new JSONObject();
            Log.e("LOG", str);
            try {
                JSONObject jSONObject2 = new JSONObject(URLDecoder.decode(str, "UTF-8"));
                jSONObject.put("offeringId", "xhome");
                try {
                    str2 = jSONObject2.getJSONObject("tokenData").getString("token");
                } catch (Exception unused) {
                    str2 = null;
                }
                if (str2 == null) {
                    str2 = jSONObject2.getString("Token");
                }
                jSONObject.put("token", str2);
            } catch (Exception e) {
                e.printStackTrace();
                this.gsTokenStatus.setFailed();
                this.consoleStatus.setFailed();
            }
            newRequestQueue.add(new JsonObjectRequest(1, this.LOGIN_ENDPOINT, jSONObject, new Response.Listener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV3$$ExternalSyntheticLambda0
                @Override // com.android.volley.Response.Listener
                public final void onResponse(Object obj) {
                    LoginClientV3.this.m326xa3042057(str, (JSONObject) obj);
                }
            }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV3$$ExternalSyntheticLambda1
                @Override // com.android.volley.Response.ErrorListener
                public final void onErrorResponse(VolleyError volleyError) {
                    LoginClientV3.this.m327xbd1f9ef6(volleyError);
                }
            }));
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$exchangeCookieForGsToken$0$com-studio08-xbgamestream-Authenticate-LoginClientV3  reason: not valid java name */
    public /* synthetic */ void m326xa3042057(String str, JSONObject jSONObject) {
        try {
            String string = jSONObject.getString("gsToken");
            this.encryptClient.saveValue("gsToken", string);
            this.encryptClient.saveValue("streamCookieRaw", str);
            this.gsTokenStatus.setCompleted();
            getConsoles(string);
            exchangeCookieForXcloudToken(str, null);
        } catch (JSONException e) {
            e.printStackTrace();
            this.gsTokenStatus.setFailed();
            this.consoleStatus.setFailed();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$exchangeCookieForGsToken$1$com-studio08-xbgamestream-Authenticate-LoginClientV3  reason: not valid java name */
    public /* synthetic */ void m327xbd1f9ef6(VolleyError volleyError) {
        this.gsTokenStatus.setFailed();
        this.consoleStatus.setFailed();
    }

    private void exchangeCookieForXcloudToken(final String str, final String str2) {
        String str3;
        boolean skipXcloud = getSkipXcloud();
        if (this.xCloudTokenStatus.completed.booleanValue()) {
            appendLogs("Ignoring xcloud retrieval because we already have it");
        } else if (skipXcloud) {
            appendLogs("Ignoring xcloud retrieval because user set skipXcloudLogin to true");
        } else if (str == null) {
            appendLogs("Ignoring xcloud retrieval because user streamCookieRaw is null");
        } else {
            RequestQueue newRequestQueue = Volley.newRequestQueue(this.context);
            JSONObject jSONObject = new JSONObject();
            try {
                JSONObject jSONObject2 = new JSONObject(URLDecoder.decode(str, "UTF-8"));
                jSONObject.put("offeringId", "xgpuweb");
                try {
                    str3 = jSONObject2.getJSONObject("tokenData").getString("token");
                } catch (Exception unused) {
                    str3 = null;
                }
                if (str3 == null) {
                    str3 = jSONObject2.getString("Token");
                }
                jSONObject.put("token", str3);
                final String string = this.context.getSharedPreferences("SettingsSharedPref", 0).getString("region_key", CookieSpecs.DEFAULT);
                newRequestQueue.add(new JsonObjectRequest(1, this.XCLOUD_LOGIN_ENDPOINT, jSONObject, new Response.Listener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV3$$ExternalSyntheticLambda2
                    @Override // com.android.volley.Response.Listener
                    public final void onResponse(Object obj) {
                        LoginClientV3.this.m328xd2b62766(string, str2, (JSONObject) obj);
                    }
                }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV3$$ExternalSyntheticLambda3
                    @Override // com.android.volley.Response.ErrorListener
                    public final void onErrorResponse(VolleyError volleyError) {
                        LoginClientV3.this.m329xecd1a605(str2, string, str, volleyError);
                    }
                }) { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV3.5
                    @Override // com.android.volley.Request
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap hashMap = new HashMap();
                        hashMap.put("Content-Type", "application/json; charset=utf-8");
                        hashMap.put("x-gssv-client", "XboxComBrowser");
                        String str4 = str2;
                        if (str4 != null) {
                            hashMap.put(HttpHeaders.X_FORWARDED_FOR, str4);
                        }
                        return hashMap;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                this.xCloudTokenStatus.setFailed();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$exchangeCookieForXcloudToken$2$com-studio08-xbgamestream-Authenticate-LoginClientV3  reason: not valid java name */
    public /* synthetic */ void m328xd2b62766(String str, String str2, JSONObject jSONObject) {
        try {
            String string = jSONObject.getString("gsToken");
            this.encryptClient.saveValue("xcloudToken", string);
            JSONArray jSONArray = jSONObject.getJSONObject("offeringSettings").getJSONArray("regions");
            String str3 = "";
            int i = 0;
            while (true) {
                if (i >= jSONArray.length()) {
                    break;
                }
                JSONObject jSONObject2 = jSONArray.getJSONObject(i);
                if (!str.equals(CookieSpecs.DEFAULT) && jSONObject2.getString("name").equals(str)) {
                    appendLogs("Saving custom region");
                    this.encryptClient.saveValue("xcloudRegion", jSONObject2.getString("baseUri").substring(8));
                    str3 = jSONObject2.getString("name");
                    Toast.makeText(this.context, "Using custom region: " + str3, 0).show();
                    break;
                }
                if (jSONObject2.getBoolean("isDefault")) {
                    appendLogs("Saving default region");
                    this.encryptClient.saveValue("xcloudRegion", jSONObject2.getString("baseUri").substring(8));
                    str3 = jSONObject2.getString("name");
                }
                i++;
            }
            appendLogs("Got xCloud token: " + string + " - " + str3);
            if (str2 != null) {
                appendLogs("Recovered from invalid country error: " + str2);
            }
            this.xCloudTokenStatus.setCompleted();
        } catch (JSONException e) {
            e.printStackTrace();
            this.xCloudTokenStatus.setFailed();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$exchangeCookieForXcloudToken$3$com-studio08-xbgamestream-Authenticate-LoginClientV3  reason: not valid java name */
    public /* synthetic */ void m329xecd1a605(String str, String str2, String str3, VolleyError volleyError) {
        this.encryptClient.saveValue("xcloudToken", null);
        this.encryptClient.saveValue("xcloudRegion", null);
        if (BuildConfig.FLAVOR.equals(BuildConfig.FLAVOR) || BuildConfig.FLAVOR.equals("legacy") || BuildConfig.FLAVOR.equals("stream") || BuildConfig.FLAVOR.equals("tv") || BuildConfig.FLAVOR.equals("vr")) {
            Boolean bool = false;
            if (volleyError != null && volleyError.networkResponse != null && volleyError.networkResponse.data != null && str == null) {
                String str4 = new String(volleyError.networkResponse.data, StandardCharsets.UTF_8);
                appendLogs(str4);
                if (str4.contains("InvalidCountry")) {
                    if (str2.equals(CookieSpecs.DEFAULT)) {
                        appendLogs("not attempting to spoof location");
                        Toast.makeText(this.context, "Invalid location. Set an xCloud region in the settings and re-login!", 1).show();
                    } else {
                        appendLogs("attempting to spoof location");
                        String ipFromCustomRegion = getIpFromCustomRegion(str2);
                        if (ipFromCustomRegion != null) {
                            bool = true;
                            exchangeCookieForXcloudToken(str3, ipFromCustomRegion);
                        } else {
                            appendLogs("Invalid custom xCloud Region! Choose a different region in the settings " + str2);
                            Toast.makeText(this.context, "Invalid custom xCloud Region! Choose a different region in the settings " + str2, 1).show();
                        }
                    }
                }
            }
            if (bool.booleanValue()) {
                return;
            }
            this.xCloudTokenStatus.setFailed();
        }
    }

    private String getIpFromCustomRegion(String str) {
        if (str.equals(CookieSpecs.DEFAULT)) {
            return null;
        }
        if (str.contains("Australia")) {
            return "203.41.44.20";
        }
        if (str.contains("Brazil")) {
            return "200.221.11.101";
        }
        if (str.contains("Europe") || str.contains("UK")) {
            return "194.25.0.68";
        }
        if (str.contains("Japan")) {
            return "122.1.0.154";
        }
        if (str.contains("Korea")) {
            return "203.253.64.1";
        }
        if (str.contains("US") || str.contains("Us")) {
            return "4.2.2.2";
        }
        return null;
    }

    private void getConsoles(final String str) {
        if (this.consoleStatus.completed.booleanValue()) {
            appendLogs("Ignoring console retrieval because we already have it");
        } else if (TextUtils.isEmpty(str)) {
            appendLogs("Ignoring validateGsToken because string null");
            this.consoleStatus.setFailed();
        } else {
            Volley.newRequestQueue(this.context).add(new StringRequest(0, this.LIST_CONSOLES_ENDPOINT, new Response.Listener<String>() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV3.6
                @Override // com.android.volley.Response.Listener
                public void onResponse(String str2) {
                    LoginClientV3.this.encryptClient.saveValue("gsToken", str);
                    LoginClientV3.this.encryptClient.saveValue("consoles", str2);
                    LoginClientV3.this.appendLogs("console response is: " + str2);
                    LoginClientV3.this.appendLogs("gsToken is: " + str);
                    LoginClientV3.this.consoleStatus.setCompleted();
                }
            }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV3.7
                @Override // com.android.volley.Response.ErrorListener
                public void onErrorResponse(VolleyError volleyError) {
                    LoginClientV3.this.consoleStatus.setFailed();
                }
            }) { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV3.8
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
        }
        this.encryptClient.saveValue("gsToken", "");
        this.encryptClient.saveValue("streamCookieRaw", "");
        this.encryptClient.saveValue("xcloudToken", "");
        this.encryptClient.saveValue("xcloudRegion", "");
        this.encryptClient.saveValue("msalAccessToken", "");
        this.encryptClient.saveValue("msalRefreshToken", "");
        this.encryptClient.saveValue("clientId", "");
        this.encryptClient.saveValue("consoles", "");
        ((Activity) this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV3.9
            @Override // java.lang.Runnable
            public void run() {
                LoginClientV3.this.doLogin();
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

    private String getCookie(String str, String str2) {
        String str3;
        String str4;
        String cookie = CookieManager.getInstance().getCookie(str);
        if (cookie == null || cookie.length() <= 0) {
            return null;
        }
        for (String str5 : cookie.split(";")) {
            try {
                String[] split = str5.split(f.b);
                str3 = split[0];
                str4 = split[1];
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (URLDecoder.decode(str3, "UTF-8").contains(str2)) {
                return str4;
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

    public void setupPrefillHelper() {
        StreamWebview streamWebview = this.loginWebview;
        if (streamWebview == null) {
            appendLogs("Ignoring prefill helper");
        } else {
            callJavaScriptCode(streamWebview, " (function() {\n     function handlePrefillKey() {\n         const prefilledKey = localStorage.getItem('prefilled_key');\n         try {\n             const prefillKeyField = document.querySelector('input[type=\"password\"]');\n             const submitButton = document.querySelector('input[type=\"submit\"]');\n             if (!prefillKeyField || !submitButton) {\n                 return false;\n             }\n             const originalMethod = submitButton.onclick;\n             submitButton.onclick = function(event) {\n                 localStorage.setItem('prefilled_key', prefillKeyField.value);\n                 console.log('set value', prefillKeyField.value);\n                 if (originalMethod) {\n                     originalMethod.call(this, event);\n                 }\n             };\n             if (prefilledKey) {\n                 prefillKeyField.focus();\n                 prefillKeyField.value = prefilledKey;\n             }\n             return true;\n         } catch (err) {\n             console.log(err);\n             return false;\n         }\n     }\n     let setValueOnceInterval = setInterval(() => {\n         let worked = handlePrefillKey();\n         if (worked) {\n             clearInterval(setValueOnceInterval);\n             console.log('worked, stop trying');\n         } else {\n         }\n     }, 100);\n })();");
        }
    }

    public void getMsalToken() {
        StreamWebview streamWebview;
        if (this.msalTokenStatus.completed.booleanValue() || (streamWebview = this.loginWebview) == null) {
            appendLogs("Ignoring msal retrieval because we already have it");
        } else {
            callJavaScriptCode(streamWebview, " (function() {        let result;        const items = localStorage;        const keys = Object.keys(items);            for(let i = 0; i < keys.length; i++){                try {                    const key = keys[i];                    const dataRaw = items[key];                    let jsonData = JSON.parse(dataRaw);                    console.log('LOCAL STORAGE ITEM', key, dataRaw);                    if(jsonData &&                            jsonData['credentialType'] &&                            jsonData['credentialType'] === 'RefreshToken' &&                            jsonData['environment'] == 'login.windows.net' &&                            jsonData['secret']){                                   result = jsonData['secret'].replace('\"', '');                    }                } catch (err){                    console.error(err);                }            }            return result;        })()");
        }
    }

    private void callJavaScriptCode(WebView webView, String str) {
        webView.evaluateJavascript(str, new ValueCallback<String>() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV3.10
            @Override // android.webkit.ValueCallback
            public void onReceiveValue(String str2) {
                LoginClientV3.this.appendLogs("MSAL Response: " + str2);
                if (str2 == null || str2.equals("null") || TextUtils.isEmpty(str2)) {
                    LoginClientV3.this.appendLogs("NO MSAL! " + str2);
                    return;
                }
                String replaceAll = str2.replaceAll("\"", "");
                LoginClientV3.this.appendLogs("Found Valid MSAL Token: " + replaceAll);
                LoginClientV3.this.encryptClient.saveValue("msalAccessToken", Base64.encodeToString(replaceAll.getBytes(StandardCharsets.UTF_8), 0));
                LoginClientV3.this.msalTokenStatus.setCompleted();
            }
        });
    }
}
