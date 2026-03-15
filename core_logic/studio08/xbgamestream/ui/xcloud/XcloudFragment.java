package com.studio08.xbgamestream.ui.xcloud;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.fragment.app.Fragment;
import com.studio08.xbgamestream.Authenticate.LoginActivity;
import com.studio08.xbgamestream.Helpers.EncryptClient;
import com.studio08.xbgamestream.Helpers.Helper;
import com.studio08.xbgamestream.Helpers.PopupWebview;
import com.studio08.xbgamestream.Helpers.RewardedAdLoader;
import com.studio08.xbgamestream.MainActivity;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.Web.ApiClient;
import com.studio08.xbgamestream.Web.StreamWebview;
import com.studio08.xbgamestream.databinding.FragmentXcloudBinding;
import org.json.JSONObject;
import org.mozilla.geckoview.GeckoView;
/* loaded from: /app/base.apk/classes3.dex */
public class XcloudFragment extends Fragment {
    private FragmentXcloudBinding binding;
    GeckoView geckoStreamView;
    StreamWebview streamView;
    ApiClient streamingClient;
    boolean viewActive = false;
    String startStreamClickedGameTitleId = null;
    ApiClient.StreamingClientListener loginRequiredListener = new ApiClient.StreamingClientListener() { // from class: com.studio08.xbgamestream.ui.xcloud.XcloudFragment.1
        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void onCloseScreenDetected() {
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void pressButtonWifiRemote(String str) {
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void setOrientationValue(String str) {
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void onReLoginDetected() {
            Toast.makeText(XcloudFragment.this.getActivity(), "Re Login Required!", 1).show();
            XcloudFragment.this.promptUserForLogin();
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void vibrate() {
            Helper.vibrate(XcloudFragment.this.getActivity());
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void genericMessage(final String str, final String str2) {
            XcloudFragment.this.getActivity().runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.ui.xcloud.XcloudFragment.1.1
                @Override // java.lang.Runnable
                public void run() {
                    XcloudFragment.this.handleGenericMessage(str, str2);
                }
            });
        }
    };

    public void handleGenericMessage(String str, String str2) {
        if (str.equals("start_xcloud_stream_v2")) {
            try {
                JSONObject jSONObject = new JSONObject(str2);
                final String string = jSONObject.getString("titleId");
                final String string2 = jSONObject.getString("image");
                final String string3 = jSONObject.getString("title");
                Log.e("HERE", jSONObject.toString());
                AlertDialog.Builder negativeButton = new AlertDialog.Builder(getActivity()).setTitle("Start Game?").setMessage("Start Playing: " + string3).setCancelable(true).setPositiveButton("Play", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.ui.xcloud.XcloudFragment.3
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        XcloudFragment.this.handleGenericMessage("start_xcloud_stream", string);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.ui.xcloud.XcloudFragment.2
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                if (ShortcutManagerCompat.isRequestPinShortcutSupported(getContext())) {
                    negativeButton.setMessage("Start Playing: " + string3 + "?\n\nPlay this game often? Add a home-screen shortcut to start directly into this game!");
                    negativeButton.setNeutralButton("Add Shortcut", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.ui.xcloud.XcloudFragment.4
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Helper.addShortcutToHomeScreen(XcloudFragment.this.getActivity(), string, string3, string2, "xcloud");
                        }
                    });
                }
                negativeButton.show();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("HERE", "Failed to decode json message");
            }
        } else if (str.equals("start_xcloud_stream")) {
            Toast.makeText(getActivity(), "Starting " + str2 + "!", 1).show();
            this.startStreamClickedGameTitleId = str2;
            if (!TextUtils.isEmpty(new EncryptClient(getContext()).getValue("xcloudToken"))) {
                ((MainActivity) getActivity()).setOrientationLandscape();
                startXCloudStream(str2);
            } else {
                promptUserForLogin();
            }
            Helper.hideKeyboard(getActivity());
        } else if (str.equals("quitGame")) {
            Toast.makeText(getActivity(), "Quitting Stream", 1).show();
            if (((MainActivity) getActivity()) == null || ((MainActivity) getActivity()).navController.getCurrentDestination() == null) {
                return;
            }
            getActivity().runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.ui.xcloud.XcloudFragment.5
                @Override // java.lang.Runnable
                public void run() {
                    ((MainActivity) XcloudFragment.this.getActivity()).navController.navigate(R.id.nav_xcloud);
                    if (((MainActivity) XcloudFragment.this.getActivity()).inFullScreenMode) {
                        ((MainActivity) XcloudFragment.this.getActivity()).showSystemUI();
                    }
                }
            });
        } else {
            Log.e("HERE", "Unknown generic message received in xcloudFrag: " + str);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startXCloudStream(String str) {
        String value = new EncryptClient(getContext()).getValue("xcloudToken");
        if (Helper.getRenderEngine(getActivity()).equals("geckoview")) {
            this.streamingClient = new ApiClient((Context) getActivity(), this.geckoStreamView, value, str, true);
            this.binding.geckowebview.setVisibility(0);
            this.geckoStreamView.requestFocus();
        } else {
            this.streamingClient = new ApiClient((Context) getActivity(), this.streamView, value, str, true);
            this.streamView.requestFocus();
        }
        this.streamingClient.setCustomObjectListener(this.loginRequiredListener);
        this.streamingClient.setControllerHandler(((MainActivity) getActivity()).controllerHandler);
        this.streamingClient.doStreaming();
        this.streamView.requestFocus();
    }

    public void promptUserForLogin() {
        try {
            getActivity().startActivityForResult(new Intent(getContext(), LoginActivity.class), 444);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.viewActive = true;
        this.startStreamClickedGameTitleId = null;
        ((MainActivity) getActivity()).analyticsClient.logFragmentCreated("xcloud");
        getActivity().getWindow().setSoftInputMode(48);
        FragmentXcloudBinding inflate = FragmentXcloudBinding.inflate(layoutInflater, viewGroup, false);
        this.binding = inflate;
        ConstraintLayout root = inflate.getRoot();
        this.streamView = this.binding.webview1;
        this.geckoStreamView = this.binding.geckowebview;
        if (Helper.getRenderEngine(getActivity()).equals("geckoview")) {
            Log.w("HERE", "Using GeckoView");
            this.geckoStreamView.setBackgroundColor(0);
            this.geckoStreamView.coverUntilFirstPaint(0);
            this.streamView.setVisibility(8);
            this.geckoStreamView.setVisibility(4);
        } else {
            Log.w("HERE", "Not using GeckoView");
            this.geckoStreamView.setVisibility(8);
            this.streamView.setBackgroundColor(0);
            this.streamView.init();
        }
        Button button = this.binding.xcloudButton;
        button.setVisibility(0);
        final String value = new EncryptClient(getContext()).getValue("xcloudToken");
        button.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.xcloud.XcloudFragment.6
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ((MainActivity) XcloudFragment.this.getActivity()).analyticsClient.logButtonClickEvent("open_xcloud");
                if (!TextUtils.isEmpty(value)) {
                    ((MainActivity) XcloudFragment.this.getActivity()).rewardedAd.setCallbackListener(new RewardedAdLoader.RewardAdListener() { // from class: com.studio08.xbgamestream.ui.xcloud.XcloudFragment.6.1
                        @Override // com.studio08.xbgamestream.Helpers.RewardedAdLoader.RewardAdListener
                        public void onRewardComplete() {
                            if (XcloudFragment.this.binding != null) {
                                Log.e("HERE", "onRewardCompleteCaught!");
                                XcloudFragment.this.hideNonWebviewUI();
                                if (((MainActivity) XcloudFragment.this.getActivity()) != null) {
                                    if (!((MainActivity) XcloudFragment.this.getActivity()).inFullScreenMode) {
                                        ((MainActivity) XcloudFragment.this.getActivity()).hideSystemUI();
                                    }
                                    if (((MainActivity) XcloudFragment.this.getActivity()).xCloudShortcutTitleId != null) {
                                        XcloudFragment.this.startXCloudStream(((MainActivity) XcloudFragment.this.getActivity()).xCloudShortcutTitleId);
                                        return;
                                    }
                                }
                                if (Helper.getRenderEngine(XcloudFragment.this.getActivity()).equals("geckoview")) {
                                    XcloudFragment.this.streamingClient = new ApiClient((Context) XcloudFragment.this.getActivity(), XcloudFragment.this.geckoStreamView, value, (String) null, true);
                                    XcloudFragment.this.geckoStreamView.setVisibility(0);
                                    XcloudFragment.this.geckoStreamView.requestFocus();
                                } else {
                                    XcloudFragment.this.streamingClient = new ApiClient((Context) XcloudFragment.this.getActivity(), XcloudFragment.this.streamView, value, (String) null, false);
                                    XcloudFragment.this.streamView.requestFocus();
                                }
                                XcloudFragment.this.streamingClient.setCustomObjectListener(XcloudFragment.this.loginRequiredListener);
                                XcloudFragment.this.streamingClient.setControllerHandler(((MainActivity) XcloudFragment.this.getActivity()).controllerHandler);
                                XcloudFragment.this.streamingClient.doXcloudGamePicker();
                                return;
                            }
                            Log.e("HERE", "Binding null");
                        }
                    });
                    ((MainActivity) XcloudFragment.this.getActivity()).showConnectAdPossibly();
                    return;
                }
                new AlertDialog.Builder(XcloudFragment.this.getActivity()).setTitle("Login Required").setMessage("You must login to use this feature.\n\nNote, Microsoft requires you to have a 'Game Pass' subscription to play xCloud games. If you don't have Game Pass, use the xHome feature.").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.ui.xcloud.XcloudFragment.6.2
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        XcloudFragment.this.promptUserForLogin();
                    }
                }).show();
            }
        });
        this.binding.textXcloudFooter.setVisibility(0);
        this.binding.helpButton.setVisibility(0);
        this.binding.helpButton.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.xcloud.XcloudFragment.7
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                new PopupWebview(XcloudFragment.this.getActivity()).showPopup(view, PopupWebview.XCLOUD_POPUP);
            }
        });
        return root;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hideNonWebviewUI() {
        this.binding.helpButton.setVisibility(4);
        this.binding.textXcloudFooter.setVisibility(4);
        this.binding.xcloudButton.setVisibility(4);
    }

    @Override // androidx.fragment.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        String str = this.startStreamClickedGameTitleId;
        if (str != null) {
            bundle.putString("startStreamClickedGameTitleId", str);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewStateRestored(Bundle bundle) {
        Log.e("xHomeFrag", "onViewStateRestored" + bundle);
        super.onViewStateRestored(bundle);
        if (bundle == null || bundle.getString("startStreamClickedGameTitleId") == null) {
            return;
        }
        Log.e("xHomeFrag", "Recreated from orientation change" + bundle.toString());
        hideNonWebviewUI();
        startXCloudStream(bundle.getString("startStreamClickedGameTitleId"));
        if (((MainActivity) getActivity()) == null || ((MainActivity) getActivity()).inFullScreenMode) {
            return;
        }
        ((MainActivity) getActivity()).hideSystemUI();
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        try {
            this.viewActive = false;
            this.binding.getRoot().removeAllViews();
            this.streamingClient.cleanUp();
        } catch (Exception unused) {
        }
        this.binding = null;
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        if (this.viewActive) {
            return;
        }
        try {
            ((MainActivity) getActivity()).navController.navigate(R.id.nav_xcloud);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onStop() {
        super.onStop();
        try {
            this.viewActive = false;
            getActivity().getWindow().setSoftInputMode(16);
            this.binding.getRoot().removeAllViews();
            this.streamingClient.cleanUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.binding = null;
    }
}
