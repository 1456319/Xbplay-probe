package com.studio08.xbgamestream.Web;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;
import com.amazon.a.a.o.b.f;
import com.android.billingclient.api.Purchase;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.applovin.sdk.AppLovinEventTypes;
import com.google.firebase.sessions.settings.RemoteSettings;
import com.studio08.xbgamestream.BuildConfig;
import com.studio08.xbgamestream.Controller.ControllerHandler;
import com.studio08.xbgamestream.Helpers.EncryptClient;
import com.studio08.xbgamestream.Helpers.Helper;
import com.studio08.xbgamestream.Helpers.RewardedAdLoader;
import com.studio08.xbgamestream.Helpers.TWAClient;
import com.tapjoy.TJAdUnitConstants;
import com.tapjoy.TapjoyConstants;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.geckoview.GeckoView;
/* loaded from: /app/base.apk/classes3.dex */
public class ApiClient {
    public static String BASE_URL_DEV;
    public static String BASE_URL_PROD;
    public static String LOOKUP_PCHECK_BASE_URL;
    public static String SMARTGLASS_COMMAND_URL;
    public static String STREAMING_URL;
    public static String TOKEN_DATA_ENDPOINT;
    public static String TOKEN_GET_BASE_URL;
    public static String TOKEN_SAVE_BASE_URL;
    public static boolean USE_DEV;
    private String CAST_REMOTE_URL;
    private String CONTROLLER_BUILDER_URL;
    private String CONTROLLER_URL;
    private String LOOKUP_TVCODE_BASE_URL;
    private final int MAX_CALLS_PER_SECOND;
    private final long MIN_TIME_BETWEEN_CALLS_MS;
    private String PHYSICAL_CONTROLLER_SETUP_URL;
    private String STREAMING_ONLY_URL;
    private String TUTORIAL_SCREENS_URL;
    private String VOICE_REMOTE_URL;
    private String WEBOS_TVCODE_BASE_URL;
    private String WIDGET_INFO_URL;
    private String WIFI_REMOTE_URL;
    private String XCLOUD_GAME_PICKER;
    private Context context;
    private ControllerHandler controllerHandler;
    ControllerHandler.ControllerHandlerListener controllerHandlerListener;
    private GeckoView geckoStreamWebview;
    GeckoWebviewClient geckoWebviewClient;
    private String gsToken;
    private Boolean isXcloud;
    private long lastCallTime;
    StreamingClientListener listener;
    private Boolean loadedStreamView;
    private float movementXValue;
    private float movementYValue;
    private String serverId;
    private StreamWebview streamWebview;
    StreamWebviewListener webviewListener;
    CustomWebClient webviewStreamStartClient;

    /* loaded from: /app/base.apk/classes3.dex */
    public interface StreamingClientListener {
        void genericMessage(String str, String str2);

        void onCloseScreenDetected();

        void onReLoginDetected();

        void pressButtonWifiRemote(String str);

        void setOrientationValue(String str);

        void vibrate();
    }

    static {
        StringBuilder sb;
        String str;
        StringBuilder sb2;
        String str2;
        StringBuilder sb3;
        String str3;
        StringBuilder sb4;
        String str4;
        "release".equals("debug");
        USE_DEV = false;
        BASE_URL_DEV = "http://192.168.12.221:3000/";
        BASE_URL_PROD = "https://www.xbgamestream.com/";
        STREAMING_URL = BASE_URL_PROD + "android_stream.html";
        if (USE_DEV) {
            sb = new StringBuilder();
            str = BASE_URL_DEV;
        } else {
            sb = new StringBuilder();
            str = BASE_URL_PROD;
        }
        LOOKUP_PCHECK_BASE_URL = sb.append(str).append("api/get_user_data").toString();
        if (USE_DEV) {
            sb2 = new StringBuilder();
            str2 = BASE_URL_DEV;
        } else {
            sb2 = new StringBuilder();
            str2 = BASE_URL_PROD;
        }
        TOKEN_SAVE_BASE_URL = sb2.append(str2).append("users/tokens/android").toString();
        if (USE_DEV) {
            sb3 = new StringBuilder();
            str3 = BASE_URL_DEV;
        } else {
            sb3 = new StringBuilder();
            str3 = BASE_URL_PROD;
        }
        TOKEN_GET_BASE_URL = sb3.append(str3).append("users/tokens/get_active").toString();
        if (USE_DEV) {
            sb4 = new StringBuilder();
            str4 = BASE_URL_DEV;
        } else {
            sb4 = new StringBuilder();
            str4 = BASE_URL_PROD;
        }
        TOKEN_DATA_ENDPOINT = sb4.append(str4).append("xal/tokenData").toString();
        SMARTGLASS_COMMAND_URL = "https://xccs.xboxlive.com/commands";
    }

    public void togglePointerLock(String str) {
        Log.e("HERE", "handle pointer lock" + str);
        if (Build.VERSION.SDK_INT >= 26) {
            if (str.equals("true")) {
                StreamWebview streamWebview = this.streamWebview;
                if (streamWebview != null) {
                    streamWebview.requestPointerCapture();
                    return;
                }
                GeckoView geckoView = this.geckoStreamWebview;
                if (geckoView != null) {
                    geckoView.requestPointerCapture();
                    return;
                }
                return;
            }
            StreamWebview streamWebview2 = this.streamWebview;
            if (streamWebview2 != null) {
                streamWebview2.releasePointerCapture();
                return;
            }
            GeckoView geckoView2 = this.geckoStreamWebview;
            if (geckoView2 != null) {
                geckoView2.releasePointerCapture();
            }
        }
    }

    public void setControllerHandler(ControllerHandler controllerHandler) {
        this.controllerHandler = controllerHandler;
        controllerHandler.setListener(this.controllerHandlerListener);
        GeckoView geckoView = this.geckoStreamWebview;
        if (geckoView != null) {
            this.controllerHandler.setSourceView(geckoView);
            return;
        }
        StreamWebview streamWebview = this.streamWebview;
        if (streamWebview != null) {
            this.controllerHandler.setPassthroughView(streamWebview);
        }
    }

