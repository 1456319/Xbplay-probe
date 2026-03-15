package com.studio08.xbgamestream.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.Web.StreamWebview;
/* loaded from: /app/base.apk/classes3.dex */
public final class ActivityFullscreenBinding implements ViewBinding {
    public final ConstraintLayout constrainedLayout;
    private final ConstraintLayout rootView;
    public final StreamWebview systemwebview;

    private ActivityFullscreenBinding(ConstraintLayout constraintLayout, ConstraintLayout constraintLayout2, StreamWebview streamWebview) {
        this.rootView = constraintLayout;
        this.constrainedLayout = constraintLayout2;
        this.systemwebview = streamWebview;
    }

    @Override // androidx.viewbinding.ViewBinding
    public ConstraintLayout getRoot() {
        return this.rootView;
    }

    public static ActivityFullscreenBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static ActivityFullscreenBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.activity_fullscreen, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static ActivityFullscreenBinding bind(View view) {
        ConstraintLayout constraintLayout = (ConstraintLayout) view;
        int i = R.id.systemwebview;
        StreamWebview streamWebview = (StreamWebview) ViewBindings.findChildViewById(view, i);
        if (streamWebview != null) {
            return new ActivityFullscreenBinding(constraintLayout, constraintLayout, streamWebview);
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
