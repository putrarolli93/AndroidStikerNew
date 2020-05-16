package com.icaali.StickerIslami.Manager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.icaali.StickerIslami.Application;

public class AdManager{

    public static String PUBLISHER_ID;
    public static String ADMOB_APP_ID;
    public static String ADMOB_BANNER_ID;
    public static String ADMOB_NATIVE_ID;
    public static Integer ADMOB_NATIVE_LINES = 6;
    public static String ADMOB_NATIVE_ENABLED;
    public static String ADMOB_INTERSTITIAL_ID;
    public static String ADMOB_REWARD_ID;

    public static Integer ADMOB_INTERSTITIAL_SHOW_CLICKS = 0;
    public static Integer ADMOB_INTERSTITIAL_COUNT_CLICKS = 0;
    public static boolean INTERSTITIAL_AD_IS_LOADING = false;

    private static InterstitialAd interstitialAd;
    private static AdRequest bannerAd;
    private static boolean interstitialAdIsRunning = false;

//    public AdManager(){
//        MobileAds.initialize(Application.getAppContext(), admobAppId);
//        sInterstitialAd = newInterstitialAd();
//        loadInterstitial();
//        getBannerAdRequest();
//        adIsRunning = true;
//    }

    public static InterstitialAd getInterstitialAd(){
        if (!INTERSTITIAL_AD_IS_LOADING){
            loadInterstitialAd();
        }
        return interstitialAd;
    }

    public static AdRequest getBannerAd(){
        if (null == bannerAd) {
            bannerAd = adRequest();
            return bannerAd;
        }
        return bannerAd;
    }

    public static void loadInterstitialAd() {
        if (!interstitialAdIsRunning) {
            interstitialAdIsRunning = true;
            interstitialAd = new InterstitialAd(Application.getAppContext());
            interstitialAd.setAdUnitId(ADMOB_INTERSTITIAL_ID);
        }

        INTERSTITIAL_AD_IS_LOADING = true;
        interstitialAd.loadAd(adRequest());
    }

    private static AdRequest adRequest() {
        return new AdRequest.Builder().build();
    }
}