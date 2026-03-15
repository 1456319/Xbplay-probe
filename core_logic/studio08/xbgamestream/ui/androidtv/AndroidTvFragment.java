package com.studio08.xbgamestream.ui.androidtv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import com.studio08.xbgamestream.Authenticate.LoginActivity;
import com.studio08.xbgamestream.Helpers.Helper;
import com.studio08.xbgamestream.Helpers.PopupWebview;
import com.studio08.xbgamestream.MainActivity;
import com.studio08.xbgamestream.Web.ApiClient;
import com.studio08.xbgamestream.databinding.FragmentAndroidTvBinding;
/* loaded from: /app/base.apk/classes3.dex */
public class AndroidTvFragment extends Fragment {
    ApiClient apiClient;
    private FragmentAndroidTvBinding binding;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        ((MainActivity) getActivity()).analyticsClient.logFragmentCreated("androidtv");
        FragmentAndroidTvBinding inflate = FragmentAndroidTvBinding.inflate(layoutInflater, viewGroup, false);
        this.binding = inflate;
        ConstraintLayout root = inflate.getRoot();
        this.apiClient = new ApiClient(getContext());
        this.binding.sendToTvButton.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.androidtv.AndroidTvFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ((MainActivity) AndroidTvFragment.this.getActivity()).analyticsClient.logButtonClickEvent("open_send_android_tv");
                AndroidTvFragment.this.apiClient.doLookupTvCode(AndroidTvFragment.this.binding.tvCodeEditText.getText().toString());
            }
        });
        this.binding.loginToAccount.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.androidtv.AndroidTvFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ((MainActivity) AndroidTvFragment.this.getActivity()).analyticsClient.logButtonClickEvent("open_console_connect_tv");
                if (!Helper.checkWifiConnected(AndroidTvFragment.this.getActivity().getApplicationContext())) {
                    new AlertDialog.Builder(AndroidTvFragment.this.getActivity()).setTitle("Connect to Wifi").setMessage("You must be connected to the same Wifi network as your console. Connect now?").setCancelable(true).setPositiveButton("Connect Wifi", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.ui.androidtv.AndroidTvFragment.2.2
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            AndroidTvFragment.this.startActivity(new Intent("android.settings.WIFI_SETTINGS"));
                        }
                    }).setNegativeButton("Continue Anyway", new DialogInterface.OnClickListener() { // from class: com.studio08.xbgamestream.ui.androidtv.AndroidTvFragment.2.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            AndroidTvFragment.this.getActivity().startActivityForResult(new Intent(AndroidTvFragment.this.getContext(), LoginActivity.class), 444);
                        }
                    }).show();
                    return;
                }
                AndroidTvFragment.this.getActivity().startActivityForResult(new Intent(AndroidTvFragment.this.getContext(), LoginActivity.class), 444);
            }
        });
        this.binding.helpButton.setVisibility(0);
        this.binding.helpButton.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.androidtv.AndroidTvFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                new PopupWebview(AndroidTvFragment.this.getActivity()).showPopup(view, PopupWebview.ANDROID_TV_POPUP);
            }
        });
        if (((MainActivity) getActivity()) != null && !((MainActivity) getActivity()).tvCodeUri.equals("")) {
            this.binding.tvCodeEditText.setText(((MainActivity) getActivity()).tvCodeUri);
            ((MainActivity) getActivity()).tvCodeUri = "";
        }
        return root;
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        try {
            this.apiClient.cleanUp();
        } catch (Exception unused) {
        }
        this.binding = null;
    }
}
