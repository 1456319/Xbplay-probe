package com.studio08.xbgamestream.Authenticate;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.connectsdk.discovery.DiscoveryProvider;
import com.studio08.xbgamestream.Helpers.EncryptClient;
import com.studio08.xbgamestream.Helpers.Helper;
import com.studio08.xbgamestream.Web.ApiClient;
import com.studio08.xbgamestream.Web.StreamWebview;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: /app/base.apk/classes3.dex */
public class LoginClientV4 {
    public static DefaultRetryPolicy volleyPolicy = new DefaultRetryPolicy(DiscoveryProvider.TIMEOUT, 0, 1.0f);
    private Context context;
    private EncryptClient encryptClient;
    private String headerTmpRedirectLocation;
    private StreamWebview loginWebview;
    private JSONObject redirectTmpData;
    private String redirectTmpUri;
    private int retryAttempts = 0;
    LoginClientListener listener = null;

    /* loaded from: /app/base.apk/classes3.dex */
    public interface LoginClientListener {
        void errorMessage(String str);

        void hideDialog();

        void onLoginComplete();

        void showDialog();
    }

    public LoginClientV4(Context context, StreamWebview streamWebview) {
        this.encryptClient = null;
        this.context = context;
        this.loginWebview = streamWebview;
        this.encryptClient = new EncryptClient(this.context);
        setupWebviewListeners();
    }

    public void setCustomObjectListener(LoginClientListener loginClientListener) {
        this.listener = loginClientListener;
    }

    public void loginButtonClicked() {
        appendLogs("Starting loginButtonClicked");
        JSONObject savedXalToken = getSavedXalToken();
        if (savedXalToken == null) {
            getRedirectUriFromApi();
        } else {
            updateExistingXalTokens(savedXalToken);
        }
    }

    private void updateExistingXalTokens(JSONObject jSONObject) {
        appendLogs("updateExistingXalTokens: " + jSONObject.toString());
        LoginClientListener loginClientListener = this.listener;
        if (loginClientListener != null) {
            loginClientListener.showDialog();
        }
        RequestQueue newRequestQueue = Volley.newRequestQueue(this.context);
        JSONObject jSONObject2 = new JSONObject();
        try {
            JSONObject jSONObject3 = new JSONObject();
            jSONObject3.put("xalTokens", jSONObject);
            String value = this.encryptClient.getValue("loginRegionIp");
            if (value != null) {
                jSONObject3.put("loginRegionIp", value);
            }
            jSONObject2.put("data", jSONObject3);
            appendLogs("Data: " + jSONObject2.toString());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(1, ApiClient.TOKEN_DATA_ENDPOINT, jSONObject2, new Response.Listener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV4$$ExternalSyntheticLambda0
                @Override // com.android.volley.Response.Listener
                public final void onResponse(Object obj) {
                    LoginClientV4.this.m334x458e4614((JSONObject) obj);
                }
            }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV4$$ExternalSyntheticLambda1
                @Override // com.android.volley.Response.ErrorListener
                public final void onErrorResponse(VolleyError volleyError) {
                    LoginClientV4.this.m335x5fa9c4b3(volleyError);
                }
            });
            jsonObjectRequest.setRetryPolicy(volleyPolicy);
            newRequestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
            showError(this.listener, "Error Logging In. Try Clearing Cache. Failed to extract Xal Tokens.");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$updateExistingXalTokens$1$com-studio08-xbgamestream-Authenticate-LoginClientV4  reason: not valid java name */
    public /* synthetic */ void m335x5fa9c4b3(VolleyError volleyError) {
        appendLogs("updateExistingXalTokens failed:" + volleyError.getLocalizedMessage());
        showError(this.listener, "Error Logging In. Try Clearing Cache. Failed to update Xal Tokens.");
    }

