package com.studio08.xbgamestream.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.studio08.xbgamestream.R;
/* loaded from: /app/base.apk/classes3.dex */
public final class FragmentFilecastMediaRemoteBinding implements ViewBinding {
    public final Button filecastButton;
    public final ConstraintLayout filecastFrag1;
    private final ConstraintLayout rootView;
    public final TextView textFilecastFooter;
    public final TextView textFilecastHeader;

    private FragmentFilecastMediaRemoteBinding(ConstraintLayout constraintLayout, Button button, ConstraintLayout constraintLayout2, TextView textView, TextView textView2) {
        this.rootView = constraintLayout;
        this.filecastButton = button;
        this.filecastFrag1 = constraintLayout2;
        this.textFilecastFooter = textView;
        this.textFilecastHeader = textView2;
    }

    @Override // androidx.viewbinding.ViewBinding
    public ConstraintLayout getRoot() {
        return this.rootView;
    }

    public static FragmentFilecastMediaRemoteBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static FragmentFilecastMediaRemoteBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.fragment_filecast_media_remote, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static FragmentFilecastMediaRemoteBinding bind(View view) {
        int i = R.id.filecast_button;
        Button button = (Button) ViewBindings.findChildViewById(view, i);
        if (button != null) {
            ConstraintLayout constraintLayout = (ConstraintLayout) view;
            i = R.id.text_filecast_footer;
            TextView textView = (TextView) ViewBindings.findChildViewById(view, i);
            if (textView != null) {
                i = R.id.text_filecast_header;
                TextView textView2 = (TextView) ViewBindings.findChildViewById(view, i);
                if (textView2 != null) {
                    return new FragmentFilecastMediaRemoteBinding(constraintLayout, button, constraintLayout, textView, textView2);
                }
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
