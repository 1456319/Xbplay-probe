package com.studio08.xbgamestream.ui.filecast;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import com.studio08.xbgamestream.Helpers.RewardedAdLoader;
import com.studio08.xbgamestream.MainActivity;
import com.studio08.xbgamestream.ScreenCastActivity;
import com.studio08.xbgamestream.databinding.FragmentFilecastMediaRemoteBinding;
/* loaded from: /app/base.apk/classes3.dex */
public class CastMediaRemoteFragment extends Fragment {
    private FragmentFilecastMediaRemoteBinding binding;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        ((MainActivity) getActivity()).analyticsClient.logFragmentCreated("cast_remote");
        FragmentFilecastMediaRemoteBinding inflate = FragmentFilecastMediaRemoteBinding.inflate(layoutInflater, viewGroup, false);
        this.binding = inflate;
        ConstraintLayout root = inflate.getRoot();
        Button button = this.binding.filecastButton;
        button.setVisibility(0);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.filecast.CastMediaRemoteFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ((MainActivity) CastMediaRemoteFragment.this.getActivity()).analyticsClient.logButtonClickEvent("open_cast_remote");
                ((MainActivity) CastMediaRemoteFragment.this.getActivity()).rewardedAd.setCallbackListener(new RewardedAdLoader.RewardAdListener() { // from class: com.studio08.xbgamestream.ui.filecast.CastMediaRemoteFragment.1.1
                    @Override // com.studio08.xbgamestream.Helpers.RewardedAdLoader.RewardAdListener
                    public void onRewardComplete() {
                        Log.e("HERE", "onRewardCompleteCaught!");
                        Intent intent = new Intent(CastMediaRemoteFragment.this.getContext(), ScreenCastActivity.class);
                        intent.setFlags(131072);
                        intent.putExtra("showRemoteView", true);
                        CastMediaRemoteFragment.this.startActivity(intent);
                        CastMediaRemoteFragment.this.getActivity().overridePendingTransition(17432576, 17432577);
                    }
                });
                ((MainActivity) CastMediaRemoteFragment.this.getActivity()).showConnectAdPossibly();
            }
        });
        return root;
    }
}
