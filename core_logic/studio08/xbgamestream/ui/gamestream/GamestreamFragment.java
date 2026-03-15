package com.studio08.xbgamestream.ui.gamestream;

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
import com.studio08.xbgamestream.databinding.FragmentGamestreamBinding;
import org.mozilla.geckoview.GeckoView;
/* loaded from: /app/base.apk/classes3.dex */
public class GamestreamFragment extends Fragment {
    private FragmentGamestreamBinding binding;
    private GamestreamViewModel galleryViewModel;
    ApiClient streamingClient;
    boolean viewActive = false;
    boolean startStreamClicked = false;
    ApiClient.StreamingClientListener loginRequiredListener = new ApiClient.StreamingClientListener() { // from class: com.studio08.xbgamestream.ui.gamestream.GamestreamFragment.1
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
            Toast.makeText(GamestreamFragment.this.getActivity(), "Re Login Required!", 1).show();
            GamestreamFragment.this.promptUserForLogin();
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void vibrate() {
            Helper.vibrate(GamestreamFragment.this.getActivity());
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void genericMessage(String str, String str2) {
            if (str.equals("quitGame")) {
                Toast.makeText(GamestreamFragment.this.getActivity(), "Quitting Stream", 1).show();
                if (((MainActivity) GamestreamFragment.this.getActivity()) == null || ((MainActivity) GamestreamFragment.this.getActivity()).navController.getCurrentDestination() == null) {
                    return;
                }
                GamestreamFragment.this.getActivity().runOnUiThread(new Runnable() { // from class: com.studio08.xbgamestream.ui.gamestream.GamestreamFragment.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        ((MainActivity) GamestreamFragment.this.getActivity()).navController.navigate(R.id.nav_gamestream);
                        if (((MainActivity) GamestreamFragment.this.getActivity()).inFullScreenMode) {
                            ((MainActivity) GamestreamFragment.this.getActivity()).showSystemUI();
                        }
                    }
                });
                return;
            }
            Log.e("HERE", "Unknown generic message received in xhomeFrag: " + str);
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
        ((MainActivity) getActivity()).analyticsClient.logFragmentCreated("gamestream");
        this.galleryViewModel = (GamestreamViewModel) new ViewModelProvider(this).get(GamestreamViewModel.class);
        FragmentGamestreamBinding inflate = FragmentGamestreamBinding.inflate(layoutInflater, viewGroup, false);
        this.binding = inflate;
        ConstraintLayout root = inflate.getRoot();
        GeckoView geckoView = this.binding.geckowebview;
        StreamWebview streamWebview = this.binding.webview1;
        Button button = this.binding.gamestreamButton;
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
        button.setVisibility(0);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.gamestream.GamestreamFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                GamestreamFragment.this.startStreamButtonPress();
            }
        });
        this.binding.helpButton.setVisibility(0);
        this.binding.helpButton.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.gamestream.GamestreamFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                new PopupWebview(GamestreamFragment.this.getActivity()).showPopup(view, PopupWebview.GAMESTREAM_POPUP);
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
        Log.e("xHomeFrag", "onViewStateRestored" + bundle);
        super.onViewStateRestored(bundle);
        if (bundle == null || !bundle.getBoolean("startStreamClicked")) {
            return;
        }
        Log.e("xHomeFrag", "Recreated from orientation change" + bundle.toString());
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
        final Button button = this.binding.gamestreamButton;
        if (!TextUtils.isEmpty(value2) && !TextUtils.isEmpty(value)) {
            ((MainActivity) getActivity()).analyticsClient.logButtonClickEvent("open_gamestream");
            ((MainActivity) getActivity()).rewardedAd.setCallbackListener(new RewardedAdLoader.RewardAdListener() { // from class: com.studio08.xbgamestream.ui.gamestream.GamestreamFragment.4
                @Override // com.studio08.xbgamestream.Helpers.RewardedAdLoader.RewardAdListener
                public void onRewardComplete() {
                    if (GamestreamFragment.this.binding != null) {
                        Log.e("HERE", "onRewardCompleteCaught!");
                        ((MainActivity) GamestreamFragment.this.getActivity()).setOrientationLandscape();
                        GamestreamFragment.this.binding.helpButton.setVisibility(4);
                        button.setVisibility(4);
                        if (Helper.getRenderEngine(GamestreamFragment.this.getActivity()).equals("geckoview")) {
                            GamestreamFragment.this.streamingClient = new ApiClient((Context) GamestreamFragment.this.getActivity(), geckoView, value2, value, false);
                            geckoView.setVisibility(0);
                            geckoView.requestFocus();
                        } else {
                            GamestreamFragment.this.streamingClient = new ApiClient((Context) GamestreamFragment.this.getActivity(), streamWebview, value2, value, false);
                            streamWebview.requestFocus();
                        }
                        GamestreamFragment.this.streamingClient.setCustomObjectListener(GamestreamFragment.this.loginRequiredListener);
                        GamestreamFragment.this.streamingClient.setControllerHandler(((MainActivity) GamestreamFragment.this.getActivity()).controllerHandler);
                        GamestreamFragment.this.streamingClient.doStreaming();
                        if (((MainActivity) GamestreamFragment.this.getActivity()) == null || ((MainActivity) GamestreamFragment.this.getActivity()).inFullScreenMode) {
                            return;
                        }
                        ((MainActivity) GamestreamFragment.this.getActivity()).hideSystemUI();
                        return;
                    }
                    Log.e("HERE", "Binding null");
                }
            });
            ((MainActivity) getActivity()).showConnectAdPossibly();
            return;
        }
        new AlertDialog.Builder(getActivity()).setTitle("Login Required").setMessage("You must login to connect to your console.").setCancelable(false).setPositiveButton("Login", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.ui.gamestream.GamestreamFragment.5
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                GamestreamFragment.this.promptUserForLogin();
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
            ((MainActivity) getActivity()).navController.navigate(R.id.nav_gamestream);
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
