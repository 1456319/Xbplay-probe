package com.studio08.xbgamestream.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.studio08.xbgamestream.R;
/* loaded from: /app/base.apk/classes3.dex */
public final class FragmentHomeBinding implements ViewBinding {
    public final Button connectButton;
    public final FloatingActionButton helpButton;
    public final ConstraintLayout homefrag1;
    private final ConstraintLayout rootView;
    public final TextView textHome;

    private FragmentHomeBinding(ConstraintLayout constraintLayout, Button button, FloatingActionButton floatingActionButton, ConstraintLayout constraintLayout2, TextView textView) {
        this.rootView = constraintLayout;
        this.connectButton = button;
        this.helpButton = floatingActionButton;
        this.homefrag1 = constraintLayout2;
        this.textHome = textView;
    }

    @Override // androidx.viewbinding.ViewBinding
    public ConstraintLayout getRoot() {
        return this.rootView;
    }

    public static FragmentHomeBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static FragmentHomeBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.fragment_home, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static FragmentHomeBinding bind(View view) {
        int i = R.id.connect_button;
        Button button = (Button) ViewBindings.findChildViewById(view, i);
        if (button != null) {
            i = R.id.help_button;
            FloatingActionButton floatingActionButton = (FloatingActionButton) ViewBindings.findChildViewById(view, i);
            if (floatingActionButton != null) {
                ConstraintLayout constraintLayout = (ConstraintLayout) view;
                i = R.id.text_home;
                TextView textView = (TextView) ViewBindings.findChildViewById(view, i);
                if (textView != null) {
                    return new FragmentHomeBinding(constraintLayout, button, floatingActionButton, constraintLayout, textView);
                }
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
