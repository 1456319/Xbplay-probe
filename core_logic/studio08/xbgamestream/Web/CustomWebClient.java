package com.studio08.xbgamestream.Web;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.studio08.xbgamestream.Helpers.PWAWebviewHandler;
/* loaded from: /app/base.apk/classes3.dex */
public class CustomWebClient extends WebViewClient {
    private AlertDialog dialog;
    private boolean showLoadingDialog;

    public CustomWebClient(boolean z) {
        this.showLoadingDialog = z;
    }

    private void hideDialog() {
        try {
            this.dialog.dismiss();
        } catch (Exception unused) {
        }
    }

    @Override // android.webkit.WebViewClient
    public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
        super.onPageStarted(webView, str, bitmap);
        try {
            setProgressDialog(webView.getContext());
        } catch (Exception unused) {
        }
    }

    @Override // android.webkit.WebViewClient
    public void onPageFinished(WebView webView, String str) {
        super.onPageFinished(webView, str);
        try {
            hideDialog();
        } catch (Exception unused) {
        }
    }

    @Override // android.webkit.WebViewClient
    public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
        hideDialog();
        Log.e("CustomWV", ((Object) webResourceError.getDescription()) + " - " + webResourceError.getErrorCode() + " - " + webResourceRequest.getUrl().getPath());
        if (webResourceRequest.getUrl().getPath().contains("/image/")) {
            return;
        }
        boolean z = false;
        Toast.makeText(webView.getContext(), "Error - Check Internet. " + ((Object) webResourceError.getDescription()), 0).show();
        try {
            z = webView.getContext().getSharedPreferences("SettingsSharedPref", 0).getBoolean("pwa_use_legacy_theme_key", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!z && webResourceRequest.getUrl() != null && (webResourceRequest.getUrl().getPath().contains("android_stream") || webResourceRequest.getUrl().getPath().contains("title_picker") || webResourceRequest.getUrl().getPath().contains("controller_builder"))) {
            webView.loadUrl(PWAWebviewHandler.PWA_MAIN_MENU);
        } else if (webResourceRequest.getUrl() == null || !webResourceRequest.getUrl().getPath().contains("pwa/main.html")) {
        } else {
            webView.loadUrl("file:///android_asset/warning-screen.html");
        }
    }

    public void cleanup() {
        hideDialog();
    }

    public void setProgressDialog(Context context) {
        if (this.showLoadingDialog) {
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(0);
            linearLayout.setPadding(10, 10, 10, 10);
            linearLayout.setGravity(17);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
            layoutParams.gravity = 17;
            linearLayout.setLayoutParams(layoutParams);
            ProgressBar progressBar = new ProgressBar(context);
            progressBar.setIndeterminate(true);
            progressBar.setPadding(0, 0, 0, 0);
            progressBar.setLayoutParams(layoutParams);
            progressBar.setVisibility(0);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams2.gravity = 17;
            TextView textView = new TextView(context);
            textView.setText("Please Wait...");
            textView.setTextColor(Color.parseColor("#FFFFFF"));
            textView.setTextSize(20.0f);
            textView.setLayoutParams(layoutParams2);
            linearLayout.addView(progressBar);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(true);
            builder.setView(linearLayout);
            hideDialog();
            AlertDialog create = builder.create();
            this.dialog = create;
            create.show();
            if (this.dialog.getWindow() != null) {
                WindowManager.LayoutParams layoutParams3 = new WindowManager.LayoutParams();
                layoutParams3.copyFrom(this.dialog.getWindow().getAttributes());
                layoutParams3.width = -2;
                layoutParams3.height = -2;
                this.dialog.getWindow().setAttributes(layoutParams3);
            }
        }
    }
}
