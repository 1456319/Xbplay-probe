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
import com.studio08.xbgamestream.Web.StreamWebview;
/* loaded from: /app/base.apk/classes3.dex */
public final class FragmentVoiceremoteBinding implements ViewBinding {
    public final FloatingActionButton helpButton;
    public final Button remoteBuilderButton;
    public final Button remoteButton;
    public final ConstraintLayout remoteFrag1;
    private final ConstraintLayout rootView;
    public final TextView textRemoteHeader;
    public final StreamWebview webview1;

    private FragmentVoiceremoteBinding(ConstraintLayout constraintLayout, FloatingActionButton floatingActionButton, Button button, Button button2, ConstraintLayout constraintLayout2, TextView textView, StreamWebview streamWebview) {
        this.rootView = constraintLayout;
        this.helpButton = floatingActionButton;
        this.remoteBuilderButton = button;
        this.remoteButton = button2;
        this.remoteFrag1 = constraintLayout2;
        this.textRemoteHeader = textView;
        this.webview1 = streamWebview;
    }

    @Override // androidx.viewbinding.ViewBinding
    public ConstraintLayout getRoot() {
        return this.rootView;
    }

    public static FragmentVoiceremoteBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static FragmentVoiceremoteBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.fragment_voiceremote, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static FragmentVoiceremoteBinding bind(View view) {
        int i = R.id.help_button;
        FloatingActionButton floatingActionButton = (FloatingActionButton) ViewBindings.findChildViewById(view, i);
        if (floatingActionButton != null) {
            i = R.id.remote_builder_button;
            Button button = (Button) ViewBindings.findChildViewById(view, i);
            if (button != null) {
                i = R.id.remote_button;
                Button button2 = (Button) ViewBindings.findChildViewById(view, i);
                if (button2 != null) {
                    ConstraintLayout constraintLayout = (ConstraintLayout) view;
                    i = R.id.text_remote_header;
                    TextView textView = (TextView) ViewBindings.findChildViewById(view, i);
                    if (textView != null) {
                        i = R.id.webview1;
                        StreamWebview streamWebview = (StreamWebview) ViewBindings.findChildViewById(view, i);
                        if (streamWebview != null) {
                            return new FragmentVoiceremoteBinding(constraintLayout, floatingActionButton, button, button2, constraintLayout, textView, streamWebview);
                        }
                    }
                }
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
