package com.studio08.xbgamestream.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.studio08.xbgamestream.R;
/* loaded from: /app/base.apk/classes3.dex */
public final class FragmentAndroidTvBinding implements ViewBinding {
    public final ConstraintLayout androidtvfrag1;
    public final FloatingActionButton helpButton;
    public final Button loginToAccount;
    private final ConstraintLayout rootView;
    public final Button sendToTvButton;
    public final TextView textAndroidtvFooter;
    public final TextView textAndroidtvHeader;
    public final EditText tvCodeEditText;

    private FragmentAndroidTvBinding(ConstraintLayout constraintLayout, ConstraintLayout constraintLayout2, FloatingActionButton floatingActionButton, Button button, Button button2, TextView textView, TextView textView2, EditText editText) {
        this.rootView = constraintLayout;
        this.androidtvfrag1 = constraintLayout2;
        this.helpButton = floatingActionButton;
        this.loginToAccount = button;
        this.sendToTvButton = button2;
        this.textAndroidtvFooter = textView;
        this.textAndroidtvHeader = textView2;
        this.tvCodeEditText = editText;
    }

    @Override // androidx.viewbinding.ViewBinding
    public ConstraintLayout getRoot() {
        return this.rootView;
    }

    public static FragmentAndroidTvBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static FragmentAndroidTvBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.fragment_android_tv, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static FragmentAndroidTvBinding bind(View view) {
        ConstraintLayout constraintLayout = (ConstraintLayout) view;
        int i = R.id.help_button;
        FloatingActionButton floatingActionButton = (FloatingActionButton) ViewBindings.findChildViewById(view, i);
        if (floatingActionButton != null) {
            i = R.id.login_to_account;
            Button button = (Button) ViewBindings.findChildViewById(view, i);
            if (button != null) {
                i = R.id.send_to_tv_button;
                Button button2 = (Button) ViewBindings.findChildViewById(view, i);
                if (button2 != null) {
                    i = R.id.text_androidtv_footer;
                    TextView textView = (TextView) ViewBindings.findChildViewById(view, i);
                    if (textView != null) {
                        i = R.id.text_androidtv_header;
                        TextView textView2 = (TextView) ViewBindings.findChildViewById(view, i);
                        if (textView2 != null) {
                            i = R.id.tv_code_edit_text;
                            EditText editText = (EditText) ViewBindings.findChildViewById(view, i);
                            if (editText != null) {
                                return new FragmentAndroidTvBinding(constraintLayout, constraintLayout, floatingActionButton, button, button2, textView, textView2, editText);
                            }
                        }
                    }
                }
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
