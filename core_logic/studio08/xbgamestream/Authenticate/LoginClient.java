package com.studio08.xbgamestream.Authenticate;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
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
import com.studio08.xbgamestream.Helpers.EncryptClient;
import fi.iki.elonen.NanoHTTPD;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.http.client.config.CookieSpecs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: /app/base.apk/classes3.dex */
public class LoginClient {
    private String code;
    private Context context;
    private EncryptClient encryptClient;
    LoginClientListener listener;
    private WebView loginWebview;
    private String LOGIN_URL = "https://account.xbox.com/account/signin?returnUrl=https%3A%2F%2Fwww.xbox.com%2Fen-US%2Fplay&ru=https%3A%2F%2Fwww.xbox.com%2Fen-US%2Fplay";
    private String LOGIN_COMPLETE_URL = "https://www.xbox.com/en-US/play";
    private String AUTH_COOKIE_KEY = "XBXXtkhttp://xboxlive.com";
    private String STREAM_COOKIE_KEY = "XBXXtkhttp://gssv.xboxlive.com";
    private String LOGIN_ENDPOINT = "https://xhome.gssv-play-prod.xboxlive.com/v2/login/user";
    private String XCLOUD_LOGIN_ENDPOINT = "https://xgpuweb.gssv-play-prod.xboxlive.com/v2/login/user";
    private String OAUTH_EXCHANGE_ACCESS_TOKENS = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    private String LIST_CONSOLES_ENDPOINT = "https://uks.gssv-play-prodxhome.xboxlive.com/v6/servers/home";
    private String SIGN_IN_USER_PAGE = "login.live.com";
    private String codeVerifier = "4FJg33qPZtL5zU4WpGSK2f-gC-MU8k-LrP8Vt6o1EQo";
    private String codeChallenge = "Tt6Fc841IKfboO71IESdorZ-iH0ZHhcBRhX9jOPM048";
    private Boolean alreadyFetchingAccessToken = false;
    private Boolean receivedMsalToken = false;
    private Boolean shouldInterceptMsalData = true;
    private Boolean consolesEndpointComplete = false;

    /* loaded from: /app/base.apk/classes3.dex */
    public interface LoginClientListener {
        void genericMessage(String str, String str2);

        void hideDialog();

        void onLoginComplete(String str);

        void showDialog();

        void statusMessage(String str);
    }

    public LoginClient(Context context, WebView webView) {
        this.loginWebview = null;
        this.listener = null;
        this.encryptClient = null;
        this.context = context;
        this.listener = null;
        this.loginWebview = webView;
        this.encryptClient = new EncryptClient(this.context);
        setupWebviewListeners();
        resetLogs();
    }

    public void setCustomObjectListener(LoginClientListener loginClientListener) {
        this.listener = loginClientListener;
    }

