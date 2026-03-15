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
import com.studio08.xbgamestream.Helpers.PopupWebview;
import com.studio08.xbgamestream.Helpers.RewardedAdLoader;
import com.studio08.xbgamestream.MainActivity;
import com.studio08.xbgamestream.ScreenCastActivity;
import com.studio08.xbgamestream.databinding.FragmentFilecastBinding;
/* loaded from: /app/base.apk/classes3.dex */
public class FileCastFragment extends Fragment {
    private FragmentFilecastBinding binding;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        ((MainActivity) getActivity()).analyticsClient.logFragmentCreated("video_cast");
        FragmentFilecastBinding inflate = FragmentFilecastBinding.inflate(layoutInflater, viewGroup, false);
        this.binding = inflate;
        ConstraintLayout root = inflate.getRoot();
        Button button = this.binding.filecastButton;
        button.setVisibility(0);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.filecast.FileCastFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ((MainActivity) FileCastFragment.this.getActivity()).analyticsClient.logButtonClickEvent("open_video_cast");
                ((MainActivity) FileCastFragment.this.getActivity()).rewardedAd.setCallbackListener(new RewardedAdLoader.RewardAdListener() { // from class: com.studio08.xbgamestream.ui.filecast.FileCastFragment.1.1
                    @Override // com.studio08.xbgamestream.Helpers.RewardedAdLoader.RewardAdListener
                    public void onRewardComplete() {
                        Log.e("HERE", "onRewardCompleteCaught!");
                        try {
                            Intent intent = new Intent((MainActivity) FileCastFragment.this.getActivity(), ScreenCastActivity.class);
                            intent.setFlags(131072);
                            intent.putExtra("showCastView", true);
                            intent.putExtra("audioCastType", false);
                            FileCastFragment.this.startActivity(intent);
                            FileCastFragment.this.getActivity().overridePendingTransition(17432576, 17432577);
                        } catch (Exception e) {
                            Log.e("HERE", "Failed to start file cast activity");
                            e.printStackTrace();
                        }
                    }
                });
                ((MainActivity) FileCastFragment.this.getActivity()).showConnectAdPossibly();
            }
        });
        this.binding.helpButton.setVisibility(0);
        this.binding.helpButton.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.filecast.FileCastFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                new PopupWebview(FileCastFragment.this.getActivity()).showPopup(view, PopupWebview.FILECAST_POPUP);
            }
        });
        return root;
    }
}
