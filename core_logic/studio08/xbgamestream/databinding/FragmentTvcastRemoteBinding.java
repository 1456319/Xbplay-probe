package com.studio08.xbgamestream.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.studio08.xbgamestream.R;
/* loaded from: /app/base.apk/classes3.dex */
public final class FragmentTvcastRemoteBinding implements ViewBinding {
    public final FloatingActionButton helpButton;
    private final ConstraintLayout rootView;
    public final TextView textTvcastFooter;
    public final TextView textTvcastHeader;
    public final MediaRouteButton tvcastButton;
    public final Button tvcastRefreshButton;
    public final ConstraintLayout tvcastRemote;
    public final Button tvcastToggleInfo;

    private FragmentTvcastRemoteBinding(ConstraintLayout constraintLayout, FloatingActionButton floatingActionButton, TextView textView, TextView textView2, MediaRouteButton mediaRouteButton, Button button, ConstraintLayout constraintLayout2, Button button2) {
        this.rootView = constraintLayout;
        this.helpButton = floatingActionButton;
        this.textTvcastFooter = textView;
        this.textTvcastHeader = textView2;
        this.tvcastButton = mediaRouteButton;
        this.tvcastRefreshButton = button;
        this.tvcastRemote = constraintLayout2;
        this.tvcastToggleInfo = button2;
    }

    @Override // androidx.viewbinding.ViewBinding
    public ConstraintLayout getRoot() {
        return this.rootView;
    }

    public static FragmentTvcastRemoteBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static FragmentTvcastRemoteBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.fragment_tvcast_remote, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static FragmentTvcastRemoteBinding bind(View view) {
        int i = R.id.help_button;
        FloatingActionButton floatingActionButton = (FloatingActionButton) ViewBindings.findChildViewById(view, i);
        if (floatingActionButton != null) {
            i = R.id.text_tvcast_footer;
            TextView textView = (TextView) ViewBindings.findChildViewById(view, i);
            if (textView != null) {
                i = R.id.text_tvcast_header;
                TextView textView2 = (TextView) ViewBindings.findChildViewById(view, i);
                if (textView2 != null) {
                    i = R.id.tvcast_button;
                    MediaRouteButton mediaRouteButton = (MediaRouteButton) ViewBindings.findChildViewById(view, i);
                    if (mediaRouteButton != null) {
                        i = R.id.tvcast_refresh_button;
                        Button button = (Button) ViewBindings.findChildViewById(view, i);
                        if (button != null) {
                            ConstraintLayout constraintLayout = (ConstraintLayout) view;
                            i = R.id.tvcast_toggle_info;
                            Button button2 = (Button) ViewBindings.findChildViewById(view, i);
                            if (button2 != null) {
                                return new FragmentTvcastRemoteBinding(constraintLayout, floatingActionButton, textView, textView2, mediaRouteButton, button, constraintLayout, button2);
                            }
                        }
                    }
                }
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
