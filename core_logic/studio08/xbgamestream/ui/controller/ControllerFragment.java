package com.studio08.xbgamestream.ui.controller;

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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.studio08.xbgamestream.Authenticate.LoginActivity;
import com.studio08.xbgamestream.Helpers.EncryptClient;
import com.studio08.xbgamestream.Helpers.Helper;
import com.studio08.xbgamestream.Helpers.PopupWebview;
import com.studio08.xbgamestream.Helpers.RewardedAdLoader;
import com.studio08.xbgamestream.MainActivity;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.Web.ApiClient;
import com.studio08.xbgamestream.Web.StreamWebview;
import com.studio08.xbgamestream.databinding.FragmentControllerBinding;
import org.mozilla.geckoview.GeckoView;
/* loaded from: /app/base.apk/classes3.dex */
public class ControllerFragment extends Fragment {
    private FragmentControllerBinding binding;
    private ControllerViewModel galleryViewModel;
    ApiClient streamingClient;
    boolean viewActive = false;
    boolean startStreamClicked = false;
    ApiClient.StreamingClientListener loginRequiredListener = new ApiClient.StreamingClientListener() { // from class: com.studio08.xbgamestream.ui.controller.ControllerFragment.1
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
            Toast.makeText(ControllerFragment.this.getActivity(), "Re Login Required!", 1).show();
            ControllerFragment.this.promptUserForLogin();
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void vibrate() {
            Helper.vibrate(ControllerFragment.this.getActivity());
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void genericMessage(String str, String str2) {
            if (str.equals("quitGame")) {
                Toast.makeText(ControllerFragment.this.getActivity(), "Quitting Stream", 1).show();
                if (((MainActivity) ControllerFragment.this.getActivity()) == null || ((MainActivity) ControllerFragment.this.getActivity()).navController.getCurrentDestination() == null) {
                    return;
                }
                ControllerFragment.this.getActivity().runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.ui.controller.ControllerFragment.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        ((MainActivity) ControllerFragment.this.getActivity()).navController.navigate(R.id.nav_controller);
                        if (((MainActivity) ControllerFragment.this.getActivity()).inFullScreenMode) {
                            ((MainActivity) ControllerFragment.this.getActivity()).showSystemUI();
                        }
                    }
                });
                return;
            }
            Log.e("HERE", "Unknown generic message received in controllerFrag: " + str);
        }
    };

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
        this.startStreamClicked = false;
        ((MainActivity) getActivity()).analyticsClient.logFragmentCreated("standalone_gamepad");
        this.galleryViewModel = (ControllerViewModel) new ViewModelProvider(this).get(ControllerViewModel.class);
        FragmentControllerBinding inflate = FragmentControllerBinding.inflate(layoutInflater, viewGroup, false);
        this.binding = inflate;
        ConstraintLayout root = inflate.getRoot();
        GeckoView geckoView = this.binding.geckowebview;
        StreamWebview streamWebview = this.binding.webview1;
        if (Helper.getRenderEngine(getActivity()).equals("geckoview")) {
            Log.w("HERE", "Using GeckoView");
            geckoView.setBackgroundColor(0);
            geckoView.coverUntilFirstPaint(0);
            streamWebview.setVisibility(8);
            geckoView.setVisibility(4);
        } else {
            Log.w("HERE", "Not using GeckoView");
            geckoView.setVisibility(8);
            streamWebview.setBackgroundColor(0);
            streamWebview.init();
        }
        Button button = this.binding.controllerButton;
        button.setVisibility(0);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.controller.ControllerFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ControllerFragment.this.startStreamButtonPress();
            }
        });
        this.binding.helpButton.setVisibility(0);
        this.binding.helpButton.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.controller.ControllerFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                new PopupWebview(ControllerFragment.this.getActivity()).showPopup(view, PopupWebview.STANDALONE_GAMEPAD_POPUP);
            }
        });
        return root;
    }

    @Override // androidx.fragment.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("startStreamClicked", this.startStreamClicked);
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewStateRestored(Bundle bundle) {
        Log.e("controllerFrag", "onViewStateRestored" + bundle);
        super.onViewStateRestored(bundle);
        if (bundle == null || !bundle.getBoolean("startStreamClicked")) {
            return;
        }
        Log.e("controllerFrag", "Recreated from orientation change" + bundle.toString());
        startStreamButtonPress();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startStreamButtonPress() {
        this.startStreamClicked = true;
        EncryptClient encryptClient = new EncryptClient(getContext());
        final String value = encryptClient.getValue("serverId");
        final String value2 = encryptClient.getValue("gsToken");
        final GeckoView geckoView = this.binding.geckowebview;
        final StreamWebview streamWebview = this.binding.webview1;
        final Button button = this.binding.controllerButton;
        if (!TextUtils.isEmpty(value2) && !TextUtils.isEmpty(value)) {
            ((MainActivity) getActivity()).analyticsClient.logButtonClickEvent("open_standalone_gamepad");
            ((MainActivity) getActivity()).rewardedAd.setCallbackListener(new RewardedAdLoader.RewardAdListener() { // from class: com.studio08.xbgamestream.ui.controller.ControllerFragment.4
                @Override // com.studio08.xbgamestream.Helpers.RewardedAdLoader.RewardAdListener
                public void onRewardComplete() {
                    if (ControllerFragment.this.binding != null) {
                        Log.e("HERE", "onRewardCompleteCaught!");
                        ((MainActivity) ControllerFragment.this.getActivity()).setOrientationLandscape();
                        ControllerFragment.this.binding.helpButton.setVisibility(4);
                        button.setVisibility(4);
                        if (Helper.getRenderEngine(ControllerFragment.this.getActivity()).equals("geckoview")) {
                            ControllerFragment.this.streamingClient = new ApiClient((Context) ControllerFragment.this.getActivity(), geckoView, value2, value, false);
                            geckoView.setVisibility(0);
                            geckoView.requestFocus();
                        } else {
                            ControllerFragment.this.streamingClient = new ApiClient((Context) ControllerFragment.this.getActivity(), streamWebview, value2, value, false);
                            streamWebview.requestFocus();
                        }
                        ControllerFragment.this.streamingClient.setControllerHandler(((MainActivity) ControllerFragment.this.getActivity()).controllerHandler);
                        ControllerFragment.this.streamingClient.setCustomObjectListener(ControllerFragment.this.loginRequiredListener);
                        ControllerFragment.this.streamingClient.doController();
                        if (((MainActivity) ControllerFragment.this.getActivity()) == null || ((MainActivity) ControllerFragment.this.getActivity()).inFullScreenMode) {
                            return;
                        }
                        ((MainActivity) ControllerFragment.this.getActivity()).hideSystemUI();
                        return;
                    }
                    Log.e("HERE", "Binding null");
                }
            });
            ((MainActivity) getActivity()).showConnectAdPossibly();
            return;
        }
        new AlertDialog.Builder(getActivity()).setTitle("Login Required").setMessage("You must login to connect to your console.").setCancelable(false).setPositiveButton("Login", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.ui.controller.ControllerFragment.5
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                ControllerFragment.this.promptUserForLogin();
            }
        }).setNegativeButton("Cancel", (DialogInterface.OnClickListener) null).show();
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
            ((MainActivity) getActivity()).navController.navigate(R.id.nav_controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onStop() {
        super.onStop();
        try {
            this.viewActive = false;
            this.binding.getRoot().removeAllViews();
            this.streamingClient.cleanUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.binding = null;
    }
}
