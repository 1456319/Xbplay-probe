package com.studio08.xbgamestream.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.navigation.NavigationView;
import com.studio08.xbgamestream.R;
/* loaded from: /app/base.apk/classes3.dex */
public final class ActivityMainBinding implements ViewBinding {
    public final AppBarMainBinding appBarMain;
    public final DrawerLayout drawerLayout;
    public final NavigationView navView;
    private final DrawerLayout rootView;

    private ActivityMainBinding(DrawerLayout drawerLayout, AppBarMainBinding appBarMainBinding, DrawerLayout drawerLayout2, NavigationView navigationView) {
        this.rootView = drawerLayout;
        this.appBarMain = appBarMainBinding;
        this.drawerLayout = drawerLayout2;
        this.navView = navigationView;
    }

    @Override // androidx.viewbinding.ViewBinding
    public DrawerLayout getRoot() {
        return this.rootView;
    }

    public static ActivityMainBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static ActivityMainBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.activity_main, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static ActivityMainBinding bind(View view) {
        int i = R.id.app_bar_main;
        View findChildViewById = ViewBindings.findChildViewById(view, i);
        if (findChildViewById != null) {
            AppBarMainBinding bind = AppBarMainBinding.bind(findChildViewById);
            DrawerLayout drawerLayout = (DrawerLayout) view;
            int i2 = R.id.nav_view;
            NavigationView navigationView = (NavigationView) ViewBindings.findChildViewById(view, i2);
            if (navigationView != null) {
                return new ActivityMainBinding(drawerLayout, bind, drawerLayout, navigationView);
            }
            i = i2;
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
