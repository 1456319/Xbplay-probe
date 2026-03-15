package com.studio08.xbgamestream.ui.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import androidx.fragment.app.Fragment;
import com.studio08.xbgamestream.MainActivity;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.Web.ApiClient;
import com.studio08.xbgamestream.Web.StreamWebview;
import com.studio08.xbgamestream.Widgets.PowerWidgetProvider;
import com.studio08.xbgamestream.Widgets.RemoteWidgetProvider;
import com.studio08.xbgamestream.databinding.FragmentWidgetsBinding;
/* loaded from: /app/base.apk/classes3.dex */
public class WidgetsFragment extends Fragment {
    private FragmentWidgetsBinding binding;
    ApiClient.StreamingClientListener buttonPressListener = new ApiClient.StreamingClientListener() { // from class: com.studio08.xbgamestream.ui.widgets.WidgetsFragment.1
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
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void genericMessage(String str, String str2) {
            Log.e("HERE", "caught generic message");
            try {
                if (str.equals("addPowerWidget")) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        AppWidgetManager appWidgetManager = (AppWidgetManager) WidgetsFragment.this.getActivity().getSystemService(AppWidgetManager.class);
                        if (appWidgetManager.isRequestPinAppWidgetSupported()) {
                            appWidgetManager.requestPinAppWidget(new ComponentName(WidgetsFragment.this.getActivity(), PowerWidgetProvider.class), new Bundle(), PendingIntent.getBroadcast(WidgetsFragment.this.getActivity(), 0, new Intent(WidgetsFragment.this.getActivity(), PowerWidgetProvider.class), AccessibilityEventCompat.TYPE_VIEW_TARGETED_BY_SCROLL));
                        }
                    }
                    Toast.makeText(WidgetsFragment.this.getContext(), "Long press on your home-screen and select widgets->" + WidgetsFragment.this.getResources().getString(R.string.app_name) + " to add the widget", 0).show();
                }
                if (str.equals("addRemoteWidget") && Build.VERSION.SDK_INT >= 26) {
                    AppWidgetManager appWidgetManager2 = (AppWidgetManager) WidgetsFragment.this.getActivity().getSystemService(AppWidgetManager.class);
                    if (appWidgetManager2.isRequestPinAppWidgetSupported()) {
                        appWidgetManager2.requestPinAppWidget(new ComponentName(WidgetsFragment.this.getActivity(), RemoteWidgetProvider.class), new Bundle(), PendingIntent.getBroadcast(WidgetsFragment.this.getActivity(), 0, new Intent(WidgetsFragment.this.getActivity(), RemoteWidgetProvider.class), AccessibilityEventCompat.TYPE_VIEW_TARGETED_BY_SCROLL));
                    }
                }
                Toast.makeText(WidgetsFragment.this.getContext(), "Long press on your home-screen and select widgets->" + WidgetsFragment.this.getResources().getString(R.string.app_name) + " to add the widget", 0).show();
            } catch (Error e) {
                e.printStackTrace();
            }
        }
    };
    ApiClient streamingClient;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        ((MainActivity) getActivity()).setOrientationPortrait();
        ((MainActivity) getActivity()).analyticsClient.logFragmentCreated("widget");
        FragmentWidgetsBinding inflate = FragmentWidgetsBinding.inflate(layoutInflater, viewGroup, false);
        this.binding = inflate;
        ConstraintLayout root = inflate.getRoot();
        StreamWebview streamWebview = this.binding.webview1;
        streamWebview.setBackgroundColor(0);
        streamWebview.init();
        ApiClient apiClient = new ApiClient(getActivity(), this.binding.webview1);
        this.streamingClient = apiClient;
        apiClient.setCustomObjectListener(this.buttonPressListener);
        this.streamingClient.doWidgetTutorial();
        streamWebview.requestFocus();
        return root;
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        try {
            this.streamingClient.cleanUp();
        } catch (Exception unused) {
        }
        this.binding = null;
    }
}
