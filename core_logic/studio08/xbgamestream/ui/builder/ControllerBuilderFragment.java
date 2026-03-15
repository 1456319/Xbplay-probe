package com.studio08.xbgamestream.ui.builder;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.gms.cast.MediaError;
import com.studio08.xbgamestream.BuildConfig;
import com.studio08.xbgamestream.Helpers.Helper;
import com.studio08.xbgamestream.Helpers.PopupWebview;
import com.studio08.xbgamestream.MainActivity;
import com.studio08.xbgamestream.Web.ApiClient;
import com.studio08.xbgamestream.Web.StreamWebview;
import com.studio08.xbgamestream.databinding.FragmentControllerBuilderBinding;
import com.tapjoy.TJAdUnitConstants;
import org.mozilla.geckoview.GeckoView;
/* loaded from: /app/base.apk/classes3.dex */
public class ControllerBuilderFragment extends Fragment {
    private FragmentControllerBuilderBinding binding;
    private ControllerBuilderViewModel galleryViewModel;
    ApiClient.StreamingClientListener orientationChangeListener = new ApiClient.StreamingClientListener() { // from class: com.studio08.xbgamestream.ui.builder.ControllerBuilderFragment.1
        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void genericMessage(String str, String str2) {
        }

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
        public void vibrate() {
        }

        @Override // com.studio08.xbgamestream.Web.ApiClient.StreamingClientListener
        public void setOrientationValue(String str) {
            if (str.equals(TJAdUnitConstants.String.LANDSCAPE)) {
                ((MainActivity) ControllerBuilderFragment.this.getActivity()).setOrientationLandscape();
            } else if (str.equals(TJAdUnitConstants.String.PORTRAIT)) {
                ((MainActivity) ControllerBuilderFragment.this.getActivity()).setOrientationPortrait();
            } else {
                Log.e(MediaError.ERROR_TYPE_ERROR, "Invalid orientation specified from website");
            }
        }
    };
    ApiClient streamingClient;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        ((MainActivity) getActivity()).analyticsClient.logFragmentCreated("gamepad_builder");
        this.galleryViewModel = (ControllerBuilderViewModel) new ViewModelProvider(this).get(ControllerBuilderViewModel.class);
        FragmentControllerBuilderBinding inflate = FragmentControllerBuilderBinding.inflate(layoutInflater, viewGroup, false);
        this.binding = inflate;
        ConstraintLayout root = inflate.getRoot();
        final StreamWebview streamWebview = this.binding.webview1;
        final GeckoView geckoView = this.binding.geckowebview;
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
        final Button button = this.binding.builderButton;
        button.setVisibility(0);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.builder.ControllerBuilderFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ((MainActivity) ControllerBuilderFragment.this.getActivity()).analyticsClient.logButtonClickEvent("build_gamepad");
                ControllerBuilderFragment.this.binding.helpButton.setVisibility(4);
                button.setVisibility(4);
                if (Helper.getRenderEngine(ControllerBuilderFragment.this.getActivity()).equals("geckoview")) {
                    ControllerBuilderFragment.this.streamingClient = new ApiClient(ControllerBuilderFragment.this.getActivity(), geckoView);
                    ControllerBuilderFragment.this.streamingClient.setCustomObjectListener(ControllerBuilderFragment.this.orientationChangeListener);
                    geckoView.setVisibility(0);
                    geckoView.requestFocus();
                } else {
                    ControllerBuilderFragment.this.streamingClient = new ApiClient(ControllerBuilderFragment.this.getActivity(), streamWebview);
                    ControllerBuilderFragment.this.streamingClient.setCustomObjectListener(ControllerBuilderFragment.this.orientationChangeListener);
                    streamWebview.requestFocus();
                }
                if (BuildConfig.FLAVOR.equals("mediaRemote")) {
                    ((MainActivity) ControllerBuilderFragment.this.getActivity()).setOrientationPortrait();
                    ControllerBuilderFragment.this.streamingClient.doControllerBuilder("navigationRemoteControllerOption");
                } else if (BuildConfig.FLAVOR.equals("gamepadController")) {
                    ((MainActivity) ControllerBuilderFragment.this.getActivity()).setOrientationLandscape();
                    ControllerBuilderFragment.this.streamingClient.doControllerBuilder("standaloneControllerOption");
                } else if (BuildConfig.FLAVOR.equals("stream")) {
                    ((MainActivity) ControllerBuilderFragment.this.getActivity()).setOrientationLandscape();
                    ControllerBuilderFragment.this.streamingClient.doControllerBuilder("gamestreamControllerOption");
                } else {
                    ((MainActivity) ControllerBuilderFragment.this.getActivity()).setOrientationLandscape();
                    ControllerBuilderFragment.this.streamingClient.doControllerBuilder(null);
                }
            }
        });
        this.binding.helpButton.setVisibility(0);
        this.binding.helpButton.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.builder.ControllerBuilderFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                new PopupWebview(ControllerBuilderFragment.this.getActivity()).showPopup(view, PopupWebview.BUILDER_POPUP);
            }
        });
        return root;
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        try {
            this.binding.getRoot().removeAllViews();
            this.streamingClient.cleanUp();
        } catch (Exception unused) {
        }
        this.binding = null;
    }
}
