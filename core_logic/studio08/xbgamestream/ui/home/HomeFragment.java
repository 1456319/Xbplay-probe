package com.studio08.xbgamestream.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.studio08.xbgamestream.Authenticate.LoginActivity;
import com.studio08.xbgamestream.Helpers.PopupWebview;
import com.studio08.xbgamestream.MainActivity;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.databinding.FragmentHomeBinding;
/* loaded from: /app/base.apk/classes3.dex */
public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        ((MainActivity) getActivity()).analyticsClient.logFragmentCreated("consoles");
        this.homeViewModel = (HomeViewModel) new ViewModelProvider(this).get(HomeViewModel.class);
        FragmentHomeBinding inflate = FragmentHomeBinding.inflate(layoutInflater, viewGroup, false);
        this.binding = inflate;
        ConstraintLayout root = inflate.getRoot();
        TextView textView = this.binding.textHome;
        if (!TextUtils.isEmpty(((MainActivity) requireActivity()).consoleText)) {
            textView.setText(((MainActivity) requireActivity()).consoleText);
        }
        ((Button) root.findViewById(R.id.connect_button)).setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.home.HomeFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ((MainActivity) HomeFragment.this.getActivity()).analyticsClient.logButtonClickEvent("open_console_connect");
                HomeFragment.this.getActivity().startActivityForResult(new Intent(view.getContext(), LoginActivity.class), 444);
            }
        });
        this.binding.helpButton.setVisibility(0);
        this.binding.helpButton.setOnClickListener(new View.OnClickListener() { // from class: com.studio08.xbgamestream.ui.home.HomeFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                new PopupWebview(HomeFragment.this.getActivity()).showPopup(view, PopupWebview.CONSOLES_POPUP);
            }
        });
        return root;
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }
}
