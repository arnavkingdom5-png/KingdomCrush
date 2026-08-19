package com.kingdom.crush;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Intent;
import android.net.Uri;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends Activity {
    private WebView webView;
    private FrameLayout root;
    private AdView banner;
    private WebAppInterface bridge;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManagerFlags.FLAG_FULLSCREEN, WindowManagerFlags.FLAG_FULLSCREEN);
        MobileAds.initialize(this, status -> {});
        setContentView(buildUi());
        banner.loadAd(new AdRequest.Builder().build());
        configureWebView();
        webView.loadUrl("file:///android_asset/c.html");
    }

    private View buildUi() {
        root = new FrameLayout(this);
        webView = new WebView(this);
        banner = new AdView(this);
        banner.setAdSize(AdSize.BANNER);
        banner.setAdUnitId("ca-app-pub-2082766092953444/6472058478");
        FrameLayout.LayoutParams wp = new FrameLayout.LayoutParams(-1, -1);
        root.addView(webView, wp);
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(-1, -2);
        bp.gravity = android.view.Gravity.BOTTOM;
        root.addView(banner, bp);
        bridge = new WebAppInterface(this, banner);
        return root;
    }

    private void configureWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(bridge, "AndroidBridge");
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setBackgroundColor(android.graphics.Color.rgb(26,15,46));
    }

    public void evaluateJs(String js) { runOnUiThread(() -> webView.evaluateJavascript(js, null)); }

    public void shareAchievement(String base64, String title, String text) {
        // Keep sharing functional without requiring a FileProvider dependency.
        try {
            byte[] data = android.util.Base64.decode(base64.replaceFirst("^data:image/[^;]+;base64,", ""), android.util.Base64.DEFAULT);
            java.io.File dir = new java.io.File(getCacheDir(), "shared"); dir.mkdirs();
            java.io.File f = new java.io.File(dir, "kingdom-crush-share.png");
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(f)) { out.write(data); }
            Uri uri = androidx.core.content.FileProvider.getUriForFile(this, getPackageName()+".fileprovider", f);
            Intent send = new Intent(Intent.ACTION_SEND); send.setType("image/png"); send.putExtra(Intent.EXTRA_STREAM, uri); send.putExtra(Intent.EXTRA_TEXT, text); send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(send, title == null ? "Share Achievement" : title));
        } catch (Exception e) { }
    }

    @Override public void onBackPressed() {
        if (android.os.Build.VERSION.SDK_INT >= 19) {
            webView.evaluateJavascript("window.onAndroidBack ? window.onAndroidBack() : false", value -> {
                if ("false".equals(value) || value == null) finish();
            });
        } else {
            finish();
        }
    }

    private static class WindowManagerFlags { static final int FLAG_FULLSCREEN = 1024; }
  }
  
