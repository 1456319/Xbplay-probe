package com.studio08.xbgamestream.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.studio08.xbgamestream.R;
/* loaded from: /app/base.apk/classes3.dex */
public final class AppBarMainBinding implements ViewBinding {
    public final FloatingActionButton fab;
    private final CoordinatorLayout rootView;
    public final Toolbar toolbar;

    private AppBarMainBinding(CoordinatorLayout coordinatorLayout, FloatingActionButton floatingActionButton, Toolbar toolbar) {
        this.rootView = coordinatorLayout;
        this.fab = floatingActionButton;
        this.toolbar = toolbar;
    }

    @Override // androidx.viewbinding.ViewBinding
    public CoordinatorLayout getRoot() {
        return this.rootView;
    }

    public static AppBarMainBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static AppBarMainBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.app_bar_main, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static AppBarMainBinding bind(View view) {
        int i = R.id.fab;
        FloatingActionButton floatingActionButton = (FloatingActionButton) ViewBindings.findChildViewById(view, i);
        if (floatingActionButton != null) {
            i = R.id.toolbar;
            Toolbar toolbar = (Toolbar) ViewBindings.findChildViewById(view, i);
            if (toolbar != null) {
                return new AppBarMainBinding((CoordinatorLayout) view, floatingActionButton, toolbar);
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
