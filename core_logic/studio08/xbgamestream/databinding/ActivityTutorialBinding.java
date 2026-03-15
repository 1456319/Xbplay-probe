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
public final class ActivityTutorialBinding implements ViewBinding {
    private final CoordinatorLayout rootView;
    public final Toolbar toolbar;
    public final StreamWebview webview1;

    private ActivityTutorialBinding(CoordinatorLayout coordinatorLayout, Toolbar toolbar, StreamWebview streamWebview) {
        this.rootView = coordinatorLayout;
        this.toolbar = toolbar;
        this.webview1 = streamWebview;
    }

    @Override // androidx.viewbinding.ViewBinding
    public CoordinatorLayout getRoot() {
        return this.rootView;
    }

    public static ActivityTutorialBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static ActivityTutorialBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.activity_tutorial, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static ActivityTutorialBinding bind(View view) {
        int i = R.id.toolbar;
        Toolbar toolbar = (Toolbar) ViewBindings.findChildViewById(view, i);
        if (toolbar != null) {
            i = R.id.webview1;
            StreamWebview streamWebview = (StreamWebview) ViewBindings.findChildViewById(view, i);
            if (streamWebview != null) {
                return new ActivityTutorialBinding((CoordinatorLayout) view, toolbar, streamWebview);
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
