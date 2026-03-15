package com.studio08.xbgamestream.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.Web.StreamWebview;
/* loaded from: /app/base.apk/classes3.dex */
public final class ActivityLoginBinding implements ViewBinding {
    private final CoordinatorLayout rootView;
    public final Toolbar toolbar;
    public final StreamWebview webview1;

    private ActivityLoginBinding(CoordinatorLayout coordinatorLayout, Toolbar toolbar, StreamWebview streamWebview) {
        this.rootView = coordinatorLayout;
        this.toolbar = toolbar;
        this.webview1 = streamWebview;
    }

    @Override // androidx.viewbinding.ViewBinding
    public CoordinatorLayout getRoot() {
        return this.rootView;
    }

    public static ActivityLoginBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static ActivityLoginBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.activity_login, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static ActivityLoginBinding bind(View view) {
        int i = R.id.toolbar;
        Toolbar toolbar = (Toolbar) ViewBindings.findChildViewById(view, i);
        if (toolbar != null) {
            i = R.id.webview1;
            StreamWebview streamWebview = (StreamWebview) ViewBindings.findChildViewById(view, i);
            if (streamWebview != null) {
                return new ActivityLoginBinding((CoordinatorLayout) view, toolbar, streamWebview);
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
