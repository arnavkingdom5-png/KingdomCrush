package com.kingdom.crush;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);

        if (webView != null) {
            WebSettings s = webView.getSettings();
            s.setJavaScriptEnabled(true);
            s.setDomStorageEnabled(true);
            s.setAllowFileAccess(true);
            s.setAllowContentAccess(true);
            s.setAllowFileAccessFromFileURLs(true);
            s.setAllowUniversalAccessFromFileURLs(true);
            s.setDatabaseEnabled(true);

            // Hardware Acceleration for HTML5 Games
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

            webView.setWebViewClient(new WebViewClient());
            webView.setWebChromeClient(new WebChromeClient());

            webView.loadUrl("file:///android_asset/c.html");
        }
    }

    public void evaluateJs(String js) {
        runOnUiThread(() -> {
            if (webView != null) {
                webView.evaluateJavascript(js, null);
            }
        });
    }

    public void shareAchievement(String base64, String title, String text) {
        try {
            byte[] data = android.util.Base64.decode(base64.replaceFirst("^data:image/[^;]+;base64,", ""), android.util.Base64.DEFAULT);
            java.io.File dir = new java.io.File(getCacheDir(), "shared");
            dir.mkdirs();
            java.io.File f = new java.io.File(dir, "kingdom-crush-share.png");
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(f)) {
                out.write(data);
            }
            Uri uri = androidx.core.content.FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", f);
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("image/png");
            send.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(send, title != null ? title : "Share Achievement"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
                                 }
                                              