    public void doLogin() {
        if (!TextUtils.isEmpty(this.encryptClient.getValue("gsToken"))) {
            this.listener.statusMessage("Using previous GameStream token. Validating...");
            if (this.encryptClient.getValue("streamCookieRaw") != null) {
                exchangeCookieForXcloudToken(this.encryptClient.getValue("streamCookieRaw"));
            } else {
                appendLogs("streamCookieRaw NULL!! WTH");
            }
        }
        if (!this.receivedMsalToken.booleanValue()) {
            exchangeRefreshTokenForAccessToken();
        }
        this.loginWebview.loadUrl(this.LOGIN_URL);
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

    private void setupWebviewListeners() {
        this.loginWebview.setWebChromeClient(new WebChromeClient() { // from class: com.studio08.xbgamestream.Authenticate.LoginClient.1
            @Override // android.webkit.WebChromeClient
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if (consoleMessage.message().contains("InvalidCountry")) {
                    LoginClient.this.listener.statusMessage("Invalid location detected. XCloud feature will not work without using a VPN to login. Please wait up to 2 minutes...");
                }
                LoginClient.this.appendLogs(String.format(Locale.ENGLISH, "%s @ %d: %s", consoleMessage.message(), Integer.valueOf(consoleMessage.lineNumber()), consoleMessage.sourceId()));
                return true;
            }
        });
        this.loginWebview.setWebViewClient(new WebViewClient() { // from class: com.studio08.xbgamestream.Authenticate.LoginClient.2
            @Override // android.webkit.WebViewClient
            public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
                LoginClient.this.appendLogs("shouldInterceptRequest: " + webResourceRequest.getUrl() + " - " + webResourceRequest.getMethod());
                if (LoginClient.this.shouldInterceptMsalData.booleanValue() && webResourceRequest.getUrl().getHost() != null && webResourceRequest.getUrl().getHost().contains("login.microsoftonline") && webResourceRequest.getMethod().equals("POST")) {
                    LoginClient.this.appendLogs("3. INTERCEPTING THE POST REQUEST! ClientId:" + LoginClient.this.encryptClient.getValue("clientId") + " Code:" + LoginClient.this.code + " CodeVerifier:" + LoginClient.this.codeVerifier + " CodeChallenge:" + LoginClient.this.codeChallenge);
                    LoginClient.this.alreadyFetchingAccessToken = true;
                    LoginClient.this.exchangeOauthDataForAccessToken();
                    return new WebResourceResponse("text/javascript", "UTF-8", null);
                }
                return super.shouldInterceptRequest(webView, webResourceRequest);
            }

            @Override // android.webkit.WebViewClient
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
                LoginClient.this.appendLogs("shouldOverrideUrlLoading: " + webResourceRequest.getUrl() + " - " + webResourceRequest.getMethod());
                if (!LoginClient.this.shouldInterceptMsalData.booleanValue() || !webResourceRequest.getUrl().toString().contains("code_challenge") || webResourceRequest.getUrl().toString().contains(LoginClient.this.codeChallenge) || !webResourceRequest.getMethod().equals("GET")) {
                    if (LoginClient.this.shouldInterceptMsalData.booleanValue() && webResourceRequest.getUrl().toString().contains("redirect") && webResourceRequest.getUrl().toString().contains("code=") && webResourceRequest.getMethod().equals("GET")) {
                        if (LoginClient.this.alreadyFetchingAccessToken.booleanValue()) {
                            LoginClient.this.appendLogs("Ignoring request to get access token because we already are: 2");
                            return false;
                        }
                        String fragment = webResourceRequest.getUrl().getFragment();
                        LoginClient.this.code = fragment.substring(5, fragment.indexOf("&"));
                        LoginClient.this.appendLogs("2. SAVING THE CODE! Original: " + LoginClient.this.code);
                    }
                    if (webResourceRequest.getUrl().toString().contains("google.com")) {
                        LoginClient.this.appendLogs("Caught fake URL! RE prompting user to login!");
                        LoginClient.this.doLogin();
                        return true;
                    }
                    return false;
                }
                LoginClient.this.appendLogs("1. INJECTING OUR CHALLENGE! Original: " + webResourceRequest.getUrl().getQueryParameter("code_challenge"));
                LoginClient.this.encryptClient.saveValue("clientId", webResourceRequest.getUrl().getQueryParameter("client_id"));
                Uri replaceUriParameter = LoginClient.replaceUriParameter(webResourceRequest.getUrl(), "code_challenge", LoginClient.this.codeChallenge);
                if (LoginClient.this.alreadyFetchingAccessToken.booleanValue()) {
                    LoginClient.this.appendLogs("Ignoring request to get access token because we already are: 1");
                } else {
                    webView.loadUrl(replaceUriParameter.toString());
                }
                return true;
            }

            @Override // android.webkit.WebViewClient
            public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
                super.onPageStarted(webView, str, bitmap);
                LoginClient.this.listener.showDialog();
                LoginClient.this.listener.statusMessage("Waiting for: " + str.substring(0, str.indexOf(".com") + 4));
                if (str.toLowerCase().contains(LoginClient.this.SIGN_IN_USER_PAGE.toLowerCase())) {
                    LoginClient.this.listener.hideDialog();
                }
            }

