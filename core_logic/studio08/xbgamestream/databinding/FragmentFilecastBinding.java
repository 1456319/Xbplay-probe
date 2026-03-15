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
public final class FragmentFilecastBinding implements ViewBinding {
    public final Button filecastButton;
    public final ConstraintLayout filecastFrag1;
    public final FloatingActionButton helpButton;
    private final ConstraintLayout rootView;
    public final TextView textFilecastHeader;

    private FragmentFilecastBinding(ConstraintLayout constraintLayout, Button button, ConstraintLayout constraintLayout2, FloatingActionButton floatingActionButton, TextView textView) {
        this.rootView = constraintLayout;
        this.filecastButton = button;
        this.filecastFrag1 = constraintLayout2;
        this.helpButton = floatingActionButton;
        this.textFilecastHeader = textView;
    }

    @Override // androidx.viewbinding.ViewBinding
    public ConstraintLayout getRoot() {
        return this.rootView;
    }

    public static FragmentFilecastBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static FragmentFilecastBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.fragment_filecast, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static FragmentFilecastBinding bind(View view) {
        int i = R.id.filecast_button;
        Button button = (Button) ViewBindings.findChildViewById(view, i);
        if (button != null) {
            ConstraintLayout constraintLayout = (ConstraintLayout) view;
            i = R.id.help_button;
            FloatingActionButton floatingActionButton = (FloatingActionButton) ViewBindings.findChildViewById(view, i);
            if (floatingActionButton != null) {
                i = R.id.text_filecast_header;
                TextView textView = (TextView) ViewBindings.findChildViewById(view, i);
                if (textView != null) {
                    return new FragmentFilecastBinding(constraintLayout, button, constraintLayout, floatingActionButton, textView);
                }
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
