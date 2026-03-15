package com.studio08.xbgamestream.ui.mirrorcast;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import com.studio08.xbgamestream.Helpers.Helper;
import com.studio08.xbgamestream.Helpers.PopupWebview;
import com.studio08.xbgamestream.MainActivity;
import com.studio08.xbgamestream.Servers.Server;
import com.studio08.xbgamestream.Web.ApiClient;
import com.studio08.xbgamestream.databinding.FragmentMirrorcastBinding;
import java.io.IOException;
/* loaded from: /app/base.apk/classes3.dex */
public class MirrorCastFragment extends Fragment {
    private FragmentMirrorcastBinding binding;
    ApiClient streamingClient;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        ((MainActivity) getActivity()).analyticsClient.logFragmentCreated("mirrorcast");
        FragmentMirrorcastBinding inflate = FragmentMirrorcastBinding.inflate(layoutInflater, viewGroup, false);
        this.binding = inflate;
        ConstraintLayout root = inflate.getRoot();
        TextView textView = this.binding.textMirrorcastFooter;
        if (!TextUtils.isEmpty(((MainActivity) requireActivity()).mirrorcastText)) {
            textView.setText(((MainActivity) requireActivity()).mirrorcastText);
        }
        Button button = this.binding.mirrorcastButton;
        button.setVisibility(0);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.mirrorcast.MirrorCastFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ((MainActivity) MirrorCastFragment.this.getActivity()).analyticsClient.logButtonClickEvent("open_mirrorcast");
                if (((MainActivity) MirrorCastFragment.this.requireActivity()).server != null) {
                    Toast.makeText(MirrorCastFragment.this.getActivity(), "Server already running! Restarting!", 0).show();
                    try {
                        ((MainActivity) MirrorCastFragment.this.requireActivity()).server.stop();
                        ((MainActivity) MirrorCastFragment.this.requireActivity()).server = null;
                    } catch (Exception unused) {
                    }
                }
                ((MainActivity) MirrorCastFragment.this.requireActivity()).server = new Server(Server.PORT, MirrorCastFragment.this.getActivity());
                try {
                    ((MainActivity) MirrorCastFragment.this.requireActivity()).server.start();
                    Toast.makeText(MirrorCastFragment.this.getActivity(), "MirrorCast Server Running!", 0).show();
                } catch (IOException e) {
                    Toast.makeText(MirrorCastFragment.this.getActivity(), "Error creating MirrorCast server!" + e.getMessage(), 0).show();
                    e.printStackTrace();
                }
                ((MainActivity) MirrorCastFragment.this.requireActivity()).mirrorcastText = "MirrorCast Running! Open a web-browser on any device connected to your local wifi and enter the following URL\n\n" + Helper.getLocalIpAddress() + ":" + Server.PORT;
                MirrorCastFragment.this.binding.textMirrorcastFooter.setText(((MainActivity) MirrorCastFragment.this.requireActivity()).mirrorcastText);
            }
        });
        this.binding.helpButton.setVisibility(0);
        this.binding.helpButton.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.mirrorcast.MirrorCastFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                new PopupWebview(MirrorCastFragment.this.getActivity()).showPopup(view, PopupWebview.MIRRORCAST_POPUP);
            }
        });
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