    public ApiClient(Context context, StreamWebview streamWebview, String str, String str2, boolean z) {
        StringBuilder sb;
        String str3;
        StringBuilder sb2;
        String str4;
        StringBuilder sb3;
        String str5;
        StringBuilder sb4;
        String str6;
        StringBuilder sb5;
        String str7;
        StringBuilder sb6;
        String str8;
        StringBuilder sb7;
        String str9;
        StringBuilder sb8;
        String str10;
        StringBuilder sb9;
        String str11;
        StringBuilder sb10;
        String str12;
        StringBuilder sb11;
        String str13;
        StringBuilder sb12;
        String str14;
        if (USE_DEV) {
            sb = new StringBuilder();
            str3 = BASE_URL_DEV;
        } else {
            sb = new StringBuilder();
            str3 = BASE_URL_PROD;
        }
        this.CONTROLLER_URL = sb.append(str3).append("android_stream.html?controllerOnly=1").toString();
        if (USE_DEV) {
            sb2 = new StringBuilder();
            str4 = BASE_URL_DEV;
        } else {
            sb2 = new StringBuilder();
            str4 = BASE_URL_PROD;
        }
        this.STREAMING_ONLY_URL = sb2.append(str4).append("android_stream.html?stream=1").toString();
        if (USE_DEV) {
            sb3 = new StringBuilder();
            str5 = BASE_URL_DEV;
        } else {
            sb3 = new StringBuilder();
            str5 = BASE_URL_PROD;
        }
        this.CONTROLLER_BUILDER_URL = sb3.append(str5).append("builder/controller_builder.html").toString();
        if (USE_DEV) {
            sb4 = new StringBuilder();
            str6 = BASE_URL_DEV;
        } else {
            sb4 = new StringBuilder();
            str6 = BASE_URL_PROD;
        }
        this.TUTORIAL_SCREENS_URL = sb4.append(str6).append("swipe-screens/features_full.html").toString();
        if (USE_DEV) {
            sb5 = new StringBuilder();
            str7 = BASE_URL_DEV;
        } else {
            sb5 = new StringBuilder();
            str7 = BASE_URL_PROD;
        }
        this.WIFI_REMOTE_URL = sb5.append(str7).append("android_stream.html?remoteOnly=1").toString();
        if (USE_DEV) {
            sb6 = new StringBuilder();
            str8 = BASE_URL_DEV;
        } else {
            sb6 = new StringBuilder();
            str8 = BASE_URL_PROD;
        }
        this.CAST_REMOTE_URL = sb6.append(str8).append("android_stream.html?castRemoteOnly=1").toString();
        if (USE_DEV) {
            sb7 = new StringBuilder();
            str9 = BASE_URL_DEV;
        } else {
            sb7 = new StringBuilder();
            str9 = BASE_URL_PROD;
        }
        this.PHYSICAL_CONTROLLER_SETUP_URL = sb7.append(str9).append("physical_controller/setup.html").toString();
        if (USE_DEV) {
            sb8 = new StringBuilder();
            str10 = BASE_URL_DEV;
        } else {
            sb8 = new StringBuilder();
            str10 = BASE_URL_PROD;
        }
        this.LOOKUP_TVCODE_BASE_URL = sb8.append(str10).append("aws/tv_code").toString();
        if (USE_DEV) {
            sb9 = new StringBuilder();
            str11 = BASE_URL_DEV;
        } else {
            sb9 = new StringBuilder();
            str11 = BASE_URL_PROD;
        }
        this.WEBOS_TVCODE_BASE_URL = sb9.append(str11).append("aws/webos/tv_code_tokens").toString();
        if (USE_DEV) {
            sb10 = new StringBuilder();
            str12 = BASE_URL_DEV;
        } else {
            sb10 = new StringBuilder();
            str12 = BASE_URL_PROD;
        }
        this.XCLOUD_GAME_PICKER = sb10.append(str12).append("title_picker.html").toString();
        if (USE_DEV) {
            sb11 = new StringBuilder();
            str13 = BASE_URL_DEV;
        } else {
            sb11 = new StringBuilder();
            str13 = BASE_URL_PROD;
        }
        this.VOICE_REMOTE_URL = sb11.append(str13).append("voice_commands.html").toString();
        if (USE_DEV) {
            sb12 = new StringBuilder();
            str14 = BASE_URL_DEV;
        } else {
            sb12 = new StringBuilder();
            str14 = BASE_URL_PROD;
        }
        this.WIDGET_INFO_URL = sb12.append(str14).append("swipe-screens/info-popup/features_widgets.html").toString();
        this.loadedStreamView = false;
        this.isXcloud = false;
        this.MAX_CALLS_PER_SECOND = 60;
        this.MIN_TIME_BETWEEN_CALLS_MS = 16L;
        this.lastCallTime = 0L;
        this.movementXValue = 0.0f;
        this.movementYValue = 0.0f;
        this.webviewListener = new StreamWebviewListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.1
            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void onReLoginRequest() {
                ((Activity) ApiClient.this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Web.ApiClient.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        ApiClient.this.listener.onReLoginDetected();
                    }
                });
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void closeScreen() {
                ApiClient.this.listener.onCloseScreenDetected();
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void pressButtonWifiRemote(String str15) {
                ApiClient.this.listener.pressButtonWifiRemote(str15);
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void setOrientationValue(String str15) {
                ApiClient.this.listener.setOrientationValue(str15);
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void vibrate() {
                ApiClient.this.listener.vibrate();
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void genericMessage(String str15, final String str16) {
                if (str15.equals("rumble")) {
                    ((Activity) ApiClient.this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Web.ApiClient.1.2
                        @Override // java.lang.Runnable
                        public void run() {
                            if (ApiClient.this.controllerHandler != null) {
                                try {
                                    ApiClient.this.controllerHandler.handleRumble(str16);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } else if (str15.equals("pointerLock")) {
                    ApiClient.this.togglePointerLock(str16);
                } else {
                    ApiClient.this.listener.genericMessage(str15, str16);
                }
            }
        };
        this.controllerHandlerListener = new ControllerHandler.ControllerHandlerListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.2
            @Override // com.studio08.xbgamestream.Controller.ControllerHandler.ControllerHandlerListener
            public void controllerData(JSONObject jSONObject) {
                if (ApiClient.this.geckoWebviewClient != null) {
                    ApiClient.this.geckoWebviewClient.sendControllerInput(jSONObject);
                }
            }
        };
        this.context = context;
        this.listener = null;
        this.streamWebview = streamWebview;
        this.gsToken = str;
        this.serverId = str2;
        this.isXcloud = Boolean.valueOf(z);
        setWebClient();
    }

    public ApiClient(Context context, GeckoView geckoView, String str, String str2, boolean z) {
        StringBuilder sb;
        String str3;
        StringBuilder sb2;
        String str4;
        StringBuilder sb3;
        String str5;
        StringBuilder sb4;
        String str6;
        StringBuilder sb5;
        String str7;
        StringBuilder sb6;
        String str8;
        StringBuilder sb7;
        String str9;
        StringBuilder sb8;
        String str10;
        StringBuilder sb9;
        String str11;
        StringBuilder sb10;
        String str12;
        StringBuilder sb11;
        String str13;
        StringBuilder sb12;
        String str14;
        if (USE_DEV) {
            sb = new StringBuilder();
            str3 = BASE_URL_DEV;
        } else {
            sb = new StringBuilder();
            str3 = BASE_URL_PROD;
        }
        this.CONTROLLER_URL = sb.append(str3).append("android_stream.html?controllerOnly=1").toString();
        if (USE_DEV) {
            sb2 = new StringBuilder();
            str4 = BASE_URL_DEV;
        } else {
            sb2 = new StringBuilder();
            str4 = BASE_URL_PROD;
        }
        this.STREAMING_ONLY_URL = sb2.append(str4).append("android_stream.html?stream=1").toString();
        if (USE_DEV) {
            sb3 = new StringBuilder();
            str5 = BASE_URL_DEV;
        } else {
            sb3 = new StringBuilder();
            str5 = BASE_URL_PROD;
        }
        this.CONTROLLER_BUILDER_URL = sb3.append(str5).append("builder/controller_builder.html").toString();
        if (USE_DEV) {
            sb4 = new StringBuilder();
            str6 = BASE_URL_DEV;
        } else {
            sb4 = new StringBuilder();
            str6 = BASE_URL_PROD;
        }
        this.TUTORIAL_SCREENS_URL = sb4.append(str6).append("swipe-screens/features_full.html").toString();
        if (USE_DEV) {
            sb5 = new StringBuilder();
            str7 = BASE_URL_DEV;
        } else {
            sb5 = new StringBuilder();
            str7 = BASE_URL_PROD;
        }
        this.WIFI_REMOTE_URL = sb5.append(str7).append("android_stream.html?remoteOnly=1").toString();
        if (USE_DEV) {
            sb6 = new StringBuilder();
            str8 = BASE_URL_DEV;
        } else {
            sb6 = new StringBuilder();
            str8 = BASE_URL_PROD;
        }
        this.CAST_REMOTE_URL = sb6.append(str8).append("android_stream.html?castRemoteOnly=1").toString();
        if (USE_DEV) {
            sb7 = new StringBuilder();
            str9 = BASE_URL_DEV;
        } else {
            sb7 = new StringBuilder();
            str9 = BASE_URL_PROD;
        }
        this.PHYSICAL_CONTROLLER_SETUP_URL = sb7.append(str9).append("physical_controller/setup.html").toString();
        if (USE_DEV) {
            sb8 = new StringBuilder();
            str10 = BASE_URL_DEV;
        } else {
            sb8 = new StringBuilder();
            str10 = BASE_URL_PROD;
        }
        this.LOOKUP_TVCODE_BASE_URL = sb8.append(str10).append("aws/tv_code").toString();
        if (USE_DEV) {
            sb9 = new StringBuilder();
            str11 = BASE_URL_DEV;
        } else {
            sb9 = new StringBuilder();
            str11 = BASE_URL_PROD;
        }
        this.WEBOS_TVCODE_BASE_URL = sb9.append(str11).append("aws/webos/tv_code_tokens").toString();
        if (USE_DEV) {
            sb10 = new StringBuilder();
            str12 = BASE_URL_DEV;
        } else {
            sb10 = new StringBuilder();
            str12 = BASE_URL_PROD;
        }
        this.XCLOUD_GAME_PICKER = sb10.append(str12).append("title_picker.html").toString();
        if (USE_DEV) {
            sb11 = new StringBuilder();
            str13 = BASE_URL_DEV;
        } else {
            sb11 = new StringBuilder();
            str13 = BASE_URL_PROD;
        }
        this.VOICE_REMOTE_URL = sb11.append(str13).append("voice_commands.html").toString();
        if (USE_DEV) {
            sb12 = new StringBuilder();
            str14 = BASE_URL_DEV;
        } else {
            sb12 = new StringBuilder();
            str14 = BASE_URL_PROD;
        }
        this.WIDGET_INFO_URL = sb12.append(str14).append("swipe-screens/info-popup/features_widgets.html").toString();
        this.loadedStreamView = false;
        this.isXcloud = false;
        this.MAX_CALLS_PER_SECOND = 60;
        this.MIN_TIME_BETWEEN_CALLS_MS = 16L;
        this.lastCallTime = 0L;
        this.movementXValue = 0.0f;
        this.movementYValue = 0.0f;
        this.webviewListener = new StreamWebviewListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.1
            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void onReLoginRequest() {
                ((Activity) ApiClient.this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Web.ApiClient.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        ApiClient.this.listener.onReLoginDetected();
                    }
                });
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void closeScreen() {
                ApiClient.this.listener.onCloseScreenDetected();
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void pressButtonWifiRemote(String str15) {
                ApiClient.this.listener.pressButtonWifiRemote(str15);
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void setOrientationValue(String str15) {
                ApiClient.this.listener.setOrientationValue(str15);
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void vibrate() {
                ApiClient.this.listener.vibrate();
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void genericMessage(String str15, final String str16) {
                if (str15.equals("rumble")) {
                    ((Activity) ApiClient.this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Web.ApiClient.1.2
                        @Override // java.lang.Runnable
                        public void run() {
                            if (ApiClient.this.controllerHandler != null) {
                                try {
                                    ApiClient.this.controllerHandler.handleRumble(str16);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } else if (str15.equals("pointerLock")) {
                    ApiClient.this.togglePointerLock(str16);
                } else {
                    ApiClient.this.listener.genericMessage(str15, str16);
                }
            }
        };
        this.controllerHandlerListener = new ControllerHandler.ControllerHandlerListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.2
            @Override // com.studio08.xbgamestream.Controller.ControllerHandler.ControllerHandlerListener
            public void controllerData(JSONObject jSONObject) {
                if (ApiClient.this.geckoWebviewClient != null) {
                    ApiClient.this.geckoWebviewClient.sendControllerInput(jSONObject);
                }
            }
        };
        this.context = context;
        this.listener = null;
        this.geckoStreamWebview = geckoView;
        this.gsToken = str;
        this.serverId = str2;
        this.isXcloud = Boolean.valueOf(z);
        setGeckoWebClient();
    }

    public ApiClient(Context context, StreamWebview streamWebview) {
        StringBuilder sb;
        String str;
        StringBuilder sb2;
        String str2;
        StringBuilder sb3;
        String str3;
        StringBuilder sb4;
        String str4;
        StringBuilder sb5;
        String str5;
        StringBuilder sb6;
        String str6;
        StringBuilder sb7;
        String str7;
        StringBuilder sb8;
        String str8;
        StringBuilder sb9;
        String str9;
        StringBuilder sb10;
        String str10;
        StringBuilder sb11;
        String str11;
        StringBuilder sb12;
        String str12;
        if (USE_DEV) {
            sb = new StringBuilder();
            str = BASE_URL_DEV;
        } else {
            sb = new StringBuilder();
            str = BASE_URL_PROD;
        }
        this.CONTROLLER_URL = sb.append(str).append("android_stream.html?controllerOnly=1").toString();
        if (USE_DEV) {
            sb2 = new StringBuilder();
            str2 = BASE_URL_DEV;
        } else {
            sb2 = new StringBuilder();
            str2 = BASE_URL_PROD;
        }
        this.STREAMING_ONLY_URL = sb2.append(str2).append("android_stream.html?stream=1").toString();
        if (USE_DEV) {
            sb3 = new StringBuilder();
            str3 = BASE_URL_DEV;
        } else {
            sb3 = new StringBuilder();
            str3 = BASE_URL_PROD;
        }
        this.CONTROLLER_BUILDER_URL = sb3.append(str3).append("builder/controller_builder.html").toString();
        if (USE_DEV) {
            sb4 = new StringBuilder();
            str4 = BASE_URL_DEV;
        } else {
            sb4 = new StringBuilder();
            str4 = BASE_URL_PROD;
        }
        this.TUTORIAL_SCREENS_URL = sb4.append(str4).append("swipe-screens/features_full.html").toString();
        if (USE_DEV) {
            sb5 = new StringBuilder();
            str5 = BASE_URL_DEV;
        } else {
            sb5 = new StringBuilder();
            str5 = BASE_URL_PROD;
        }
        this.WIFI_REMOTE_URL = sb5.append(str5).append("android_stream.html?remoteOnly=1").toString();
        if (USE_DEV) {
            sb6 = new StringBuilder();
            str6 = BASE_URL_DEV;
        } else {
            sb6 = new StringBuilder();
            str6 = BASE_URL_PROD;
        }
        this.CAST_REMOTE_URL = sb6.append(str6).append("android_stream.html?castRemoteOnly=1").toString();
        if (USE_DEV) {
            sb7 = new StringBuilder();
            str7 = BASE_URL_DEV;
        } else {
            sb7 = new StringBuilder();
            str7 = BASE_URL_PROD;
        }
        this.PHYSICAL_CONTROLLER_SETUP_URL = sb7.append(str7).append("physical_controller/setup.html").toString();
        if (USE_DEV) {
            sb8 = new StringBuilder();
            str8 = BASE_URL_DEV;
        } else {
            sb8 = new StringBuilder();
            str8 = BASE_URL_PROD;
        }
        this.LOOKUP_TVCODE_BASE_URL = sb8.append(str8).append("aws/tv_code").toString();
        if (USE_DEV) {
            sb9 = new StringBuilder();
            str9 = BASE_URL_DEV;
        } else {
            sb9 = new StringBuilder();
            str9 = BASE_URL_PROD;
        }
        this.WEBOS_TVCODE_BASE_URL = sb9.append(str9).append("aws/webos/tv_code_tokens").toString();
        if (USE_DEV) {
            sb10 = new StringBuilder();
            str10 = BASE_URL_DEV;
        } else {
            sb10 = new StringBuilder();
            str10 = BASE_URL_PROD;
        }
        this.XCLOUD_GAME_PICKER = sb10.append(str10).append("title_picker.html").toString();
        if (USE_DEV) {
            sb11 = new StringBuilder();
            str11 = BASE_URL_DEV;
        } else {
            sb11 = new StringBuilder();
            str11 = BASE_URL_PROD;
        }
        this.VOICE_REMOTE_URL = sb11.append(str11).append("voice_commands.html").toString();
        if (USE_DEV) {
            sb12 = new StringBuilder();
            str12 = BASE_URL_DEV;
        } else {
            sb12 = new StringBuilder();
            str12 = BASE_URL_PROD;
        }
        this.WIDGET_INFO_URL = sb12.append(str12).append("swipe-screens/info-popup/features_widgets.html").toString();
        this.loadedStreamView = false;
        this.isXcloud = false;
        this.MAX_CALLS_PER_SECOND = 60;
        this.MIN_TIME_BETWEEN_CALLS_MS = 16L;
        this.lastCallTime = 0L;
        this.movementXValue = 0.0f;
        this.movementYValue = 0.0f;
        this.webviewListener = new StreamWebviewListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.1
            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void onReLoginRequest() {
                ((Activity) ApiClient.this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Web.ApiClient.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        ApiClient.this.listener.onReLoginDetected();
                    }
                });
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void closeScreen() {
                ApiClient.this.listener.onCloseScreenDetected();
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void pressButtonWifiRemote(String str15) {
                ApiClient.this.listener.pressButtonWifiRemote(str15);
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void setOrientationValue(String str15) {
                ApiClient.this.listener.setOrientationValue(str15);
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void vibrate() {
                ApiClient.this.listener.vibrate();
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void genericMessage(String str15, final String str16) {
                if (str15.equals("rumble")) {
                    ((Activity) ApiClient.this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Web.ApiClient.1.2
                        @Override // java.lang.Runnable
                        public void run() {
                            if (ApiClient.this.controllerHandler != null) {
                                try {
                                    ApiClient.this.controllerHandler.handleRumble(str16);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } else if (str15.equals("pointerLock")) {
                    ApiClient.this.togglePointerLock(str16);
                } else {
                    ApiClient.this.listener.genericMessage(str15, str16);
                }
            }
        };
        this.controllerHandlerListener = new ControllerHandler.ControllerHandlerListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.2
            @Override // com.studio08.xbgamestream.Controller.ControllerHandler.ControllerHandlerListener
            public void controllerData(JSONObject jSONObject) {
                if (ApiClient.this.geckoWebviewClient != null) {
                    ApiClient.this.geckoWebviewClient.sendControllerInput(jSONObject);
                }
            }
        };
        this.context = context;
        this.streamWebview = streamWebview;
        setWebClient();
    }

    public ApiClient(Context context, GeckoView geckoView) {
        StringBuilder sb;
        String str;
        StringBuilder sb2;
        String str2;
        StringBuilder sb3;
        String str3;
        StringBuilder sb4;
        String str4;
        StringBuilder sb5;
        String str5;
        StringBuilder sb6;
        String str6;
        StringBuilder sb7;
        String str7;
        StringBuilder sb8;
        String str8;
        StringBuilder sb9;
        String str9;
        StringBuilder sb10;
        String str10;
        StringBuilder sb11;
        String str11;
        StringBuilder sb12;
        String str12;
        if (USE_DEV) {
            sb = new StringBuilder();
            str = BASE_URL_DEV;
        } else {
            sb = new StringBuilder();
            str = BASE_URL_PROD;
        }
        this.CONTROLLER_URL = sb.append(str).append("android_stream.html?controllerOnly=1").toString();
        if (USE_DEV) {
            sb2 = new StringBuilder();
            str2 = BASE_URL_DEV;
        } else {
            sb2 = new StringBuilder();
            str2 = BASE_URL_PROD;
        }
        this.STREAMING_ONLY_URL = sb2.append(str2).append("android_stream.html?stream=1").toString();
        if (USE_DEV) {
            sb3 = new StringBuilder();
            str3 = BASE_URL_DEV;
        } else {
            sb3 = new StringBuilder();
            str3 = BASE_URL_PROD;
        }
        this.CONTROLLER_BUILDER_URL = sb3.append(str3).append("builder/controller_builder.html").toString();
        if (USE_DEV) {
            sb4 = new StringBuilder();
            str4 = BASE_URL_DEV;
        } else {
            sb4 = new StringBuilder();
            str4 = BASE_URL_PROD;
        }
        this.TUTORIAL_SCREENS_URL = sb4.append(str4).append("swipe-screens/features_full.html").toString();
        if (USE_DEV) {
            sb5 = new StringBuilder();
            str5 = BASE_URL_DEV;
        } else {
            sb5 = new StringBuilder();
            str5 = BASE_URL_PROD;
        }
        this.WIFI_REMOTE_URL = sb5.append(str5).append("android_stream.html?remoteOnly=1").toString();
        if (USE_DEV) {
            sb6 = new StringBuilder();
            str6 = BASE_URL_DEV;
        } else {
            sb6 = new StringBuilder();
            str6 = BASE_URL_PROD;
        }
        this.CAST_REMOTE_URL = sb6.append(str6).append("android_stream.html?castRemoteOnly=1").toString();
        if (USE_DEV) {
            sb7 = new StringBuilder();
            str7 = BASE_URL_DEV;
        } else {
            sb7 = new StringBuilder();
            str7 = BASE_URL_PROD;
        }
        this.PHYSICAL_CONTROLLER_SETUP_URL = sb7.append(str7).append("physical_controller/setup.html").toString();
        if (USE_DEV) {
            sb8 = new StringBuilder();
            str8 = BASE_URL_DEV;
        } else {
            sb8 = new StringBuilder();
            str8 = BASE_URL_PROD;
        }
        this.LOOKUP_TVCODE_BASE_URL = sb8.append(str8).append("aws/tv_code").toString();
        if (USE_DEV) {
            sb9 = new StringBuilder();
            str9 = BASE_URL_DEV;
        } else {
            sb9 = new StringBuilder();
            str9 = BASE_URL_PROD;
        }
        this.WEBOS_TVCODE_BASE_URL = sb9.append(str9).append("aws/webos/tv_code_tokens").toString();
        if (USE_DEV) {
            sb10 = new StringBuilder();
            str10 = BASE_URL_DEV;
        } else {
            sb10 = new StringBuilder();
            str10 = BASE_URL_PROD;
        }
        this.XCLOUD_GAME_PICKER = sb10.append(str10).append("title_picker.html").toString();
        if (USE_DEV) {
            sb11 = new StringBuilder();
            str11 = BASE_URL_DEV;
        } else {
            sb11 = new StringBuilder();
            str11 = BASE_URL_PROD;
        }
        this.VOICE_REMOTE_URL = sb11.append(str11).append("voice_commands.html").toString();
        if (USE_DEV) {
            sb12 = new StringBuilder();
            str12 = BASE_URL_DEV;
        } else {
            sb12 = new StringBuilder();
            str12 = BASE_URL_PROD;
        }
        this.WIDGET_INFO_URL = sb12.append(str12).append("swipe-screens/info-popup/features_widgets.html").toString();
        this.loadedStreamView = false;
        this.isXcloud = false;
        this.MAX_CALLS_PER_SECOND = 60;
        this.MIN_TIME_BETWEEN_CALLS_MS = 16L;
        this.lastCallTime = 0L;
        this.movementXValue = 0.0f;
        this.movementYValue = 0.0f;
        this.webviewListener = new StreamWebviewListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.1
            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void onReLoginRequest() {
                ((Activity) ApiClient.this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Web.ApiClient.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        ApiClient.this.listener.onReLoginDetected();
                    }
                });
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void closeScreen() {
                ApiClient.this.listener.onCloseScreenDetected();
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void pressButtonWifiRemote(String str15) {
                ApiClient.this.listener.pressButtonWifiRemote(str15);
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void setOrientationValue(String str15) {
                ApiClient.this.listener.setOrientationValue(str15);
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void vibrate() {
                ApiClient.this.listener.vibrate();
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void genericMessage(String str15, final String str16) {
                if (str15.equals("rumble")) {
                    ((Activity) ApiClient.this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Web.ApiClient.1.2
                        @Override // java.lang.Runnable
                        public void run() {
                            if (ApiClient.this.controllerHandler != null) {
                                try {
                                    ApiClient.this.controllerHandler.handleRumble(str16);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } else if (str15.equals("pointerLock")) {
                    ApiClient.this.togglePointerLock(str16);
                } else {
                    ApiClient.this.listener.genericMessage(str15, str16);
                }
            }
        };
        this.controllerHandlerListener = new ControllerHandler.ControllerHandlerListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.2
            @Override // com.studio08.xbgamestream.Controller.ControllerHandler.ControllerHandlerListener
            public void controllerData(JSONObject jSONObject) {
                if (ApiClient.this.geckoWebviewClient != null) {
                    ApiClient.this.geckoWebviewClient.sendControllerInput(jSONObject);
                }
            }
        };
        this.context = context;
        this.geckoStreamWebview = geckoView;
        setGeckoWebClient();
    }

    public ApiClient(Context context) {
        StringBuilder sb;
        String str;
        StringBuilder sb2;
        String str2;
        StringBuilder sb3;
        String str3;
        StringBuilder sb4;
        String str4;
        StringBuilder sb5;
        String str5;
        StringBuilder sb6;
        String str6;
        StringBuilder sb7;
        String str7;
        StringBuilder sb8;
        String str8;
        StringBuilder sb9;
        String str9;
        StringBuilder sb10;
        String str10;
        StringBuilder sb11;
        String str11;
        StringBuilder sb12;
        String str12;
        if (USE_DEV) {
            sb = new StringBuilder();
            str = BASE_URL_DEV;
        } else {
            sb = new StringBuilder();
            str = BASE_URL_PROD;
        }
        this.CONTROLLER_URL = sb.append(str).append("android_stream.html?controllerOnly=1").toString();
        if (USE_DEV) {
            sb2 = new StringBuilder();
            str2 = BASE_URL_DEV;
        } else {
            sb2 = new StringBuilder();
            str2 = BASE_URL_PROD;
        }
        this.STREAMING_ONLY_URL = sb2.append(str2).append("android_stream.html?stream=1").toString();
        if (USE_DEV) {
            sb3 = new StringBuilder();
            str3 = BASE_URL_DEV;
        } else {
            sb3 = new StringBuilder();
            str3 = BASE_URL_PROD;
        }
        this.CONTROLLER_BUILDER_URL = sb3.append(str3).append("builder/controller_builder.html").toString();
        if (USE_DEV) {
            sb4 = new StringBuilder();
            str4 = BASE_URL_DEV;
        } else {
            sb4 = new StringBuilder();
            str4 = BASE_URL_PROD;
        }
        this.TUTORIAL_SCREENS_URL = sb4.append(str4).append("swipe-screens/features_full.html").toString();
        if (USE_DEV) {
            sb5 = new StringBuilder();
            str5 = BASE_URL_DEV;
        } else {
            sb5 = new StringBuilder();
            str5 = BASE_URL_PROD;
        }
        this.WIFI_REMOTE_URL = sb5.append(str5).append("android_stream.html?remoteOnly=1").toString();
        if (USE_DEV) {
            sb6 = new StringBuilder();
            str6 = BASE_URL_DEV;
        } else {
            sb6 = new StringBuilder();
            str6 = BASE_URL_PROD;
        }
        this.CAST_REMOTE_URL = sb6.append(str6).append("android_stream.html?castRemoteOnly=1").toString();
        if (USE_DEV) {
            sb7 = new StringBuilder();
            str7 = BASE_URL_DEV;
        } else {
            sb7 = new StringBuilder();
            str7 = BASE_URL_PROD;
        }
        this.PHYSICAL_CONTROLLER_SETUP_URL = sb7.append(str7).append("physical_controller/setup.html").toString();
        if (USE_DEV) {
            sb8 = new StringBuilder();
            str8 = BASE_URL_DEV;
        } else {
            sb8 = new StringBuilder();
            str8 = BASE_URL_PROD;
        }
        this.LOOKUP_TVCODE_BASE_URL = sb8.append(str8).append("aws/tv_code").toString();
        if (USE_DEV) {
            sb9 = new StringBuilder();
            str9 = BASE_URL_DEV;
        } else {
            sb9 = new StringBuilder();
            str9 = BASE_URL_PROD;
        }
        this.WEBOS_TVCODE_BASE_URL = sb9.append(str9).append("aws/webos/tv_code_tokens").toString();
        if (USE_DEV) {
            sb10 = new StringBuilder();
            str10 = BASE_URL_DEV;
        } else {
            sb10 = new StringBuilder();
            str10 = BASE_URL_PROD;
        }
        this.XCLOUD_GAME_PICKER = sb10.append(str10).append("title_picker.html").toString();
        if (USE_DEV) {
            sb11 = new StringBuilder();
            str11 = BASE_URL_DEV;
        } else {
            sb11 = new StringBuilder();
            str11 = BASE_URL_PROD;
        }
        this.VOICE_REMOTE_URL = sb11.append(str11).append("voice_commands.html").toString();
        if (USE_DEV) {
            sb12 = new StringBuilder();
            str12 = BASE_URL_DEV;
        } else {
            sb12 = new StringBuilder();
            str12 = BASE_URL_PROD;
        }
        this.WIDGET_INFO_URL = sb12.append(str12).append("swipe-screens/info-popup/features_widgets.html").toString();
        this.loadedStreamView = false;
        this.isXcloud = false;
        this.MAX_CALLS_PER_SECOND = 60;
        this.MIN_TIME_BETWEEN_CALLS_MS = 16L;
        this.lastCallTime = 0L;
        this.movementXValue = 0.0f;
        this.movementYValue = 0.0f;
        this.webviewListener = new StreamWebviewListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.1
            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void onReLoginRequest() {
                ((Activity) ApiClient.this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Web.ApiClient.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        ApiClient.this.listener.onReLoginDetected();
                    }
                });
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void closeScreen() {
                ApiClient.this.listener.onCloseScreenDetected();
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void pressButtonWifiRemote(String str15) {
                ApiClient.this.listener.pressButtonWifiRemote(str15);
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void setOrientationValue(String str15) {
                ApiClient.this.listener.setOrientationValue(str15);
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void vibrate() {
                ApiClient.this.listener.vibrate();
            }

            @Override // com.studio08.xbgamestream.Web.StreamWebviewListener
            public void genericMessage(String str15, final String str16) {
                if (str15.equals("rumble")) {
                    ((Activity) ApiClient.this.context).runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.Web.ApiClient.1.2
                        @Override // java.lang.Runnable
                        public void run() {
                            if (ApiClient.this.controllerHandler != null) {
                                try {
                                    ApiClient.this.controllerHandler.handleRumble(str16);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } else if (str15.equals("pointerLock")) {
                    ApiClient.this.togglePointerLock(str16);
                } else {
                    ApiClient.this.listener.genericMessage(str15, str16);
                }
            }
        };
        this.controllerHandlerListener = new ControllerHandler.ControllerHandlerListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.2
            @Override // com.studio08.xbgamestream.Controller.ControllerHandler.ControllerHandlerListener
            public void controllerData(JSONObject jSONObject) {
                if (ApiClient.this.geckoWebviewClient != null) {
                    ApiClient.this.geckoWebviewClient.sendControllerInput(jSONObject);
                }
            }
        };
        this.context = context;
    }

    public void setCustomObjectListener(StreamingClientListener streamingClientListener) {
        this.listener = streamingClientListener;
    }

    private void setGeckoWebClient() {
        this.geckoWebviewClient = new GeckoWebviewClient(this.context, this.geckoStreamWebview, this);
        setPointerCaptureListener();
    }

    private void setWebClient() {
        this.webviewStreamStartClient = new CustomWebClient(this.streamWebview.showLoadingDialog.booleanValue()) { // from class: com.studio08.xbgamestream.Web.ApiClient.3
            @Override // com.studio08.xbgamestream.Web.CustomWebClient, android.webkit.WebViewClient
            public void onPageFinished(WebView webView, String str) {
                super.onPageFinished(webView, str);
                ApiClient.this.setStreamConfig();
                ApiClient.this.loadedStreamView = true;
            }
        };
        setPointerCaptureListener();
    }

    void setPointerCaptureListener() {
        if (Build.VERSION.SDK_INT >= 26) {
            StreamWebview streamWebview = this.streamWebview;
            if (streamWebview != null) {
                streamWebview.setOnCapturedPointerListener(new View.OnCapturedPointerListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.4
                    @Override // android.view.View.OnCapturedPointerListener
                    public boolean onCapturedPointer(View view, MotionEvent motionEvent) {
                        return ApiClient.this.handleMouseMotionEvent(motionEvent);
                    }
                });
                return;
            }
            GeckoView geckoView = this.geckoStreamWebview;
            if (geckoView != null) {
                geckoView.setOnCapturedPointerListener(new View.OnCapturedPointerListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.5
                    @Override // android.view.View.OnCapturedPointerListener
                    public boolean onCapturedPointer(View view, MotionEvent motionEvent) {
                        return ApiClient.this.handleMouseMotionEvent(motionEvent);
                    }
                });
            }
        }
    }

    public boolean handleMouseMotionEvent(MotionEvent motionEvent) {
        GeckoWebviewClient geckoWebviewClient;
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
                    jSONObject.put("movementX", (motionEvent.getX() + this.movementXValue) * 4.0f);
                    jSONObject.put("movementY", (motionEvent.getY() + this.movementYValue) * 4.0f);
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
            StreamWebview streamWebview = this.streamWebview;
            if (streamWebview != null) {
                callJavaScript(streamWebview, "setMousePayload", jSONObject.toString());
            } else if (this.geckoStreamWebview != null && (geckoWebviewClient = this.geckoWebviewClient) != null) {
                geckoWebviewClient.sendMouseInput(jSONObject);
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

    public void doStreamingOnly() {
        this.streamWebview.setWebViewClient(this.webviewStreamStartClient);
        this.streamWebview.setCustomObjectListener(this.webviewListener);
        this.streamWebview.loadUrl(this.STREAMING_ONLY_URL);
    }

    public void doXcloudGamePicker() {
        if (this.geckoWebviewClient != null) {
            Log.e("APICLIENT", "Using geckoview to stream");
            this.geckoWebviewClient.setCustomObjectListener(this.webviewListener);
            this.geckoWebviewClient.loadUrl(this.XCLOUD_GAME_PICKER + "?xcloudToken=" + this.gsToken + "&use_v2=1");
            return;
        }
        this.streamWebview.setWebViewClient(this.webviewStreamStartClient);
        this.streamWebview.setCustomObjectListener(this.webviewListener);
        this.streamWebview.loadUrl(this.XCLOUD_GAME_PICKER + "?xcloudToken=" + this.gsToken + "&use_v2=1");
    }

    public void doStreamingNoConnect() {
        TWAClient tWAClient = new TWAClient(this.context, getConfigSettings());
        if (tWAClient.getShouldUseTWA()) {
            Log.e("APICLIENT", "Using TWA to stream");
            if (BuildConfig.FLAVOR.equals("firetv")) {
                Toast.makeText(this.context, "Double tap the menu button (then press back) to enter fullscreen mode", 0).show();
            }
            tWAClient.launchTWSA(this.STREAMING_ONLY_URL + "&disableConnectClick=1");
        } else if (this.geckoWebviewClient != null) {
            Log.e("APICLIENT", "Using geckoview to stream");
            this.geckoWebviewClient.setCustomObjectListener(this.webviewListener);
            this.geckoWebviewClient.loadUrl(this.STREAMING_ONLY_URL + "&disableConnectClick=1");
        } else {
            Log.e("APICLIENT", "Using system webview");
            this.streamWebview.setWebViewClient(this.webviewStreamStartClient);
            this.streamWebview.setCustomObjectListener(this.webviewListener);
            this.streamWebview.loadUrl(this.STREAMING_ONLY_URL + "&disableConnectClick=1");
        }
    }

    public void doStreaming() {
        TWAClient tWAClient = new TWAClient(this.context, getConfigSettings());
        if (tWAClient.getShouldUseTWA()) {
            Log.e("APICLIENT", "Using TWA to stream");
            tWAClient.launchTWSA(STREAMING_URL);
        } else if (this.geckoWebviewClient != null) {
            Log.e("APICLIENT", "Using geckoview to stream");
            this.geckoWebviewClient.setCustomObjectListener(this.webviewListener);
            this.geckoWebviewClient.loadUrl(STREAMING_URL);
        } else {
            Log.e("APICLIENT", "Using system webview");
            this.streamWebview.setWebViewClient(this.webviewStreamStartClient);
            this.streamWebview.setCustomObjectListener(this.webviewListener);
            this.streamWebview.loadUrl(STREAMING_URL);
        }
    }

    public void doController() {
        TWAClient tWAClient = new TWAClient(this.context, getConfigSettings());
        if (tWAClient.getShouldUseTWA()) {
            Log.e("APICLIENT", "Using TWA to stream");
            tWAClient.launchTWSA(this.CONTROLLER_URL);
        } else if (this.geckoWebviewClient != null) {
            Log.e("APICLIENT", "Using geckoview to stream");
            this.geckoWebviewClient.setCustomObjectListener(this.webviewListener);
            this.geckoWebviewClient.loadUrl(this.CONTROLLER_URL);
        } else {
            Log.e("APICLIENT", "Using system webview");
            this.streamWebview.setWebViewClient(this.webviewStreamStartClient);
            this.streamWebview.setCustomObjectListener(this.webviewListener);
            this.streamWebview.loadUrl(this.CONTROLLER_URL);
        }
    }

    public void doControllerBuilder(String str) {
        String str2;
        String str3;
        String str4;
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("SettingsSharedPref", 0);
        boolean z = sharedPreferences.getBoolean("build_snap_grid_key", false);
        String string = sharedPreferences.getString("mini_gamepad_size_key", "30");
        if (!z) {
            str2 = "";
        } else {
            int i = sharedPreferences.getInt("build_grid_size_key", 20);
            if (i < 5) {
                i = 5;
            }
            str2 = "?snap_to_grid=true&snap_to_grid_size=" + i;
        }
        if (str != null) {
            if (!z) {
                str4 = str2 + "?";
            } else {
                str4 = str2 + "&";
            }
            str2 = str4 + "customType=" + str;
            if (!BuildConfig.FLAVOR.equals(BuildConfig.FLAVOR) && !BuildConfig.FLAVOR.equals("legacy")) {
                str2 = str2 + "&disableTypeSwitch=true";
            }
        }
        if (!str2.contains("?")) {
            str3 = str2 + "?miniGamepadSize=" + string;
        } else {
            str3 = str2 + "&miniGamepadSize=" + string;
        }
        TWAClient tWAClient = new TWAClient(this.context, getConfigSettings());
        if (tWAClient.getShouldUseTWA()) {
            Log.e("APICLIENT", "Using TWA to stream");
            tWAClient.launchTWSA(this.CONTROLLER_BUILDER_URL + str3);
        } else if (this.geckoWebviewClient != null) {
            Log.e("APICLIENT", "Using geckoview to stream");
            this.geckoWebviewClient.setCustomObjectListener(this.webviewListener);
            this.geckoWebviewClient.loadUrl(this.CONTROLLER_BUILDER_URL + str3);
        } else {
            Log.e("APICLIENT", "Using system webview");
            this.streamWebview.setCustomObjectListener(this.webviewListener);
            this.streamWebview.loadUrl(this.CONTROLLER_BUILDER_URL + str3);
        }
    }

    public void doTutorialScreens(boolean z) {
        StringBuilder sb;
        String str;
        this.streamWebview.disableLoadingDialog();
        this.streamWebview.setCustomObjectListener(this.webviewListener);
        BuildConfig.FLAVOR.equals("legacy");
        if (USE_DEV) {
            sb = new StringBuilder();
            str = BASE_URL_DEV;
        } else {
            sb = new StringBuilder();
            str = BASE_URL_PROD;
        }
        String sb2 = sb.append(str).append("swipe-screens/features_").append(BuildConfig.FLAVOR).append(".html").toString();
        this.TUTORIAL_SCREENS_URL = sb2;
        this.streamWebview.loadUrl(sb2);
    }

    public void doWidgetTutorial() {
        this.streamWebview.disableLoadingDialog();
        this.streamWebview.setCustomObjectListener(this.webviewListener);
        this.streamWebview.loadUrl(this.WIDGET_INFO_URL);
    }

    public void doWifiVoiceRemote() {
        this.streamWebview.setCustomObjectListener(this.webviewListener);
        this.streamWebview.loadUrl(this.VOICE_REMOTE_URL);
    }

    public void doWifRemote() {
        this.streamWebview.setCustomObjectListener(this.webviewListener);
        this.streamWebview.loadUrl(this.WIFI_REMOTE_URL);
    }

    public void doCastRemote() {
        this.streamWebview.setCustomObjectListener(this.webviewListener);
        this.streamWebview.loadUrl(this.CAST_REMOTE_URL);
    }

    public void doPhysicalControllerSetup() {
        TWAClient tWAClient = new TWAClient(this.context, getConfigSettings());
        if (tWAClient.getShouldUseTWA()) {
            tWAClient.launchTWSA(this.PHYSICAL_CONTROLLER_SETUP_URL);
            return;
        }
        this.streamWebview.setCustomObjectListener(this.webviewListener);
        this.streamWebview.loadUrl(this.PHYSICAL_CONTROLLER_SETUP_URL);
    }

    public void doSaveTvCode(final Integer num, final String str) {
        try {
            Volley.newRequestQueue(this.context).add(new StringRequest(1, this.LOOKUP_TVCODE_BASE_URL, new Response.Listener<String>() { // from class: com.studio08.xbgamestream.Web.ApiClient.6
                @Override // com.android.volley.Response.Listener
                public void onResponse(String str2) {
                    Log.i("HERE", "Saved tv code");
                }
            }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.7
                @Override // com.android.volley.Response.ErrorListener
                public void onErrorResponse(VolleyError volleyError) {
                    Toast.makeText(ApiClient.this.context, "Network error. Companion app might not work..", 1).show();
                }
            }) { // from class: com.studio08.xbgamestream.Web.ApiClient.8
                @Override // com.android.volley.Request
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override // com.android.volley.Request
                public byte[] getBody() throws AuthFailureError {
                    try {
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("code", num.toString());
                        jSONObject.put("url", str);
                        return jSONObject.toString().getBytes(StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getToken() {
        try {
            final String value = new EncryptClient(this.context).getValue("gsToken");
            if (TextUtils.isEmpty(value)) {
                RewardedAdLoader.setPurchaseItem(false, this.context);
                return;
            }
            Volley.newRequestQueue(this.context).add(new StringRequest(1, TOKEN_GET_BASE_URL, new Response.Listener<String>() { // from class: com.studio08.xbgamestream.Web.ApiClient.9
                @Override // com.android.volley.Response.Listener
                public void onResponse(String str) {
                    JSONObject jSONObject;
                    try {
                        int i = new JSONObject(str).getInt("activePurchase");
                        boolean z = true;
                        if (i == 1 && !RewardedAdLoader.getPurchaseItem(ApiClient.this.context)) {
                            Toast.makeText(ApiClient.this.context, "License: " + jSONObject.getString(TJAdUnitConstants.String.MESSAGE), 1).show();
                        }
                        if (i != 1) {
                            z = false;
                        }
                        RewardedAdLoader.setPurchaseItem(z, ApiClient.this.context);
                        SharedPreferences.Editor edit = ApiClient.this.context.getSharedPreferences("SettingsSharedPref", 0).edit();
                        edit.putLong("nextMakeGetTokenRequest", System.currentTimeMillis() + RewardedAdLoader.GET_TOKEN_CACHE_DURATION);
                        edit.apply();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        RewardedAdLoader.setPurchaseItem(false, ApiClient.this.context);
                    }
                }
            }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.10
                @Override // com.android.volley.Response.ErrorListener
                public void onErrorResponse(VolleyError volleyError) {
                    Toast.makeText(ApiClient.this.context, "Network error.", 0).show();
                    RewardedAdLoader.setPurchaseItem(false, ApiClient.this.context);
                }
            }) { // from class: com.studio08.xbgamestream.Web.ApiClient.11
                @Override // com.android.volley.Request
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override // com.android.volley.Request
                public byte[] getBody() throws AuthFailureError {
                    try {
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("gsToken", value);
                        return jSONObject.toString().getBytes(StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            RewardedAdLoader.setPurchaseItem(false, this.context);
        }
    }

    public void sendToken(final Purchase purchase) {
        try {
            final EncryptClient encryptClient = new EncryptClient(this.context);
            final String value = encryptClient.getValue("gsToken");
            if (TextUtils.isEmpty(value)) {
                Log.e("ApiClient", "Ignore sendToken due to empty gsToken");
                return;
            }
            Volley.newRequestQueue(this.context).add(new StringRequest(1, TOKEN_SAVE_BASE_URL, new Response.Listener<String>() { // from class: com.studio08.xbgamestream.Web.ApiClient.12
                @Override // com.android.volley.Response.Listener
                public void onResponse(String str) {
                    try {
                        JSONObject jSONObject = new JSONObject(str);
                        encryptClient.saveValue("purchaseToken", jSONObject.getString("purchaseToken"));
                        encryptClient.saveValue("gamertag", jSONObject.getString("gamertag"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.13
                @Override // com.android.volley.Response.ErrorListener
                public void onErrorResponse(VolleyError volleyError) {
                    Toast.makeText(ApiClient.this.context, "Network error.", 0).show();
                }
            }) { // from class: com.studio08.xbgamestream.Web.ApiClient.14
                @Override // com.android.volley.Request
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override // com.android.volley.Request
                public byte[] getBody() throws AuthFailureError {
                    try {
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("purchaseToken", purchase.getPurchaseToken());
                        jSONObject.put("purchaseTime", purchase.getPurchaseTime());
                        jSONObject.put("packageName", purchase.getPackageName());
                        jSONObject.put(AppLovinEventTypes.USER_VIEWED_PRODUCT, purchase.getProducts().get(0));
                        jSONObject.put("orderId", purchase.getOrderId());
                        jSONObject.put("gsToken", value);
                        return jSONObject.toString().getBytes(StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doLookupTvCode(final String str) {
        try {
            if (!TextUtils.isEmpty(str) && str.length() == 6) {
                EncryptClient encryptClient = new EncryptClient(this.context);
                encryptClient.getValue("serverId");
                if (TextUtils.isEmpty(encryptClient.getValue("gsToken"))) {
                    createPopup("Sign-in Required", "You must sign-in to your Xbox Live account first. Click the Sign-in button above, then try again.");
                    return;
                }
                Toast.makeText(this.context, "Connecting to TV. Please Wait...", 0).show();
                Volley.newRequestQueue(this.context).add(new StringRequest(0, this.LOOKUP_TVCODE_BASE_URL + RemoteSettings.FORWARD_SLASH_STRING + str, new Response.Listener<String>() { // from class: com.studio08.xbgamestream.Web.ApiClient.15
                    @Override // com.android.volley.Response.Listener
                    public void onResponse(String str2) {
                        Log.i("HERE", "Got tv code");
                        ApiClient.this.startTvStream(str2);
                    }
                }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.16
                    @Override // com.android.volley.Response.ErrorListener
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError.networkResponse != null && volleyError.networkResponse.statusCode == 404) {
                            ApiClient.this.doLookupLGTvCodeTokens(str);
                        } else {
                            ApiClient.this.createPopup("Network Error", "Error getting TV code. Please make sure you are connected to the internet and try again later");
                        }
                    }
                }));
                return;
            }
            createPopup("Invalid Code", "You entered an invalid TV code. Please enter the 6 digit code displayed on your TV above.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doLookupLGTvCodeTokens(final String str) {
        try {
            if (!TextUtils.isEmpty(str) && str.length() == 6) {
                EncryptClient encryptClient = new EncryptClient(this.context);
                encryptClient.getValue("serverId");
                if (TextUtils.isEmpty(encryptClient.getValue("gsToken"))) {
                    createPopup("Sign-in Required", "You must sign-in to your Xbox Live account first. Click the Sign-in button above, then try again.");
                    return;
                }
                Volley.newRequestQueue(this.context).add(new StringRequest(0, this.WEBOS_TVCODE_BASE_URL + RemoteSettings.FORWARD_SLASH_STRING + str, new Response.Listener<String>() { // from class: com.studio08.xbgamestream.Web.ApiClient.17
                    @Override // com.android.volley.Response.Listener
                    public void onResponse(String str2) {
                        Log.i("HERE", "Got LG tv code");
                        ApiClient.this.startLGTvStream(str);
                    }
                }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.18
                    @Override // com.android.volley.Response.ErrorListener
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError.networkResponse != null && volleyError.networkResponse.statusCode == 404) {
                            ApiClient.this.createPopup("Invalid TV Code", "TV code not found. Make sure the app is open on your TV and that you entered the correct code. If he issue persists, restart the TV app.");
                        } else {
                            ApiClient.this.createPopup("Network Error", "Error getting TV code. Please make sure you are connected to the internet and try again later");
                        }
                    }
                }));
                return;
            }
            createPopup("Invalid Code", "You entered an invalid TV code. Please enter the 6 digit code displayed on your TV above.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startLGTvStream(String str) {
        try {
            EncryptClient encryptClient = new EncryptClient(this.context);
            String value = encryptClient.getValue("serverId");
            String value2 = encryptClient.getValue("gsToken");
            String value3 = encryptClient.getValue("xcloudToken");
            String value4 = encryptClient.getValue("msalAccessToken");
            boolean purchaseItem = RewardedAdLoader.getPurchaseItem(this.context);
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("gsToken", value2);
            jSONObject.put("serverId", value);
            jSONObject.put("xCloudToken", value3);
            jSONObject.put("msalToken", value4);
            jSONObject.put("pcheck", purchaseItem);
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("code", str);
            jSONObject2.put("tokens", jSONObject.toString());
            doSaveLGTvCode(jSONObject2.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doSaveLGTvCode(final String str) {
        try {
            Volley.newRequestQueue(this.context).add(new StringRequest(1, this.WEBOS_TVCODE_BASE_URL, new Response.Listener<String>() { // from class: com.studio08.xbgamestream.Web.ApiClient.19
                @Override // com.android.volley.Response.Listener
                public void onResponse(String str2) {
                    Log.i("HERE", "Saved lg tv code");
                    ApiClient.this.createPopup("TV Found", "That's it! Your LG TV should begin streaming automatically. Please ensure your console and TV are on a 5GHz or wired connection.");
                }
            }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.20
                @Override // com.android.volley.Response.ErrorListener
                public void onErrorResponse(VolleyError volleyError) {
                    Toast.makeText(ApiClient.this.context, "Network error. TV app might not work..", 1).show();
                }
            }) { // from class: com.studio08.xbgamestream.Web.ApiClient.21
                @Override // com.android.volley.Request
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override // com.android.volley.Request
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return str.getBytes(StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startTvStream(String str) {
        try {
            EncryptClient encryptClient = new EncryptClient(this.context);
            String value = encryptClient.getValue("serverId");
            String value2 = encryptClient.getValue("gsToken");
            String value3 = encryptClient.getValue("xcloudToken");
            String value4 = encryptClient.getValue("msalAccessToken");
            String str2 = "http://" + str + "/startSession?gsToken=" + value2 + "&serverId=" + value;
            if (!TextUtils.isEmpty(value3)) {
                str2 = str2 + "&xcloudToken=" + value3;
            }
            if (!TextUtils.isEmpty(value4)) {
                str2 = str2 + "&msalToken=" + value4;
            }
            Volley.newRequestQueue(this.context).add(new StringRequest(0, str2, new Response.Listener<String>() { // from class: com.studio08.xbgamestream.Web.ApiClient.22
                @Override // com.android.volley.Response.Listener
                public void onResponse(String str3) {
                    if (str3.equals("ok")) {
                        ApiClient.this.createPopup("Success!", "Your TV should start streaming directly from your console now!\n\nIt is recommended that your TV use a wired connection or a 5GHz WiFi network at a minimum.\n\nPlease be aware that not all TVs are supported. You must have a high end Android TV (60FPS support and 2GB of RAM or more).\n\nThis is tested and working with a 'Chromecast with Google TV' device\n\nTip: If the video gets stuck on your TV for any reason, hold down the back button on your remote to refresh the video.");
                    } else {
                        ApiClient.this.createPopup("Error", "Error returned from TV. Please try again later: " + str3);
                    }
                }
            }, new Response.ErrorListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.23
                @Override // com.android.volley.Response.ErrorListener
                public void onErrorResponse(VolleyError volleyError) {
                    ApiClient.this.createPopup("TV Not Found", "Error connecting to TV. Ensure the TV app is open on your TV and that its on the same WiFi network as your phone and console.");
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean sendToggleTVMenuCommand() {
        if (this.streamWebview != null && this.loadedStreamView.booleanValue()) {
            callJavaScript(this.streamWebview, "toggleTVMenu", new Object[0]);
            return true;
        } else if (this.geckoStreamWebview != null) {
            this.geckoWebviewClient.sendToggleTVMenu(new JSONObject());
            return true;
        } else {
            return false;
        }
    }

    public void sendRefreshCommand() {
        if (this.streamWebview == null || !this.loadedStreamView.booleanValue()) {
            return;
        }
        refreshVideo();
    }

    public void sendGampeadButtonPressCommand(String str) {
        if (this.streamWebview == null || !this.loadedStreamView.booleanValue()) {
            return;
        }
        pressGamepadButton(str);
    }

    public void sendSkipGamepadConfigButton() {
        StreamWebview streamWebview = this.streamWebview;
        if (streamWebview != null) {
            callJavaScript(streamWebview, "sendSkipGamepadConfigButton", new Object[0]);
        } else {
            Log.e("HERE", "sendSkipGamepadConfigButton called before page load");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setStreamConfig() {
        callJavaScript(this.streamWebview, "setConfigData", getConfigSettings());
    }

    private void refreshVideo() {
        callJavaScript(this.streamWebview, "refreshVideo", new Object[0]);
    }

    private void pressGamepadButton(String str) {
        callJavaScript(this.streamWebview, "pressGamepadButton", str);
    }

    public String getConfigSettings() {
        String str;
        boolean z;
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("SettingsSharedPref", 0);
        EncryptClient encryptClient = new EncryptClient(this.context);
        JSONObject jSONObject = new JSONObject();
        String string = sharedPreferences.getString("video_fit_key", "cover");
        Integer valueOf = Integer.valueOf(sharedPreferences.getInt("video_vertical_offset_key", 50));
        String string2 = sharedPreferences.getString("emulate_client_key", "windows");
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
        String string6 = BuildConfig.FLAVOR.equals("tv") ? sharedPreferences.getString("emulate_client_key", "android") : string2;
        try {
            if (this.isXcloud.booleanValue()) {
                String value = encryptClient.getValue("msalAccessToken");
                String value2 = encryptClient.getValue("xcloudRegion");
                str = BuildConfig.FLAVOR;
                jSONObject.put("msalToken", value.replace("\n", ""));
                jSONObject.put("xcloudTitle", this.serverId);
                jSONObject.put("xcloudRegion", value2);
            } else {
                str = BuildConfig.FLAVOR;
            }
            jSONObject.put("video-fit", string);
            jSONObject.put("video-vertical-offset", valueOf);
            jSONObject.put("gsToken", this.gsToken);
            jSONObject.put("serverId", this.serverId);
            if (Helper.getActiveCustomPhysicalGamepadMappings(this.context) != null) {
                jSONObject.put("physical-controller-button-mappings", Helper.getActiveCustomPhysicalGamepadMappings(this.context));
            }
            jSONObject.put("gamepadRefreshRateMs", string3);
            jSONObject.put("userAgentType", string6);
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
            String string7 = sharedPreferences.getString("custom_local_key", "en-US");
            if (!string7.equals("en-US")) {
                jSONObject.put("customLocal", string7);
            }
            jSONObject.put("rumble_controller", z4);
            jSONObject.put("rumble_intensity", i3);
            boolean purchaseItem = RewardedAdLoader.getPurchaseItem(this.context);
            jSONObject.put(TapjoyConstants.TJC_PLATFORM, BuildConfig.APP_PLATFORM);
            jSONObject.put("render_engine", Helper.getRenderEngine(this.context));
            String str2 = str;
            if (str2.equals(str2)) {
                jSONObject.put("pcheck", purchaseItem);
            }
        } catch (Exception unused) {
        }
        return jSONObject.toString();
    }

    public static void callJavaScript(WebView webView, String str, Object... objArr) {
        StringBuilder sb = new StringBuilder("javascript:try{");
        sb.append(str);
        sb.append("(");
        int length = objArr.length;
        String str2 = "";
        int i = 0;
        while (i < length) {
            Object obj = objArr[i];
            sb.append(str2);
            boolean z = obj instanceof String;
            if (z) {
                sb.append("'");
            }
            sb.append(obj.toString().replace("'", "\\'"));
            if (z) {
                sb.append("'");
            }
            i++;
            str2 = f.a;
        }
        sb.append(")}catch(error){console.error(error.message);}");
        String sb2 = sb.toString();
        Log.e("HERE", "callJavaScript: call=" + sb2);
        webView.loadUrl(sb2);
    }

    public void cleanUp() {
        this.loadedStreamView = false;
        try {
            StreamWebview streamWebview = this.streamWebview;
            if (streamWebview != null) {
                streamWebview.clearHistory();
                this.streamWebview.clearCache(false);
                this.streamWebview.loadUrl("about:blank");
                this.streamWebview.onPause();
                this.streamWebview.removeAllViews();
                this.streamWebview.destroy();
                this.streamWebview.cleanup();
                this.streamWebview = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.streamWebview = null;
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

    public void createPopup(String str, String str2) {
        try {
            new AlertDialog.Builder(this.context).setTitle(str).setMessage(str2).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.Web.ApiClient.24
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            }).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
