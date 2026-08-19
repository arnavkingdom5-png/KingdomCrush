package com.kingdom.crush;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class WebAppInterface {
    private static final String TEST_BANNER = "ca-app-pub-3940256099942544/6300978111";
    private static final String TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712";
    private static final String TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917";
    private static final String PROD_BANNER = "ca-app-pub-2082766092953444/6472058478";
    private static final String PROD_INTERSTITIAL = "ca-app-pub-2082766092953444/3113754624";
    private static final String PROD_REWARDED = "ca-app-pub-2082766092953444/2870212043";
    private static final boolean USE_TEST_ADS = false;

    private final MainActivity activity;
    private final AdView banner;
    private InterstitialAd interstitial;
    private RewardedAd rewarded;
    private String pendingRewardType;

    public WebAppInterface(MainActivity activity, AdView banner) {
        this.activity = activity;
        this.banner = banner;
        loadInterstitial();
        loadRewarded();
    }

    private String bannerId() { return USE_TEST_ADS ? TEST_BANNER : PROD_BANNER; }
    private String interstitialId() { return USE_TEST_ADS ? TEST_INTERSTITIAL : PROD_INTERSTITIAL; }
    private String rewardedId() { return USE_TEST_ADS ? TEST_REWARDED : PROD_REWARDED; }

    @JavascriptInterface public void showBanner() { activity.runOnUiThread(() -> { banner.setVisibility(android.view.View.VISIBLE); }); }
    @JavascriptInterface public void hideBanner() { activity.runOnUiThread(() -> { banner.setVisibility(android.view.View.GONE); }); }

    @JavascriptInterface public void showInterstitial() {
        activity.runOnUiThread(() -> {
            if (interstitial != null) {
                InterstitialAd ad = interstitial;
                interstitial = null;
                ad.show(activity);
                loadInterstitial();
            } else loadInterstitial();
        });
    }

    @JavascriptInterface public void showRewarded(String type) {
        activity.runOnUiThread(() -> {
            pendingRewardType = type == null ? "" : type;
            if (rewarded != null) {
                RewardedAd ad = rewarded;
                rewarded = null;
                ad.show(activity, rewardItem -> {
                    String t = pendingRewardType == null ? "" : pendingRewardType;
                    activity.runOnUiThread(() -> activity.evaluateJs("window.onRewardedAdComplete(" + jsString(t) + ");"));
                });
                loadRewarded();
            } else {
                Toast.makeText(activity, "Rewarded ad is loading. Try again.", Toast.LENGTH_SHORT).show();
                loadRewarded();
            }
        });
    }

    @JavascriptInterface public void openExternalUrl(String url) {
        try { activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception e) { Toast.makeText(activity, "Unable to open link", Toast.LENGTH_SHORT).show(); }
    }

    @JavascriptInterface public void shareAchievement(String base64Png, String title, String text) {
        activity.shareAchievement(base64Png, title, text);
    }

    private void loadInterstitial() {
        AdRequest req = new AdRequest.Builder().build();
        InterstitialAd.load(activity, interstitialId(), req, new InterstitialAdLoadCallback() {
            @Override public void onAdLoaded(InterstitialAd ad) { interstitial = ad; }
            @Override public void onAdFailedToLoad(LoadAdError error) { interstitial = null; }
        });
    }

    private void loadRewarded() {
        RewardedAd.load(activity, rewardedId(), new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override public void onAdLoaded(RewardedAd ad) { rewarded = ad; }
            @Override public void onAdFailedToLoad(LoadAdError error) { rewarded = null; }
        });
    }

    private static String jsString(String s) {
        return "'" + s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n") + "'";
    }
  }
  
