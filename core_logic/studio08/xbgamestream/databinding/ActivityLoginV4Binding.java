package com.studio08.xbgamestream.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.Web.StreamWebview;
/* loaded from: /app/base.apk/classes3.dex */
public final class ActivityLoginV4Binding implements ViewBinding {
    private final CoordinatorLayout rootView;
    public final StreamWebview webview1;

    private ActivityLoginV4Binding(CoordinatorLayout coordinatorLayout, StreamWebview streamWebview) {
        this.rootView = coordinatorLayout;
        this.webview1 = streamWebview;
    }

    @Override // androidx.viewbinding.ViewBinding
    public CoordinatorLayout getRoot() {
        return this.rootView;
    }

    public static ActivityLoginV4Binding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static ActivityLoginV4Binding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.activity_login_v4, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static ActivityLoginV4Binding bind(View view) {
        int i = R.id.webview1;
        StreamWebview streamWebview = (StreamWebview) ViewBindings.findChildViewById(view, i);
        if (streamWebview != null) {
            return new ActivityLoginV4Binding((CoordinatorLayout) view, streamWebview);
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
