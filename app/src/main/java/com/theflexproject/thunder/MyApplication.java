package com.theflexproject.thunder;

import android.app.Application;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.inappmessaging.FirebaseInAppMessaging;
import com.google.firebase.inappmessaging.FirebaseInAppMessagingClickListener;
import com.google.firebase.inappmessaging.FirebaseInAppMessagingImpressionListener;
import com.google.firebase.inappmessaging.model.Action;
import com.google.firebase.inappmessaging.model.InAppMessage;

import java.util.Date;

public class MyApplication extends Application {

    private static AppOpenAd appOpenAd = null;
    private static long loadTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this);
        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Initialize Firebase Analytics
        FirebaseAnalytics.getInstance(this);

        // Enable In-App Messaging
        FirebaseInAppMessaging.getInstance().setAutomaticDataCollectionEnabled(true);

        // Add click listener for in-app messages
        FirebaseInAppMessaging.getInstance().addClickListener(new FirebaseInAppMessagingClickListener() {
            @Override
            public void messageClicked(InAppMessage inAppMessage, Action action) {
                // Handle in-app message click
                String actionUrl = action.getActionUrl(); // Get the URL associated with the click
                // Implement your logic to handle the click action (e.g., open a specific activity or URL)
            }
        });

        // Add impression listener for in-app messages
        FirebaseInAppMessaging.getInstance().addImpressionListener(new FirebaseInAppMessagingImpressionListener() {
            @Override
            public void impressionDetected(InAppMessage inAppMessage) {
                // Handle in-app message impression
                // Implement your logic to handle the impression event (e.g., log analytics)
            }
        });

        if (shouldLoadAd()) {
            loadAd();
        }
    }

    public static AppOpenAd getAppOpenAd() {
        return appOpenAd;
    }

    public static void setAppOpenAd(AppOpenAd ad) {
        appOpenAd = ad;
    }

    private boolean shouldLoadAd() {
        long currentTime = new Date().getTime();
        return (currentTime - loadTime) > 3600000; // 1 hour
    }

    public void loadAd() {
        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(
                this,
                "ca-app-pub-5906976337228746/8963496548",
                request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(AppOpenAd ad) {
                        appOpenAd = ad;
                        loadTime = new Date().getTime();
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        // Handle the error
                    }
                });
    }
}
