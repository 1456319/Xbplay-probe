package com.studio08.xbgamestream.ui.extras;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import com.studio08.xbgamestream.Helpers.TutorialActivity;
import com.studio08.xbgamestream.MainActivity;
import com.studio08.xbgamestream.databinding.FragmentMoreFeaturesBinding;
/* loaded from: /app/base.apk/classes3.dex */
public class MoreFeaturesFragment extends Fragment {
    private FragmentMoreFeaturesBinding binding;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        ((MainActivity) getActivity()).analyticsClient.logFragmentCreated("more_features");
        FragmentMoreFeaturesBinding inflate = FragmentMoreFeaturesBinding.inflate(layoutInflater, viewGroup, false);
        this.binding = inflate;
        ConstraintLayout root = inflate.getRoot();
        this.binding.downloadFeaturesButton.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.extras.MoreFeaturesFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ((MainActivity) MoreFeaturesFragment.this.getActivity()).analyticsClient.logButtonClickEvent("open_more_features");
                MoreFeaturesFragment.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=com.studio08.xbgamestream")));
            }
        });
        this.binding.viewFeaturesButton.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.extras.MoreFeaturesFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Intent intent = new Intent(MoreFeaturesFragment.this.getActivity(), TutorialActivity.class);
                intent.putExtra("show_full", true);
                MoreFeaturesFragment.this.startActivity(intent);
            }
        });
        return root;
    }
}