            @Override // android.webkit.WebViewClient
            public void onPageFinished(WebView webView, String str) {
                super.onPageFinished(webView, str);
                if (!str.toLowerCase().contains(LoginClient.this.LOGIN_COMPLETE_URL.toLowerCase())) {
                    if (str.toLowerCase().contains(LoginClient.this.SIGN_IN_USER_PAGE.toLowerCase()) || str.startsWith("file")) {
                        LoginClient.this.listener.hideDialog();
                        return;
                    }
                    LoginClient.this.listener.showDialog();
                    LoginClient.this.listener.statusMessage("Waiting for: " + str.substring(0, str.indexOf(".com") + 4));
                    return;
                }
                LoginClient loginClient = LoginClient.this;
                String cookie = loginClient.getCookie(str, loginClient.STREAM_COOKIE_KEY);
                LoginClient.this.encryptClient.saveValue("streamCookieRaw", cookie);
                LoginClient.this.exchangeCookieForXcloudToken(cookie);
                LoginClient.this.exchangeCookieForGsToken(cookie);
            }

            @Override // android.webkit.WebViewClient
            public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
                LoginClient.this.appendLogs("Error: " + webResourceRequest.getUrl());
                if (webResourceError.getErrorCode() != -9) {
                    LoginClient.this.appendLogs("Possible Error: " + webResourceError.getErrorCode() + f.c + ((Object) webResourceError.getDescription()));
                } else {
                    Toast.makeText(webView.getContext(), "Network Error. Try again", 0).show();
                    CookieManager.getInstance().removeAllCookies(null);
                    CookieManager.getInstance().flush();
                }
                LoginClient.this.listener.hideDialog();
            }
        });
    }

    private void validateGSToken(final String str) {
        if (TextUtils.isEmpty(str)) {
            appendLogs("Ignoring validateGsToken because string null");
        } else {
            Volley.newRequestQueue(this.context).add(new StringRequest(0, this.LIST_CONSOLES_ENDPOINT, new Response.Listener<String>() { // from class: com.studio08.xbgamestream.Authenticate.LoginClient.3
                @Override // com.android.volley.Response.Listener
                public void onResponse(String str2) {
                    LoginClient.this.listener.statusMessage("GameStream Token Valid... Please wait.");
                    LoginClient.this.encryptClient.saveValue("gsToken", str);
                    LoginClient.this.encryptClient.saveValue("consoles", str2);
                    LoginClient.this.appendLogs("console response is: " + str2);
                    LoginClient.this.appendLogs("gsToken is: " + str);
                    LoginClient.this.consolesEndpointComplete = true;
                    LoginClient.this.emitLoginCompleteIfReady();
                }
            }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClient.4
                @Override // com.android.volley.Response.ErrorListener
                public void onErrorResponse(VolleyError volleyError) {
                    LoginClient.this.listener.statusMessage("GameStream Token Expired. Refreshing");
                    LoginClient.this.refreshExpiredGsToken();
                }
            }) { // from class: com.studio08.xbgamestream.Authenticate.LoginClient.5
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

    /* JADX INFO: Access modifiers changed from: private */
    public void emitLoginCompleteIfReady() {
        appendLogs("Attempting to emit onLoginComplete event");
        if (TextUtils.isEmpty(this.encryptClient.getValue("consoles"))) {
            appendLogs("Ignoring onComplete event due to no consoles data yet");
        } else if (!this.receivedMsalToken.booleanValue()) {
            appendLogs("Ignoring onComplete event due to no msal data yet");
        } else if (!this.consolesEndpointComplete.booleanValue()) {
            appendLogs("Ignoring onComplete event due to no console endpoint data yet");
        } else {
            this.listener.onLoginComplete(this.encryptClient.getValue("consoles"));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshExpiredGsToken() {
        this.encryptClient.saveValue("gsToken", null);
        String value = this.encryptClient.getValue("streamCookieRaw");
        if (!TextUtils.isEmpty(value)) {
            this.listener.statusMessage("Found existing StreamCookie token. Exchanging for GameStream token");
            exchangeCookieForXcloudToken(value);
            exchangeCookieForGsToken(value);
            return;
        }
        clearTokensAndReLogin(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void exchangeCookieForGsToken(final String str) {
        RequestQueue newRequestQueue = Volley.newRequestQueue(this.context);
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("token", new JSONObject(URLDecoder.decode(str, "UTF-8")).getString("Token"));
            jSONObject.put("offeringId", "xhome");
        } catch (Exception e) {
            this.listener.statusMessage("Cannot exchange StreamCookie for GameStream token. AuthError");
            e.printStackTrace();
            clearTokensAndReLogin(false);
        }
        newRequestQueue.add(new JsonObjectRequest(1, this.LOGIN_ENDPOINT, jSONObject, new Response.Listener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClient$$ExternalSyntheticLambda2
            @Override // com.android.volley.Response.Listener
            public final void onResponse(Object obj) {
                LoginClient.this.m314xc2a23c3a(str, (JSONObject) obj);
            }
        }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClient$$ExternalSyntheticLambda3
            @Override // com.android.volley.Response.ErrorListener
            public final void onErrorResponse(VolleyError volleyError) {
                LoginClient.this.m315xf07ad699(volleyError);
            }
        }));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$exchangeCookieForGsToken$0$com-studio08-xbgamestream-Authenticate-LoginClient  reason: not valid java name */
    public /* synthetic */ void m314xc2a23c3a(String str, JSONObject jSONObject) {
        try {
            String string = jSONObject.getString("gsToken");
            this.encryptClient.saveValue("gsToken", string);
            this.encryptClient.saveValue("streamCookieRaw", str);
            this.listener.statusMessage("Created new GameStream token from existing StreamCookie. Validating...");
            validateGSToken(string);
        } catch (JSONException e) {
            this.listener.statusMessage("Cannot exchange StreamCookie for GameStream token. JSONException");
            e.printStackTrace();
            clearTokensAndReLogin(false);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$exchangeCookieForGsToken$1$com-studio08-xbgamestream-Authenticate-LoginClient  reason: not valid java name */
    public /* synthetic */ void m315xf07ad699(VolleyError volleyError) {
        this.listener.statusMessage("Cannot exchange StreamCookie for GameStream token. Attempting to regenerate.");
        clearTokensAndReLogin(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void exchangeCookieForXcloudToken(String str) {
        RequestQueue newRequestQueue = Volley.newRequestQueue(this.context);
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("token", new JSONObject(URLDecoder.decode(str, "UTF-8")).getString("Token"));
            jSONObject.put("offeringId", "xgpuweb");
            newRequestQueue.add(new JsonObjectRequest(1, this.XCLOUD_LOGIN_ENDPOINT, jSONObject, new Response.Listener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClient$$ExternalSyntheticLambda6
                @Override // com.android.volley.Response.Listener
                public final void onResponse(Object obj) {
                    LoginClient.this.m316x6df8bb09((JSONObject) obj);
                }
            }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClient$$ExternalSyntheticLambda7
                @Override // com.android.volley.Response.ErrorListener
                public final void onErrorResponse(VolleyError volleyError) {
                    LoginClient.this.m317x9bd15568(volleyError);
                }
            }) { // from class: com.studio08.xbgamestream.Authenticate.LoginClient.6
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
            clearTokensAndReLogin(true);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$exchangeCookieForXcloudToken$2$com-studio08-xbgamestream-Authenticate-LoginClient  reason: not valid java name */
    public /* synthetic */ void m316x6df8bb09(JSONObject jSONObject) {
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
        } catch (JSONException e) {
            this.listener.statusMessage("Cannot exchange StreamCookie for xCloud token. JSONException");
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Removed duplicated region for block: B:25:0x0077  */
    /* renamed from: lambda$exchangeCookieForXcloudToken$3$com-studio08-xbgamestream-Authenticate-LoginClient  reason: not valid java name */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public /* synthetic */ void m317x9bd15568(com.android.volley.VolleyError r5) {
        /*
            r4 = this;
            com.studio08.xbgamestream.Authenticate.LoginClient$LoginClientListener r0 = r4.listener
            java.lang.String r1 = "Cannot exchange StreamCookie for xCloud token... Attempting to regenerate."
            r0.statusMessage(r1)
            com.studio08.xbgamestream.Helpers.EncryptClient r0 = r4.encryptClient
            java.lang.String r1 = "xcloudToken"
            r2 = 0
            r0.saveValue(r1, r2)
            com.studio08.xbgamestream.Helpers.EncryptClient r0 = r4.encryptClient
            java.lang.String r1 = "xcloudRegion"
            r0.saveValue(r1, r2)
            java.lang.String r0 = "full"
            boolean r1 = r0.equals(r0)
            if (r1 != 0) goto L36
            java.lang.String r1 = "legacy"
            boolean r1 = r0.equals(r1)
            if (r1 != 0) goto L36
            java.lang.String r1 = "stream"
            boolean r1 = r0.equals(r1)
            if (r1 != 0) goto L36
            java.lang.String r1 = "tv"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L82
        L36:
            r0 = 1
            r1 = 0
            if (r5 == 0) goto L75
            com.android.volley.NetworkResponse r2 = r5.networkResponse
            if (r2 == 0) goto L75
            com.android.volley.NetworkResponse r2 = r5.networkResponse
            byte[] r2 = r2.data
            if (r2 == 0) goto L75
            java.lang.String r2 = new java.lang.String     // Catch: java.io.UnsupportedEncodingException -> L71
            com.android.volley.NetworkResponse r5 = r5.networkResponse     // Catch: java.io.UnsupportedEncodingException -> L71
            byte[] r5 = r5.data     // Catch: java.io.UnsupportedEncodingException -> L71
            java.lang.String r3 = "UTF-8"
            r2.<init>(r5, r3)     // Catch: java.io.UnsupportedEncodingException -> L71
            r4.appendLogs(r2)     // Catch: java.io.UnsupportedEncodingException -> L71
            java.lang.String r5 = "InvalidCountry"
            boolean r5 = r2.contains(r5)     // Catch: java.io.UnsupportedEncodingException -> L71
            if (r5 == 0) goto L75
            com.studio08.xbgamestream.Authenticate.LoginClient$LoginClientListener r5 = r4.listener     // Catch: java.io.UnsupportedEncodingException -> L6e
            java.lang.String r1 = "Invalid country code detected. XCloud feature will not work."
            r5.statusMessage(r1)     // Catch: java.io.UnsupportedEncodingException -> L6e
            android.content.Context r5 = r4.context     // Catch: java.io.UnsupportedEncodingException -> L6e
            java.lang.String r1 = "Invalid location detected. XCloud feature will not work. Consider using a VPN to login if you plan to use XCloud."
            android.widget.Toast r5 = android.widget.Toast.makeText(r5, r1, r0)     // Catch: java.io.UnsupportedEncodingException -> L6e
            r5.show()     // Catch: java.io.UnsupportedEncodingException -> L6e
            r1 = r0
            goto L75
        L6e:
            r5 = move-exception
            r1 = r0
            goto L72
        L71:
            r5 = move-exception
        L72:
            r5.printStackTrace()
        L75:
            if (r1 != 0) goto L82
            android.content.Context r5 = r4.context
            java.lang.String r1 = "You don't have GamePass. You can't use the XCloud feature, everything else will work :)"
            android.widget.Toast r5 = android.widget.Toast.makeText(r5, r1, r0)
            r5.show()
        L82:
            r4.emitLoginCompleteIfReady()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.studio08.xbgamestream.Authenticate.LoginClient.m317x9bd15568(com.android.volley.VolleyError):void");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void exchangeOauthDataForAccessToken() {
        if (TextUtils.isEmpty(this.encryptClient.getValue("clientId")) || TextUtils.isEmpty(this.code) || TextUtils.isEmpty(this.codeVerifier)) {
            clearTokensAndReLogin(false);
        } else {
            Volley.newRequestQueue(this.context).add(new StringRequest(1, this.OAUTH_EXCHANGE_ACCESS_TOKENS, new Response.Listener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClient$$ExternalSyntheticLambda4
                @Override // com.android.volley.Response.Listener
                public final void onResponse(Object obj) {
                    LoginClient.this.m318xc8e80911((String) obj);
                }
            }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClient$$ExternalSyntheticLambda5
                @Override // com.android.volley.Response.ErrorListener
                public final void onErrorResponse(VolleyError volleyError) {
                    LoginClient.this.m319xf6c0a370(volleyError);
                }
            }) { // from class: com.studio08.xbgamestream.Authenticate.LoginClient.7
                @Override // com.android.volley.Request
                public String getBodyContentType() {
                    return "application/x-www-form-urlencoded; charset=UTF-8";
                }

                @Override // com.android.volley.Request
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap hashMap = new HashMap();
                    hashMap.put("client_id", LoginClient.this.encryptClient.getValue("clientId"));
                    hashMap.put("code", LoginClient.this.code);
                    hashMap.put("code_verifier", LoginClient.this.codeVerifier);
                    hashMap.put("redirect_uri", "https://www.xbox.com/play/login/redirect");
                    hashMap.put("scope", "service::http://Passport.NET/purpose::PURPOSE_XBOX_CLOUD_CONSOLE_TRANSFER_TOKEN openid profile offline_access");
                    hashMap.put("grant_type", "authorization_code");
                    hashMap.put("client_info", "1");
                    return hashMap;
                }

                @Override // com.android.volley.Request
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap hashMap = new HashMap();
                    hashMap.put("Content-Type", "application/x-www-form-urlencoded");
                    hashMap.put(HttpHeaders.ORIGIN, "https://www.xbox.com");
                    return hashMap;
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$exchangeOauthDataForAccessToken$4$com-studio08-xbgamestream-Authenticate-LoginClient  reason: not valid java name */
    public /* synthetic */ void m318xc8e80911(String str) {
        appendLogs("Got msal token: " + str.toString());
        try {
            JSONObject jSONObject = new JSONObject(str.toString());
            String string = jSONObject.getString("access_token");
            this.encryptClient.saveValue("msalRefreshToken", jSONObject.getString("refresh_token"));
            this.encryptClient.saveValue("msalAccessToken", Base64.encodeToString(string.getBytes(StandardCharsets.UTF_8), 0));
            this.listener.statusMessage("Successfully received msal token");
        } catch (JSONException e) {
            this.listener.statusMessage("Cannot acquire msal token. JSONException");
            e.printStackTrace();
        }
        this.shouldInterceptMsalData = false;
        this.receivedMsalToken = true;
        emitLoginCompleteIfReady();
        doLogin();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$exchangeOauthDataForAccessToken$5$com-studio08-xbgamestream-Authenticate-LoginClient  reason: not valid java name */
    public /* synthetic */ void m319xf6c0a370(VolleyError volleyError) {
        String str;
        String str2 = "";
        if (volleyError != null && volleyError.networkResponse.data != null) {
            try {
                str = new String(volleyError.networkResponse.data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (volleyError != null && volleyError.networkResponse != null) {
                str2 = String.valueOf(volleyError.networkResponse.statusCode);
            }
            appendLogs(str2 + str);
            this.listener.statusMessage("Error retrieving GS access token: " + str2 + str);
            clearTokensAndReLogin(false);
        }
        str = "";
        if (volleyError != null) {
            str2 = String.valueOf(volleyError.networkResponse.statusCode);
        }
        appendLogs(str2 + str);
        this.listener.statusMessage("Error retrieving GS access token: " + str2 + str);
        clearTokensAndReLogin(false);
    }

    private void exchangeRefreshTokenForAccessToken() {
        if (TextUtils.isEmpty(this.encryptClient.getValue("msalRefreshToken")) || TextUtils.isEmpty(this.encryptClient.getValue("clientId"))) {
            appendLogs("no msal data set! Not prompting for relogin... Hopefully wont hang because we are always reloading site...");
            validateGSToken(this.encryptClient.getValue("gsToken"));
            return;
        }
        Volley.newRequestQueue(this.context).add(new StringRequest(1, this.OAUTH_EXCHANGE_ACCESS_TOKENS, new Response.Listener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClient$$ExternalSyntheticLambda0
            @Override // com.android.volley.Response.Listener
            public final void onResponse(Object obj) {
                LoginClient.this.m320x4ca24ab2((String) obj);
            }
        }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClient$$ExternalSyntheticLambda1
            @Override // com.android.volley.Response.ErrorListener
            public final void onErrorResponse(VolleyError volleyError) {
                LoginClient.this.m321x7a7ae511(volleyError);
            }
        }) { // from class: com.studio08.xbgamestream.Authenticate.LoginClient.8
            @Override // com.android.volley.Request
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override // com.android.volley.Request
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap hashMap = new HashMap();
                hashMap.put("client_id", LoginClient.this.encryptClient.getValue("clientId"));
                hashMap.put("refresh_token", LoginClient.this.encryptClient.getValue("msalRefreshToken"));
                hashMap.put("redirect_uri", "https://www.xbox.com/play/login/redirect");
                hashMap.put("scope", "service::http://Passport.NET/purpose::PURPOSE_XBOX_CLOUD_CONSOLE_TRANSFER_TOKEN openid profile offline_access");
                hashMap.put("grant_type", "refresh_token");
                hashMap.put("client_info", "1");
                return hashMap;
            }

            @Override // com.android.volley.Request
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap hashMap = new HashMap();
                hashMap.put("Content-Type", "application/x-www-form-urlencoded");
                hashMap.put(HttpHeaders.ORIGIN, "https://www.xbox.com");
                return hashMap;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$exchangeRefreshTokenForAccessToken$6$com-studio08-xbgamestream-Authenticate-LoginClient  reason: not valid java name */
    public /* synthetic */ void m320x4ca24ab2(String str) {
        appendLogs("Got msal token: " + str.toString());
        try {
            JSONObject jSONObject = new JSONObject(str.toString());
            String string = jSONObject.getString("access_token");
            this.encryptClient.saveValue("msalRefreshToken", jSONObject.getString("refresh_token"));
            this.encryptClient.saveValue("msalAccessToken", Base64.encodeToString(string.getBytes(StandardCharsets.UTF_8), 0));
            this.listener.statusMessage("Successfully received msal token");
        } catch (JSONException e) {
            this.listener.statusMessage("Cannot acquire msal token. JSONException");
            e.printStackTrace();
        }
        this.receivedMsalToken = true;
        this.shouldInterceptMsalData = false;
        emitLoginCompleteIfReady();
        doLogin();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$exchangeRefreshTokenForAccessToken$7$com-studio08-xbgamestream-Authenticate-LoginClient  reason: not valid java name */
    public /* synthetic */ void m321x7a7ae511(VolleyError volleyError) {
        String str;
        String str2 = "";
        if (volleyError != null && volleyError.networkResponse.data != null) {
            try {
                str = new String(volleyError.networkResponse.data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (volleyError != null && volleyError.networkResponse != null) {
                str2 = String.valueOf(volleyError.networkResponse.statusCode);
            }
            appendLogs(str2 + str);
            this.listener.statusMessage("Error retrieving GS access token: " + str2 + str);
            clearTokensAndReLogin(true);
        }
        str = "";
        if (volleyError != null) {
            str2 = String.valueOf(volleyError.networkResponse.statusCode);
        }
        appendLogs(str2 + str);
        this.listener.statusMessage("Error retrieving GS access token: " + str2 + str);
        clearTokensAndReLogin(true);
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
        this.code = null;
        this.alreadyFetchingAccessToken = false;
        if (bool.booleanValue()) {
            return;
        }
        ((Activity) this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Authenticate.LoginClient.9
            @Override // java.lang.Runnable
            public void run() {
                LoginClient.this.doLogin();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Uri replaceUriParameter(Uri uri, String str, String str2) {
        Set<String> queryParameterNames = uri.getQueryParameterNames();
        Uri.Builder clearQuery = uri.buildUpon().clearQuery();
        for (String str3 : queryParameterNames) {
            clearQuery.appendQueryParameter(str3, str3.equals(str) ? str2 : uri.getQueryParameter(str3));
        }
        return clearQuery.build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getCookie(String str, String str2) {
        String[] split;
        String cookie = CookieManager.getInstance().getCookie(str);
        if (cookie != null) {
            for (String str3 : cookie.split(";")) {
                if (str3.contains(str2)) {
                    return str3.split(f.b)[1];
                }
            }
        }
        return null;
    }
}
