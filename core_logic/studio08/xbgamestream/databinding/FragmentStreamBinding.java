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
import com.studio08.xbgamestream.Web.StreamWebview;
/* loaded from: /app/base.apk/classes3.dex */
public final class FragmentStreamBinding implements ViewBinding {
    private final ConstraintLayout rootView;
    public final Button streamButton;
    public final TextView textStream;
    public final StreamWebview webview1;

    private FragmentStreamBinding(ConstraintLayout constraintLayout, Button button, TextView textView, StreamWebview streamWebview) {
        this.rootView = constraintLayout;
        this.streamButton = button;
        this.textStream = textView;
        this.webview1 = streamWebview;
    }

    @Override // androidx.viewbinding.ViewBinding
    public ConstraintLayout getRoot() {
        return this.rootView;
    }

    public static FragmentStreamBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static FragmentStreamBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.fragment_stream, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static FragmentStreamBinding bind(View view) {
        int i = R.id.stream_button;
        Button button = (Button) ViewBindings.findChildViewById(view, i);
        if (button != null) {
            i = R.id.text_stream;
            TextView textView = (TextView) ViewBindings.findChildViewById(view, i);
            if (textView != null) {
                i = R.id.webview1;
                StreamWebview streamWebview = (StreamWebview) ViewBindings.findChildViewById(view, i);
                if (streamWebview != null) {
                    return new FragmentStreamBinding((ConstraintLayout) view, button, textView, streamWebview);
                }
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
