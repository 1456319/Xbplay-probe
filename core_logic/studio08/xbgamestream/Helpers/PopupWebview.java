package com.studio08.xbgamestream.Helpers;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;
import androidx.core.view.ViewCompat;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.Web.ApiClient;
import com.studio08.xbgamestream.Web.StreamWebview;
/* loaded from: /app/base.apk/classes3.dex */
public class PopupWebview {
    public static String ANDROID_TV_POPUP = "swipe-screens/info-popup/features_androidTv.html";
    public static String BUILDER_POPUP = "swipe-screens/info-popup/features_builder.html";
    public static String CONSOLES_POPUP = "swipe-screens/info-popup/features_console.html";
    public static String FILECAST_POPUP = "swipe-screens/info-popup/features_filecast.html";
    public static String GAMESTREAM_POPUP = "swipe-screens/info-popup/features_stream.html";
    public static String KEYBOARD_WARNING_POPUP = "swipe-screens/info-popup/features_keyboardWarning.html";
    public static String MEDIA_REMOTE_POPUP = "swipe-screens/info-popup/features_mediaRemote.html";
    public static String MIRRORCAST_POPUP = "swipe-screens/info-popup/features_mirrorcast.html";
    public static String STANDALONE_GAMEPAD_POPUP = "swipe-screens/info-popup/features_gamepadController.html";
    public static String TV_CAST_POPUP = "swipe-screens/info-popup/features_tvCast.html";
    public static String VOICE_REMOTE_POPUP = "swipe-screens/info-popup/features_voiceRemote.html";
    public static String XCLOUD_POPUP = "swipe-screens/info-popup/features_xcloud.html";
    Activity activity;
    Context context;

    public PopupWebview(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    public void showPopup(View view, String str) {
        Toast.makeText(this.activity, "Swipe to view details about this feature", 1).show();
        View inflate = ((LayoutInflater) this.context.getSystemService("layout_inflater")).inflate(R.layout.popup_view, (ViewGroup) null);
        PopupWindow popupWindow = new PopupWindow(inflate, (int) (this.context.getResources().getDisplayMetrics().widthPixels * 0.9d), (int) (this.context.getResources().getDisplayMetrics().heightPixels * 0.75d), true);
        popupWindow.showAtLocation(view, 17, 0, 0);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() { // from class: com.studio08.xbgamestream.Helpers.PopupWebview.1
            @Override // android.widget.PopupWindow.OnDismissListener
            public void onDismiss() {
                PopupWebview.clearDim((ViewGroup) PopupWebview.this.activity.getWindow().getDecorView().getRootView());
            }
        });
        StreamWebview streamWebview = (StreamWebview) inflate.findViewById(R.id.webview1);
        streamWebview.setBackgroundColor(0);
        streamWebview.init();
        streamWebview.loadUrl(buildUrl(str));
        applyDim((ViewGroup) this.activity.getWindow().getDecorView().getRootView(), 0.8f);
    }

    public static void applyDim(ViewGroup viewGroup, float f) {
        ColorDrawable colorDrawable = new ColorDrawable(ViewCompat.MEASURED_STATE_MASK);
        colorDrawable.setBounds(0, 0, viewGroup.getWidth(), viewGroup.getHeight());
        colorDrawable.setAlpha((int) (f * 255.0f));
        viewGroup.getOverlay().add(colorDrawable);
    }

    public static void clearDim(ViewGroup viewGroup) {
        viewGroup.getOverlay().clear();
    }

    private String buildUrl(String str) {
        StringBuilder sb;
        String str2;
        if (ApiClient.USE_DEV) {
            sb = new StringBuilder();
            str2 = ApiClient.BASE_URL_DEV;
        } else {
            sb = new StringBuilder();
            str2 = ApiClient.BASE_URL_PROD;
        }
        return sb.append(str2).append(str).toString();
    }
}