    private void getRedirectUriFromApi() {
        appendLogs("getRedirectUriFromApi");
        RequestQueue newRequestQueue = Volley.newRequestQueue(this.context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(1, ApiClient.TOKEN_DATA_ENDPOINT, null, new Response.Listener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV4$$ExternalSyntheticLambda4
            @Override // com.android.volley.Response.Listener
            public final void onResponse(Object obj) {
                LoginClientV4.this.m332x157a3505((JSONObject) obj);
            }
        }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV4$$ExternalSyntheticLambda5
            @Override // com.android.volley.Response.ErrorListener
            public final void onErrorResponse(VolleyError volleyError) {
                LoginClientV4.this.m333x2f95b3a4(volleyError);
            }
        });
        jsonObjectRequest.setRetryPolicy(volleyPolicy);
        newRequestQueue.add(jsonObjectRequest);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$getRedirectUriFromApi$3$com-studio08-xbgamestream-Authenticate-LoginClientV4  reason: not valid java name */
    public /* synthetic */ void m333x2f95b3a4(VolleyError volleyError) {
        appendLogs("getRedirectUriFromApi failed: " + volleyError.networkResponse);
        showError(this.listener, "Failed getting redirect url from API.");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void exchangeRedirectUriForTokens(String str) {
        appendLogs("exchangeRedirectUriForTokens");
        LoginClientListener loginClientListener = this.listener;
        if (loginClientListener != null) {
            loginClientListener.showDialog();
        }
        RequestQueue newRequestQueue = Volley.newRequestQueue(this.context);
        JSONObject jSONObject = new JSONObject();
        try {
            JSONObject jSONObject2 = new JSONObject(this.redirectTmpData.toString());
            String value = this.encryptClient.getValue("loginRegionIp");
            if (value != null) {
                jSONObject2.put("loginRegionIp", value);
            }
            jSONObject.put("data", jSONObject2);
            jSONObject.put("redirectURI", str);
            appendLogs("Data: " + jSONObject.toString());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(1, ApiClient.TOKEN_DATA_ENDPOINT, jSONObject, new Response.Listener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV4$$ExternalSyntheticLambda2
                @Override // com.android.volley.Response.Listener
                public final void onResponse(Object obj) {
                    LoginClientV4.this.m330xceba69a9((JSONObject) obj);
                }
            }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV4$$ExternalSyntheticLambda3
                @Override // com.android.volley.Response.ErrorListener
                public final void onErrorResponse(VolleyError volleyError) {
                    LoginClientV4.this.m331xe8d5e848(volleyError);
                }
            });
            jsonObjectRequest.setRetryPolicy(volleyPolicy);
            newRequestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
            showError(this.listener, "Error exchanging redirect uri for tokens. Try clearing cache and logging in again.");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$exchangeRedirectUriForTokens$5$com-studio08-xbgamestream-Authenticate-LoginClientV4  reason: not valid java name */
    public /* synthetic */ void m331xe8d5e848(VolleyError volleyError) {
        appendLogs("exchangeRedirectUriForTokens failed:" + volleyError.getLocalizedMessage());
        showError(this.listener, "Error Logging In. Try Clearing Cache. Failed retrieving tokens from redirect url.");
    }

    private JSONObject getSavedXalToken() {
        return this.encryptClient.getJSONObject("xalTokens");
    }

    private void setupWebviewListeners() {
        this.loginWebview.setWebViewClient(new WebViewClient() { // from class: com.studio08.xbgamestream.Authenticate.LoginClientV4.1
            @Override // android.webkit.WebViewClient
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
                String uri = webResourceRequest.getUrl().toString();
                Log.e("LoginClientV4", "shouldOverrideUrlLoading: " + uri + ". headerTmpRedirectLocation:" + LoginClientV4.this.headerTmpRedirectLocation);
                if (uri.contains(LoginClientV4.this.headerTmpRedirectLocation)) {
                    LoginClientV4.appendLogs("Found redirect: " + uri);
                    String queryParameter = webResourceRequest.getUrl().getQueryParameter("error");
                    String queryParameter2 = webResourceRequest.getUrl().getQueryParameter("error_description");
                    if (queryParameter != null) {
                        Toast.makeText(LoginClientV4.this.context, "Error: " + queryParameter + " - " + queryParameter2, 1).show();
                        LoginClientV4.this.listener.errorMessage("Failed: " + queryParameter + " - " + queryParameter2);
                    } else {
                        LoginClientV4.this.exchangeRedirectUriForTokens(uri);
                    }
                    return true;
                }
                String xorDecode = Helper.xorDecode(uri.replace("https://xbgamestreamproxy.com/a/", ""));
                Log.e("LoginClientV4", "rawUrl: " + xorDecode + ". headerTmpRedirectLocation:" + LoginClientV4.this.headerTmpRedirectLocation);
                if (xorDecode.contains(LoginClientV4.this.headerTmpRedirectLocation)) {
                    LoginClientV4.appendLogs("Found proxy redirect: " + xorDecode);
                    LoginClientV4.this.exchangeRedirectUriForTokens(xorDecode);
                    return true;
                }
                return false;
            }

            @Override // android.webkit.WebViewClient
            public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
                super.onPageStarted(webView, str, bitmap);
                Log.e("HERE", "Page started:" + str);
            }

            @Override // android.webkit.WebViewClient
            public void onPageFinished(WebView webView, String str) {
                super.onPageFinished(webView, str);
                Log.e("HERE", "Page Finished:" + str);
            }

            @Override // android.webkit.WebViewClient
            public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
                LoginClientV4.appendLogs("Error: " + webResourceRequest.getUrl());
                LoginClientV4.appendLogs("Error: " + LoginClientV4.this.loginWebview.getUrl());
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void appendLogs(String str) {
        Log.e("LoginV4", str);
    }

    /* renamed from: handleXalTokenResponse */
    public void m334x458e4614(JSONObject jSONObject) {
        try {
            appendLogs("handleXalTokenResponse" + jSONObject.toString());
            String string = jSONObject.getString("type");
            String string2 = jSONObject.getString("error");
            JSONObject jSONObject2 = jSONObject.getJSONObject("data");
            if (string.equals("error")) {
                showError(this.listener, "Error Logging In. Try again or clear cache. Error: " + string2);
            } else if (string.equals("tokens")) {
                saveXalTokenData(jSONObject2, this.context, this.listener);
                LoginClientListener loginClientListener = this.listener;
                if (loginClientListener != null) {
                    loginClientListener.onLoginComplete();
                }
            } else if (string.equals("redirect")) {
                clearTokens();
                m332x157a3505(jSONObject);
            } else {
                showError(this.listener, "Error Logging In. Try again or clear cache. Invalid login token response.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError(this.listener, "Error extracting token response");
        }
    }

    public static void saveXalTokenData(JSONObject jSONObject, Context context, LoginClientListener loginClientListener) {
        try {
            EncryptClient encryptClient = new EncryptClient(context);
            Log.e("LoginClientv4", "Updated xal tokens: " + jSONObject);
            encryptClient.saveJSONObject("xalData", jSONObject);
            encryptClient.saveJSONObject("xalTokens", jSONObject.getJSONObject("xalTokens"));
        } catch (Exception e) {
            e.printStackTrace();
            showError(loginClientListener, "Error saving token data.");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: handleRedirectUriResponse */
    public void m332x157a3505(JSONObject jSONObject) {
        appendLogs("handleRedirectUriResponse: " + jSONObject.toString());
        if (failDueToMaxRetries()) {
            return;
        }
        try {
            String string = jSONObject.getString("redirectURI");
            JSONObject jSONObject2 = jSONObject.getJSONObject("data");
            String string2 = jSONObject2.getString("headerRedirectUriLocation");
            appendLogs("Received valid redirect data");
            this.headerTmpRedirectLocation = string2;
            this.redirectTmpData = jSONObject2;
            this.redirectTmpUri = string;
            openLoginWindow();
        } catch (JSONException e) {
            e.printStackTrace();
            showError(this.listener, "Error Logging In. Try Clearing Cache. Invalid redirect url response from API.");
        }
    }

    private boolean failDueToMaxRetries() {
        int i = this.retryAttempts;
        if (i >= 3) {
            Toast.makeText(this.context, "Failed. Max retries reached", 1).show();
            LoginClientListener loginClientListener = this.listener;
            if (loginClientListener != null) {
                loginClientListener.errorMessage("Failed. Max retries reached. Try again later.");
            }
            return true;
        }
        this.retryAttempts = i + 1;
        return false;
    }

    private void openLoginWindow() {
        appendLogs("open login window called");
        this.loginWebview.loadUrl(this.redirectTmpUri);
        LoginClientListener loginClientListener = this.listener;
        if (loginClientListener != null) {
            loginClientListener.hideDialog();
        }
    }

    private static void showError(LoginClientListener loginClientListener, String str) {
        appendLogs(str);
        if (loginClientListener != null) {
            loginClientListener.errorMessage(str);
        }
    }

    private void clearTokens() {
        appendLogs("clearTokens");
        this.encryptClient.deleteValue("xalData");
        this.encryptClient.deleteValue("xalTokens");
    }
}
