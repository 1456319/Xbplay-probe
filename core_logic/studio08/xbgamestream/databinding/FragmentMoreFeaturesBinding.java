package com.studio08.xbgamestream.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.studio08.xbgamestream.R;
/* loaded from: /app/base.apk/classes3.dex */
public final class FragmentMoreFeaturesBinding implements ViewBinding {
    public final Button downloadFeaturesButton;
    public final ConstraintLayout filecastFrag1;
    private final ConstraintLayout rootView;
    public final TextView textMoreFeaturesFooter;
    public final TextView textMoreFeaturesHeader;
    public final Button viewFeaturesButton;

    private FragmentMoreFeaturesBinding(ConstraintLayout constraintLayout, Button button, ConstraintLayout constraintLayout2, TextView textView, TextView textView2, Button button2) {
        this.rootView = constraintLayout;
        this.downloadFeaturesButton = button;
        this.filecastFrag1 = constraintLayout2;
        this.textMoreFeaturesFooter = textView;
        this.textMoreFeaturesHeader = textView2;
        this.viewFeaturesButton = button2;
    }

    @Override // androidx.viewbinding.ViewBinding
    public ConstraintLayout getRoot() {
        return this.rootView;
    }

    public static FragmentMoreFeaturesBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static FragmentMoreFeaturesBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.fragment_more_features, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static FragmentMoreFeaturesBinding bind(View view) {
        int i = R.id.download_features_button;
        Button button = (Button) ViewBindings.findChildViewById(view, i);
        if (button != null) {
            ConstraintLayout constraintLayout = (ConstraintLayout) view;
            i = R.id.text_more_features_footer;
            TextView textView = (TextView) ViewBindings.findChildViewById(view, i);
            if (textView != null) {
                i = R.id.text_more_features_header;
                TextView textView2 = (TextView) ViewBindings.findChildViewById(view, i);
                if (textView2 != null) {
                    i = R.id.view_features_button;
                    Button button2 = (Button) ViewBindings.findChildViewById(view, i);
                    if (button2 != null) {
                        return new FragmentMoreFeaturesBinding(constraintLayout, button, constraintLayout, textView, textView2, button2);
                    }
                }
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
