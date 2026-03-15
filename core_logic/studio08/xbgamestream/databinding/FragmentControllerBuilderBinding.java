package com.studio08.xbgamestream.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.Web.StreamWebview;
import org.mozilla.geckoview.GeckoView;
/* loaded from: /app/base.apk/classes3.dex */
public final class FragmentControllerBuilderBinding implements ViewBinding {
    public final Button builderButton;
    public final GeckoView geckowebview;
    public final FloatingActionButton helpButton;
    private final ConstraintLayout rootView;
    public final TextView textBuilder;
    public final StreamWebview webview1;

    private FragmentControllerBuilderBinding(ConstraintLayout constraintLayout, Button button, GeckoView geckoView, FloatingActionButton floatingActionButton, TextView textView, StreamWebview streamWebview) {
        this.rootView = constraintLayout;
        this.builderButton = button;
        this.geckowebview = geckoView;
        this.helpButton = floatingActionButton;
        this.textBuilder = textView;
        this.webview1 = streamWebview;
    }

    @Override // androidx.viewbinding.ViewBinding
    public ConstraintLayout getRoot() {
        return this.rootView;
    }

    public static FragmentControllerBuilderBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static FragmentControllerBuilderBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.fragment_controller_builder, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static FragmentControllerBuilderBinding bind(View view) {
        int i = R.id.builder_button;
        Button button = (Button) ViewBindings.findChildViewById(view, i);
        if (button != null) {
            i = R.id.geckowebview;
            GeckoView geckoView = (GeckoView) ViewBindings.findChildViewById(view, i);
            if (geckoView != null) {
                i = R.id.help_button;
                FloatingActionButton floatingActionButton = (FloatingActionButton) ViewBindings.findChildViewById(view, i);
                if (floatingActionButton != null) {
                    i = R.id.text_builder;
                    TextView textView = (TextView) ViewBindings.findChildViewById(view, i);
                    if (textView != null) {
                        i = R.id.webview1;
                        StreamWebview streamWebview = (StreamWebview) ViewBindings.findChildViewById(view, i);
                        if (streamWebview != null) {
                            return new FragmentControllerBuilderBinding((ConstraintLayout) view, button, geckoView, floatingActionButton, textView, streamWebview);
                        }
                    }
                }
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
