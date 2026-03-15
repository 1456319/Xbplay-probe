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
public final class PopupViewBinding implements ViewBinding {
    public final ProgressBar progressBar2;
    private final ConstraintLayout rootView;
    public final StreamWebview webview1;

    private PopupViewBinding(ConstraintLayout constraintLayout, ProgressBar progressBar, StreamWebview streamWebview) {
        this.rootView = constraintLayout;
        this.progressBar2 = progressBar;
        this.webview1 = streamWebview;
    }

    @Override // androidx.viewbinding.ViewBinding
    public ConstraintLayout getRoot() {
        return this.rootView;
    }

    public static PopupViewBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static PopupViewBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.popup_view, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static PopupViewBinding bind(View view) {
        int i = R.id.progressBar2;
        ProgressBar progressBar = (ProgressBar) ViewBindings.findChildViewById(view, i);
        if (progressBar != null) {
            i = R.id.webview1;
            StreamWebview streamWebview = (StreamWebview) ViewBindings.findChildViewById(view, i);
            if (streamWebview != null) {
                return new PopupViewBinding((ConstraintLayout) view, progressBar, streamWebview);
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
