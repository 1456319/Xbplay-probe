package com.studio08.xbgamestream.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.appbar.AppBarLayout;
import com.studio08.xbgamestream.R;
import com.studio08.xbgamestream.Web.StreamWebview;
/* loaded from: /app/base.apk/classes3.dex */
public final class ActivityScreenCastBinding implements ViewBinding {
    public final AppBarLayout appBar;
    public final Button castConnectButton;
    public final Button castFileChooseButton;
    public final Button castRemoteButton;
    public final Button castSendButton;
    public final TextView leftSeekTv;
    public final TextView rightSeekTv;
    private final ConstraintLayout rootView;
    public final SeekBar seekbar;
    public final RelativeLayout seekbarLayout;
    public final TextView textCast;
    public final TextView textCastFooter;
    public final Toolbar toolbar;
    public final StreamWebview webview1;

    private ActivityScreenCastBinding(ConstraintLayout constraintLayout, AppBarLayout appBarLayout, Button button, Button button2, Button button3, Button button4, TextView textView, TextView textView2, SeekBar seekBar, RelativeLayout relativeLayout, TextView textView3, TextView textView4, Toolbar toolbar, StreamWebview streamWebview) {
        this.rootView = constraintLayout;
        this.appBar = appBarLayout;
        this.castConnectButton = button;
        this.castFileChooseButton = button2;
        this.castRemoteButton = button3;
        this.castSendButton = button4;
        this.leftSeekTv = textView;
        this.rightSeekTv = textView2;
        this.seekbar = seekBar;
        this.seekbarLayout = relativeLayout;
        this.textCast = textView3;
        this.textCastFooter = textView4;
        this.toolbar = toolbar;
        this.webview1 = streamWebview;
    }

    @Override // androidx.viewbinding.ViewBinding
    public ConstraintLayout getRoot() {
        return this.rootView;
    }

    public static ActivityScreenCastBinding inflate(LayoutInflater layoutInflater) {
        return inflate(layoutInflater, null, false);
    }

    public static ActivityScreenCastBinding inflate(LayoutInflater layoutInflater, ViewGroup viewGroup, boolean z) {
        View inflate = layoutInflater.inflate(R.layout.activity_screen_cast, viewGroup, false);
        if (z) {
            viewGroup.addView(inflate);
        }
        return bind(inflate);
    }

    public static ActivityScreenCastBinding bind(View view) {
        int i = R.id.app_bar;
        AppBarLayout appBarLayout = (AppBarLayout) ViewBindings.findChildViewById(view, i);
        if (appBarLayout != null) {
            i = R.id.cast_connect_button;
            Button button = (Button) ViewBindings.findChildViewById(view, i);
            if (button != null) {
                i = R.id.cast_file_choose_button;
                Button button2 = (Button) ViewBindings.findChildViewById(view, i);
                if (button2 != null) {
                    i = R.id.cast_remote_button;
                    Button button3 = (Button) ViewBindings.findChildViewById(view, i);
                    if (button3 != null) {
                        i = R.id.cast_send_button;
                        Button button4 = (Button) ViewBindings.findChildViewById(view, i);
                        if (button4 != null) {
                            i = R.id.left_seek_tv;
                            TextView textView = (TextView) ViewBindings.findChildViewById(view, i);
                            if (textView != null) {
                                i = R.id.right_seek_tv;
                                TextView textView2 = (TextView) ViewBindings.findChildViewById(view, i);
                                if (textView2 != null) {
                                    i = R.id.seekbar;
                                    SeekBar seekBar = (SeekBar) ViewBindings.findChildViewById(view, i);
                                    if (seekBar != null) {
                                        i = R.id.seekbar_layout;
                                        RelativeLayout relativeLayout = (RelativeLayout) ViewBindings.findChildViewById(view, i);
                                        if (relativeLayout != null) {
                                            i = R.id.text_cast;
                                            TextView textView3 = (TextView) ViewBindings.findChildViewById(view, i);
                                            if (textView3 != null) {
                                                i = R.id.text_cast_footer;
                                                TextView textView4 = (TextView) ViewBindings.findChildViewById(view, i);
                                                if (textView4 != null) {
                                                    i = R.id.toolbar;
                                                    Toolbar toolbar = (Toolbar) ViewBindings.findChildViewById(view, i);
                                                    if (toolbar != null) {
                                                        i = R.id.webview1;
                                                        StreamWebview streamWebview = (StreamWebview) ViewBindings.findChildViewById(view, i);
                                                        if (streamWebview != null) {
                                                            return new ActivityScreenCastBinding((ConstraintLayout) view, appBarLayout, button, button2, button3, button4, textView, textView2, seekBar, relativeLayout, textView3, textView4, toolbar, streamWebview);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        throw new NullPointerException("Missing required view with ID: ".concat(view.getResources().getResourceName(i)));
    }
}
