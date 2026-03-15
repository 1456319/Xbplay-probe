package com.studio08.xbgamestream.ui.tvcast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.mediarouter.app.MediaRouteButton;
import com.applovin.impl.sdk.utils.JsonUtils;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.studio08.xbgamestream.Helpers.PopupWebview;
import com.studio08.xbgamestream.MainActivity;
import com.studio08.xbgamestream.databinding.FragmentTvcastRemoteBinding;
/* loaded from: /app/base.apk/classes3.dex */
public class TVCastFragment extends Fragment {
    private FragmentTvcastRemoteBinding remoteBinding;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        ((MainActivity) getActivity()).analyticsClient.logFragmentCreated("tvcast");
        FragmentTvcastRemoteBinding inflate = FragmentTvcastRemoteBinding.inflate(layoutInflater, viewGroup, false);
        this.remoteBinding = inflate;
        ConstraintLayout root = inflate.getRoot();
        MediaRouteButton mediaRouteButton = this.remoteBinding.tvcastButton;
        CastButtonFactory.setUpMediaRouteButton(getActivity(), mediaRouteButton);
        this.remoteBinding.tvcastRefreshButton.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.tvcast.TVCastFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                try {
                    if (TVCastFragment.this.isCastConnected()) {
                        ((MainActivity) TVCastFragment.this.requireActivity()).mCastSession.sendMessage("urn:x-cast:xbplay-refreshVideo", JsonUtils.EMPTY_JSON);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        this.remoteBinding.tvcastToggleInfo.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.tvcast.TVCastFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                try {
                    if (TVCastFragment.this.isCastConnected()) {
                        ((MainActivity) TVCastFragment.this.requireActivity()).mCastSession.sendMessage("urn:x-cast:xbplay-toggleInfoBar", JsonUtils.EMPTY_JSON);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mediaRouteButton.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.tvcast.TVCastFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ((MainActivity) TVCastFragment.this.getActivity()).analyticsClient.logButtonClickEvent("open_tvcast");
            }
        });
        this.remoteBinding.helpButton.setVisibility(0);
        this.remoteBinding.helpButton.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.tvcast.TVCastFragment.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                new PopupWebview(TVCastFragment.this.getActivity()).showPopup(view, PopupWebview.TV_CAST_POPUP);
            }
        });
        return root;
    }

    public boolean isCastConnected() {
        if (((MainActivity) requireActivity()).mCastSession == null || !((MainActivity) requireActivity()).mCastSession.isConnected()) {
            Toast.makeText(getActivity(), "Not casting. Click the cast button first!", 0).show();
            return false;
        }
        return true;
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        this.remoteBinding = null;
    }
}
