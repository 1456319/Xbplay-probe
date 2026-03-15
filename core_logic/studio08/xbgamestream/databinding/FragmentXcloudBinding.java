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
public final class FragmentXcloudBinding implements ViewBinding {
    public final GeckoView geckowebview;
    public final FloatingActionButton helpButton;
    private final ConstraintLayout rootView;
    public final TextView textXcloud;
    public final TextView textXcloudFooter;
    public final StreamWebview webview1;
    public final Button xcloudButton;

    private FragmentXcloudBinding(ConstraintLayout constraintLayout, GeckoView geckoView, FloatingActionButton floatingActionButton, TextView textView, TextView textView2, StreamWebview streamWebview, Button button) {
        this.rootView = constraintLayout;
        this.geckowebview = geckoView;
        this.helpButton = floatingActionButton;
        this.textXcloud = textView;
        this.textXcloudFooter = textView2;
        this.webview1 = streamWebview;
        this.xcloudButton = button;
    }

    @Override // androidx.viewbinding.ViewBinding
    public ConstraintLayout getRoot() {
        return this.rootView;
    }

    public static FragmentXcloudBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static FragmentXcloudBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.fragment_xcloud, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static FragmentXcloudBinding bind(View view) {
        int i = R.id.geckowebview;
        GeckoView geckoView = (GeckoView) ViewBindings.findChildViewById(view, i);
        if (geckoView != null) {
            i = R.id.help_button;
            FloatingActionButton floatingActionButton = (FloatingActionButton) ViewBindings.findChildViewById(view, i);
            if (floatingActionButton != null) {
                i = R.id.text_xcloud;
                TextView textView = (TextView) ViewBindings.findChildViewById(view, i);
                if (textView != null) {
                    i = R.id.text_xcloud_footer;
                    TextView textView2 = (TextView) ViewBindings.findChildViewById(view, i);
                    if (textView2 != null) {
                        i = R.id.webview1;
                        StreamWebview streamWebview = (StreamWebview) ViewBindings.findChildViewById(view, i);
                        if (streamWebview != null) {
                            i = R.id.xcloud_button;
                            Button button = (Button) ViewBindings.findChildViewById(view, i);
                            if (button != null) {
                                return new FragmentXcloudBinding((ConstraintLayout) view, geckoView, floatingActionButton, textView, textView2, streamWebview, button);
                            }
                        }
                    }
                }
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
