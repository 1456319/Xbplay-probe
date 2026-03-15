package com.studio08.xbgamestream.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.Web.StreamWebview;
/* loaded from: /app/base.apk/classes3.dex */
public final class FragmentWidgetsBinding implements ViewBinding {
    private final ConstraintLayout rootView;
    public final StreamWebview webview1;
    public final ProgressBar widgetConnected;

    private FragmentWidgetsBinding(ConstraintLayout constraintLayout, StreamWebview streamWebview, ProgressBar progressBar) {
        this.rootView = constraintLayout;
        this.webview1 = streamWebview;
        this.widgetConnected = progressBar;
    }

    @Override // androidx.viewbinding.ViewBinding
    public ConstraintLayout getRoot() {
        return this.rootView;
    }

    public static FragmentWidgetsBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static FragmentWidgetsBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.fragment_widgets, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static FragmentWidgetsBinding bind(View view) {
        int i = R.id.webview1;
        StreamWebview streamWebview = (StreamWebview) ViewBindings.findChildViewById(view, i);
        if (streamWebview != null) {
            i = R.id.widget_connected;
            ProgressBar progressBar = (ProgressBar) ViewBindings.findChildViewById(view, i);
            if (progressBar != null) {
                return new FragmentWidgetsBinding((ConstraintLayout) view, streamWebview, progressBar);
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
