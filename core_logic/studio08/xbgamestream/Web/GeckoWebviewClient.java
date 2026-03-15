package com.studio08.xbgamestream.Web;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.studio08.xbgamestream.Helpers.Helper;
import com.studio08.xbgamestream.PWAMainMenuActivity;
import com.tapjoy.TJAdUnitConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebExtension;
/* loaded from: /app/base.apk/classes3.dex */
public class GeckoWebviewClient {
    ApiClient apiClient;
    Context context;
    AlertDialog dialog;
    GeckoView geckoView;
    boolean isPWA;
    private String lastUrlStarted;
    StreamWebviewListener listener;
    WebExtension.Port mPort;
    WebExtension.MessageDelegate messageDelegate;
    WebExtension.PortDelegate portDelegate;
    GeckoSession.ProgressDelegate progressDelegate;
    public JSONObject pwaConfigData;
    GeckoRuntime runtime;
    GeckoSession session;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: /app/base.apk/classes3.dex */
    public class MyNavigationDelegate implements GeckoSession.NavigationDelegate {
        MyNavigationDelegate() {
        }

        @Override // org.mozilla.geckoview.GeckoSession.NavigationDelegate
        public GeckoResult<AllowOrDeny> onLoadRequest(GeckoSession geckoSession, GeckoSession.NavigationDelegate.LoadRequest loadRequest) {
            Log.d("tag", "onLoadRequest=" + loadRequest.uri + " triggerUri=" + loadRequest.triggerUri + " where=" + loadRequest.target + " isRedirect=" + loadRequest.isRedirect + " isDirectNavigation=" + loadRequest.isDirectNavigation);
            return GeckoResult.allow();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /app/base.apk/classes3.dex */
    public class PermissionDelegate implements GeckoSession.PermissionDelegate {
        private PermissionDelegate() {
        }

        @Override // org.mozilla.geckoview.GeckoSession.PermissionDelegate
        public void onAndroidPermissionsRequest(GeckoSession geckoSession, String[] strArr, GeckoSession.PermissionDelegate.Callback callback) {
            Log.e("TAG", "onAndroidPermissionsRequest");
            callback.grant();
        }

        @Override // org.mozilla.geckoview.GeckoSession.PermissionDelegate
        public void onMediaPermissionRequest(GeckoSession geckoSession, String str, GeckoSession.PermissionDelegate.MediaSource[] mediaSourceArr, GeckoSession.PermissionDelegate.MediaSource[] mediaSourceArr2, GeckoSession.PermissionDelegate.MediaCallback mediaCallback) {
            Log.e("TAG", "onMediaPermissionRequest");
            if (mediaSourceArr2 != null) {
                if (Helper.checkIfAlreadyHavePermission("android.permission.RECORD_AUDIO", GeckoWebviewClient.this.context)) {
                    Log.e("HERE", "Already have audio perm");
                } else {
                    Toast.makeText(GeckoWebviewClient.this.context, "Grant Permissions and Retry", 0).show();
                    Helper.requestForSpecificPermission(new String[]{"android.permission.RECORD_AUDIO"}, GeckoWebviewClient.this.context);
                }
                for (int i = 0; i < mediaSourceArr2.length; i++) {
                    Log.e("TAG", "Granting audio: " + mediaSourceArr2[i].name + " : " + mediaSourceArr2[i].id);
                    mediaCallback.grant((GeckoSession.PermissionDelegate.MediaSource) null, mediaSourceArr2[i]);
                }
            }
        }

        @Override // org.mozilla.geckoview.GeckoSession.PermissionDelegate
        public GeckoResult<Integer> onContentPermissionRequest(GeckoSession geckoSession, GeckoSession.PermissionDelegate.ContentPermission contentPermission) {
            Log.e("TAG", "onContentPermissionRequest " + contentPermission + contentPermission.permission);
            return GeckoResult.fromValue(1);
        }
    }

    public GeckoWebviewClient(Context context, GeckoView geckoView, ApiClient apiClient) {
        this.isPWA = false;
        this.portDelegate = new WebExtension.PortDelegate() { // from class: com.studio08.xbgamestream.Web.GeckoWebviewClient.1
            @Override // org.mozilla.geckoview.WebExtension.PortDelegate
            public void onPortMessage(Object obj, WebExtension.Port port) {
                Log.e("GeckoView", "onPortMessage: " + obj);
                if (obj instanceof String) {
                    try {
                        JSONObject jSONObject = new JSONObject((String) obj);
                        String string = jSONObject.getString("type");
                        JSONObject jSONObject2 = jSONObject.getJSONObject("data");
                        Log.e("GeckoView", "Received GeckoView Event from fronted of type: " + string);
                        if (string.equals("relogin")) {
                            GeckoWebviewClient.this.listener.onReLoginRequest();
                        } else if (string.equals("start_xcloud_stream")) {
                            GeckoWebviewClient.this.listener.genericMessage(string, jSONObject2.getString("titleId"));
                        } else if (string.equals("vibrate")) {
                            GeckoWebviewClient.this.listener.vibrate();
                        } else if (string.equals("set_orientation")) {
                            GeckoWebviewClient.this.listener.setOrientationValue(jSONObject2.getString(TJAdUnitConstants.String.MESSAGE));
                        } else if (string.equals("generic")) {
                            GeckoWebviewClient.this.listener.genericMessage(jSONObject2.getString("generic_type"), jSONObject2.getString(TJAdUnitConstants.String.MESSAGE));
                        } else {
                            Log.e("GVWC", "Invalid geckoview event type: " + string);
                        }
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                Log.e("TAG", "Geckoview port message not an object");
            }

            @Override // org.mozilla.geckoview.WebExtension.PortDelegate
            public void onDisconnect(WebExtension.Port port) {
                Log.e("GeckoView", "onDisconnect");
                if (port == GeckoWebviewClient.this.mPort) {
                    GeckoWebviewClient.this.mPort = null;
                }
            }
        };
        this.messageDelegate = new WebExtension.MessageDelegate() { // from class: com.studio08.xbgamestream.Web.GeckoWebviewClient.2
            @Override // org.mozilla.geckoview.WebExtension.MessageDelegate
            public void onConnect(WebExtension.Port port) {
                Log.e("GeckoView", "onConnect. Sending config data");
                GeckoWebviewClient.this.mPort = port;
                GeckoWebviewClient.this.mPort.setDelegate(GeckoWebviewClient.this.portDelegate);
                GeckoWebviewClient.this.setConfig();
            }
        };
        this.progressDelegate = new GeckoSession.ProgressDelegate() { // from class: com.studio08.xbgamestream.Web.GeckoWebviewClient.3
            @Override // org.mozilla.geckoview.GeckoSession.ProgressDelegate
            public void onPageStart(GeckoSession geckoSession, String str) {
                Log.e("GeckoView", "OnPageStarted " + str);
                try {
                    GeckoWebviewClient.this.lastUrlStarted = str;
                    GeckoWebviewClient geckoWebviewClient = GeckoWebviewClient.this;
                    geckoWebviewClient.setProgressDialog(geckoWebviewClient.context);
                } catch (Exception unused) {
                }
            }

            @Override // org.mozilla.geckoview.GeckoSession.ProgressDelegate
            public void onPageStop(GeckoSession geckoSession, boolean z) {
                Log.e("GeckoView", "OnPageStopped " + z);
                try {
                    GeckoWebviewClient.this.hideDialog();
                } catch (Exception unused) {
                }
                if (z) {
                    return;
                }
                try {
                    Toast.makeText(GeckoWebviewClient.this.context, "Error load page. Ensure you are connected to the internet and try again.", 1).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        this.context = context;
        this.geckoView = geckoView;
        this.apiClient = apiClient;
        init();
    }

    public GeckoWebviewClient(Context context, GeckoView geckoView, boolean z) {
        this.isPWA = false;
        this.portDelegate = new WebExtension.PortDelegate() { // from class: com.studio08.xbgamestream.Web.GeckoWebviewClient.1
            @Override // org.mozilla.geckoview.WebExtension.PortDelegate
            public void onPortMessage(Object obj, WebExtension.Port port) {
                Log.e("GeckoView", "onPortMessage: " + obj);
                if (obj instanceof String) {
                    try {
                        JSONObject jSONObject = new JSONObject((String) obj);
                        String string = jSONObject.getString("type");
                        JSONObject jSONObject2 = jSONObject.getJSONObject("data");
                        Log.e("GeckoView", "Received GeckoView Event from fronted of type: " + string);
                        if (string.equals("relogin")) {
                            GeckoWebviewClient.this.listener.onReLoginRequest();
                        } else if (string.equals("start_xcloud_stream")) {
                            GeckoWebviewClient.this.listener.genericMessage(string, jSONObject2.getString("titleId"));
                        } else if (string.equals("vibrate")) {
                            GeckoWebviewClient.this.listener.vibrate();
                        } else if (string.equals("set_orientation")) {
                            GeckoWebviewClient.this.listener.setOrientationValue(jSONObject2.getString(TJAdUnitConstants.String.MESSAGE));
                        } else if (string.equals("generic")) {
                            GeckoWebviewClient.this.listener.genericMessage(jSONObject2.getString("generic_type"), jSONObject2.getString(TJAdUnitConstants.String.MESSAGE));
                        } else {
                            Log.e("GVWC", "Invalid geckoview event type: " + string);
                        }
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                Log.e("TAG", "Geckoview port message not an object");
            }

            @Override // org.mozilla.geckoview.WebExtension.PortDelegate
            public void onDisconnect(WebExtension.Port port) {
                Log.e("GeckoView", "onDisconnect");
                if (port == GeckoWebviewClient.this.mPort) {
                    GeckoWebviewClient.this.mPort = null;
                }
            }
        };
        this.messageDelegate = new WebExtension.MessageDelegate() { // from class: com.studio08.xbgamestream.Web.GeckoWebviewClient.2
            @Override // org.mozilla.geckoview.WebExtension.MessageDelegate
            public void onConnect(WebExtension.Port port) {
                Log.e("GeckoView", "onConnect. Sending config data");
                GeckoWebviewClient.this.mPort = port;
                GeckoWebviewClient.this.mPort.setDelegate(GeckoWebviewClient.this.portDelegate);
                GeckoWebviewClient.this.setConfig();
            }
        };
        this.progressDelegate = new GeckoSession.ProgressDelegate() { // from class: com.studio08.xbgamestream.Web.GeckoWebviewClient.3
            @Override // org.mozilla.geckoview.GeckoSession.ProgressDelegate
            public void onPageStart(GeckoSession geckoSession, String str) {
                Log.e("GeckoView", "OnPageStarted " + str);
                try {
                    GeckoWebviewClient.this.lastUrlStarted = str;
                    GeckoWebviewClient geckoWebviewClient = GeckoWebviewClient.this;
                    geckoWebviewClient.setProgressDialog(geckoWebviewClient.context);
                } catch (Exception unused) {
                }
            }

            @Override // org.mozilla.geckoview.GeckoSession.ProgressDelegate
            public void onPageStop(GeckoSession geckoSession, boolean z2) {
                Log.e("GeckoView", "OnPageStopped " + z2);
                try {
                    GeckoWebviewClient.this.hideDialog();
                } catch (Exception unused) {
                }
                if (z2) {
                    return;
                }
                try {
                    Toast.makeText(GeckoWebviewClient.this.context, "Error load page. Ensure you are connected to the internet and try again.", 1).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        this.context = context;
        this.geckoView = geckoView;
        init();
        this.isPWA = z;
    }

    public void setCustomObjectListener(StreamWebviewListener streamWebviewListener) {
        this.listener = streamWebviewListener;
    }

    public void init() {
        if (this.runtime == null) {
            this.runtime = GeckoRuntime.getDefault(this.context);
        }
        if ("release".equals("debug")) {
            this.runtime.getSettings().setRemoteDebuggingEnabled(true);
            this.runtime.getSettings().setConsoleOutputEnabled(true);
            this.runtime.getSettings().setAboutConfigEnabled(true);
        }
        GeckoSession geckoSession = new GeckoSession(new GeckoSessionSettings.Builder().useTrackingProtection(true).suspendMediaWhenInactive(true).allowJavascript(true).build());
        this.session = geckoSession;
        geckoSession.setPermissionDelegate(new PermissionDelegate());
        this.session.setPromptDelegate(new BasicGeckoViewPrompt((Activity) this.context));
        this.session.setProgressDelegate(this.progressDelegate);
        this.session.setNavigationDelegate(new MyNavigationDelegate());
        this.runtime.getWebExtensionController().ensureBuiltIn("resource://android/assets/messaging/", "browser@xbgamestream.com").accept(new GeckoResult.Consumer() { // from class: com.studio08.xbgamestream.Web.GeckoWebviewClient$$ExternalSyntheticLambda0
            @Override // org.mozilla.geckoview.GeckoResult.Consumer
            public final void accept(Object obj) {
                GeckoWebviewClient.this.m346lambda$init$0$comstudio08xbgamestreamWebGeckoWebviewClient((WebExtension) obj);
            }
        }, new GeckoResult.Consumer() { // from class: com.studio08.xbgamestream.Web.GeckoWebviewClient$$ExternalSyntheticLambda1
            @Override // org.mozilla.geckoview.GeckoResult.Consumer
            public final void accept(Object obj) {
                Log.e("MessageDelegate", "Error registering extension", (Throwable) obj);
            }
        });
        this.session.open(this.runtime);
        this.geckoView.setSession(this.session);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$init$0$com-studio08-xbgamestream-Web-GeckoWebviewClient  reason: not valid java name */
    public /* synthetic */ void m346lambda$init$0$comstudio08xbgamestreamWebGeckoWebviewClient(WebExtension webExtension) {
        GeckoSession geckoSession = this.session;
        if (geckoSession != null) {
            geckoSession.getWebExtensionController().setMessageDelegate(webExtension, this.messageDelegate, "browser");
        }
    }

    public String getCurrentUrl() {
        return this.lastUrlStarted;
    }

    public void sendControllerInput(JSONObject jSONObject) {
        if (this.mPort == null) {
            Log.e("GeckoClient", "mPort NULL");
            return;
        }
        try {
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("data", jSONObject);
            jSONObject2.put("type", "setControllerInput");
            this.mPort.postMessage(jSONObject2);
        } catch (JSONException e) {
            Log.e("GeckoClient", "Failed to parse config data! Explode!");
            e.printStackTrace();
        }
    }

    public void togglePip(boolean z) {
        if (this.mPort == null) {
            Log.e("GeckoClient", "mPort NULL");
            return;
        }
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("data", z);
            jSONObject.put("type", "togglePip");
            this.mPort.postMessage(jSONObject);
        } catch (JSONException e) {
            Log.e("GeckoClient", "Failed to parse togglePip data! Explode!");
            e.printStackTrace();
        }
    }

    public void sendMouseInput(JSONObject jSONObject) {
        if (this.mPort == null) {
            Log.e("GeckoClient", "mPort NULL");
            return;
        }
        try {
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("data", jSONObject);
            jSONObject2.put("type", "setMousePayload");
            this.mPort.postMessage(jSONObject2);
        } catch (JSONException e) {
            Log.e("GeckoClient", "Failed to parse mouse data! Explode!");
            e.printStackTrace();
        }
    }

    public void sendToggleTVMenu(JSONObject jSONObject) {
        if (this.mPort == null) {
            Log.e("GeckoClient", "mPort NULL");
            return;
        }
        try {
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("data", jSONObject);
            jSONObject2.put("type", "toggleTVMenu");
            this.mPort.postMessage(jSONObject2);
        } catch (JSONException e) {
            Log.e("GeckoClient", "Failed to parse mouse data! Explode!");
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setConfig() {
        if (this.mPort == null) {
            Log.e("GeckoClient", "mPort NULL");
            return;
        }
        try {
            JSONObject jSONObject = new JSONObject();
            if (this.isPWA) {
                jSONObject.put("data", this.pwaConfigData);
                jSONObject.put("type", "setConfig");
            } else {
                jSONObject.put("data", new JSONObject(this.apiClient.getConfigSettings()));
                jSONObject.put("type", "setConfig");
            }
            this.mPort.postMessage(jSONObject);
        } catch (JSONException e) {
            Log.e("GeckoClient", "Failed to parse config data! Explode!");
            e.printStackTrace();
        }
    }

    public void loadUrl(String str) {
        this.session.loadUri(str);
    }

    public void destroy() {
        if (this.runtime != null) {
            this.runtime = null;
        }
        GeckoSession geckoSession = this.session;
        if (geckoSession != null) {
            geckoSession.stop();
            this.session.close();
            this.session = null;
        }
        GeckoView geckoView = this.geckoView;
        if (geckoView != null) {
            geckoView.destroyDrawingCache();
            this.geckoView = null;
        }
        hideDialog();
    }

    public void setProgressDialog(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(0);
        linearLayout.setPadding(10, 10, 10, 10);
        linearLayout.setGravity(17);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.gravity = 17;
        linearLayout.setLayoutParams(layoutParams);
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, 0, 0);
        progressBar.setLayoutParams(layoutParams);
        progressBar.setVisibility(0);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams2.gravity = 17;
        TextView textView = new TextView(context);
        textView.setText("Please Wait...");
        textView.setTextColor(Color.parseColor("#FFFFFF"));
        textView.setTextSize(20.0f);
        textView.setLayoutParams(layoutParams2);
        linearLayout.addView(progressBar);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setView(linearLayout);
        hideDialog();
        AlertDialog create = builder.create();
        this.dialog = create;
        create.show();
        if (this.dialog.getWindow() != null) {
            WindowManager.LayoutParams layoutParams3 = new WindowManager.LayoutParams();
            layoutParams3.copyFrom(this.dialog.getWindow().getAttributes());
            layoutParams3.width = -2;
            layoutParams3.height = -2;
            this.dialog.getWindow().setAttributes(layoutParams3);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hideDialog() {
        try {
            this.dialog.dismiss();
        } catch (Exception unused) {
        }
        try {
            PWAMainMenuActivity.hideSystemUI(this.context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
