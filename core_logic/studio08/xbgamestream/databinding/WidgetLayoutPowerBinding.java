package com.studio08.xbgamestream.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.studio08.xbgamestream.R;
/* loaded from: /app/base.apk/classes3.dex */
public final class WidgetLayoutPowerBinding implements ViewBinding {
    public final RelativeLayout oneBkRlTwo;
    public final ImageButton powerOn;
    public final ImageButton powerWaiting;
    private final RelativeLayout rootView;
    public final RelativeLayout widgetLayout;

    private WidgetLayoutPowerBinding(RelativeLayout relativeLayout, RelativeLayout relativeLayout2, ImageButton imageButton, ImageButton imageButton2, RelativeLayout relativeLayout3) {
        this.rootView = relativeLayout;
        this.oneBkRlTwo = relativeLayout2;
        this.powerOn = imageButton;
        this.powerWaiting = imageButton2;
        this.widgetLayout = relativeLayout3;
    }

    @Override // androidx.viewbinding.ViewBinding
    public RelativeLayout getRoot() {
        return this.rootView;
    }

    public static WidgetLayoutPowerBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static WidgetLayoutPowerBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.widget_layout_power, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static WidgetLayoutPowerBinding bind(View view) {
        int i = R.id.one_bk_rl_two;
        RelativeLayout relativeLayout = (RelativeLayout) ViewBindings.findChildViewById(view, i);
        if (relativeLayout != null) {
            i = R.id.power_on;
            ImageButton imageButton = (ImageButton) ViewBindings.findChildViewById(view, i);
            if (imageButton != null) {
                i = R.id.power_waiting;
                ImageButton imageButton2 = (ImageButton) ViewBindings.findChildViewById(view, i);
                if (imageButton2 != null) {
                    RelativeLayout relativeLayout2 = (RelativeLayout) view;
                    return new WidgetLayoutPowerBinding(relativeLayout2, relativeLayout, imageButton, imageButton2, relativeLayout2);
                }
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
