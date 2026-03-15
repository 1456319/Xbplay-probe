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
public final class FragmentMirrorcastBinding implements ViewBinding {
    public final FloatingActionButton helpButton;
    public final ConstraintLayout homefrag1;
    public final Button mirrorcastButton;
    private final ConstraintLayout rootView;
    public final TextView textMirrorcastFooter;
    public final TextView textMirrorcastHeader;

    private FragmentMirrorcastBinding(ConstraintLayout constraintLayout, FloatingActionButton floatingActionButton, ConstraintLayout constraintLayout2, Button button, TextView textView, TextView textView2) {
        this.rootView = constraintLayout;
        this.helpButton = floatingActionButton;
        this.homefrag1 = constraintLayout2;
        this.mirrorcastButton = button;
        this.textMirrorcastFooter = textView;
        this.textMirrorcastHeader = textView2;
    }

    @Override // androidx.viewbinding.ViewBinding
    public ConstraintLayout getRoot() {
        return this.rootView;
    }

    public static FragmentMirrorcastBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static FragmentMirrorcastBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.fragment_mirrorcast, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static FragmentMirrorcastBinding bind(View view) {
        int i = R.id.help_button;
        FloatingActionButton floatingActionButton = (FloatingActionButton) ViewBindings.findChildViewById(view, i);
        if (floatingActionButton != null) {
            ConstraintLayout constraintLayout = (ConstraintLayout) view;
            i = R.id.mirrorcast_button;
            Button button = (Button) ViewBindings.findChildViewById(view, i);
            if (button != null) {
                i = R.id.text_mirrorcast_footer;
                TextView textView = (TextView) ViewBindings.findChildViewById(view, i);
                if (textView != null) {
                    i = R.id.text_mirrorcast_header;
                    TextView textView2 = (TextView) ViewBindings.findChildViewById(view, i);
                    if (textView2 != null) {
                        return new FragmentMirrorcastBinding(constraintLayout, floatingActionButton, constraintLayout, button, textView, textView2);
                    }
                }
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
