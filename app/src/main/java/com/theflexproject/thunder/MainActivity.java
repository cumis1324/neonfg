package com.theflexproject.thunder;

import static com.theflexproject.thunder.utils.SendPostRequest.postRequestGDIndex;
import static com.theflexproject.thunder.utils.SendPostRequest.postRequestGoIndex;
import static com.theflexproject.thunder.utils.SendPostRequest.postRequestMapleIndex;
import static com.theflexproject.thunder.utils.SendPostRequest.postRequestSimpleProgramIndex;
import static com.theflexproject.thunder.utils.UpdateUtils.checkForUpdates;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Index;
import androidx.room.Room;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MediaAspectRatio;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.appupdate.AppUpdateInfo;

import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.theflexproject.thunder.database.AppDatabase;
import com.theflexproject.thunder.database.DatabaseClient;
import com.theflexproject.thunder.fragments.AddNewIndexFragment;
import com.theflexproject.thunder.fragments.HomeFragment;
import com.theflexproject.thunder.fragments.HomeNewFragment;
import com.theflexproject.thunder.fragments.LibraryFragment;
import com.theflexproject.thunder.fragments.MovieDetailsFragment;
import com.theflexproject.thunder.fragments.SearchFragment;
import com.theflexproject.thunder.fragments.SettingsFragment;
import com.theflexproject.thunder.model.FirebaseManager;
import com.theflexproject.thunder.model.IndexLink;
import com.theflexproject.thunder.MyApplication;
import com.theflexproject.thunder.utils.RefreshWorker;
import com.xcode.onboarding.MaterialOnBoarding;
import com.xcode.onboarding.OnBoardingPage;
import com.xcode.onboarding.OnFinishLastPage;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;


public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    HomeNewFragment homeFragment = new HomeNewFragment();
    MovieDetailsFragment movieDetailsFragment = new MovieDetailsFragment();
    SearchFragment searchFragment = new SearchFragment();
    LibraryFragment libraryFragment = new LibraryFragment();
    SettingsFragment settingsFragment = new SettingsFragment();
    AddNewIndexFragment scanFragment = new AddNewIndexFragment();

    BlurView blurView;
    ViewGroup rootView;
    View decorView;
    FirebaseManager firebaseManager;

    public static Context context;
    private static final String LOG_TAG = "AppOpenAdSample";
    private AppOpenAd appOpenAd;
    private HomeFragment home;
    private long loadTime = 0;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final int UPDATE_REQUEST_CODE = 123;
    Button scanButton;
    Button seriesButton;
    ProgressBar loadingScan;
    FrameLayout scanContainer;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        FirebaseApp.initializeApp(this);
        firebaseManager = new FirebaseManager();
        currentUser = firebaseManager.getCurrentUser();
        loadAd();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        if (appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)) {
            showAdIfAvailable();
        } else {
            Log.d(LOG_TAG, "Will not show ad. Ad is either not loaded or it's been less than 4 hours since last load.");
            loadAd();
        }
        // Check if the user is signed in
        if (currentUser != null) {
            checkForAppUpdate();
            Intent intent = getIntent();
            Uri data = intent.getData();
            // User is signed in
            currentUser.getUid();
            currentUser.getEmail();
            currentUser.getIdToken(true).toString();
            setContentView(R.layout.activity_main);
            initWidgets();
            setUpBottomNavigationView();
            getSupportFragmentManager().beginTransaction().replace(R.id.container , homeFragment).commit();
            AppDatabase db = Room.databaseBuilder(getApplicationContext() ,
                            AppDatabase.class , "MyToDos")
                    .build();
            scanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AddNewIndexFragment nextFrag= new AddNewIndexFragment();
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.container, nextFrag)
                            .addToBackStack(null)
                            .commit();
                }
            });
            //refresh index if set
            SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
            boolean savedREF = sharedPreferences.getBoolean("REFRESH_SETTING", false);
            int savedTime = sharedPreferences.getInt("REFRESH_TIME", 0);
            if(savedREF){
                scheduleWork(savedTime,0);
            }



        } else {
            startActivity(new Intent(MainActivity.this, SignInActivity.class));

        }

    }


    private void handleDynamicLinkData(Bundle arguments) {
        if (arguments != null) {
            Uri dynamicLinkData = arguments.getParcelable("dynamicLinkData");
            if (dynamicLinkData != null) {
                String itemId = dynamicLinkData.getQueryParameter("itemId");
                if (itemId != null) {
                    navigateToItem(itemId);
                }
            }
        }
    }

    private void navigateToItem(String itemId) {
        // Use the itemId to navigate to the appropriate screen or perform other actions
        // For example, you can start a new Fragment or Activity
        Bundle args = new Bundle();
        args.putString("itemId", itemId);

        movieDetailsFragment.setArguments(args);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, movieDetailsFragment)
                .commit();
    }
    private void loadAd() {
        MyApplication myApplication = (MyApplication) getApplication();
        myApplication.loadAd();
    }


    private void showAdIfAvailable() {
        if (appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)) {
            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            appOpenAd = null;
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            // Handle the error
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            // Ad showed successfully.
                        }
                    };

            appOpenAd.show(this);
        } else {
            Log.d(LOG_TAG, "Can not show ad. Ad is not available.");
            loadAd();
        }
    }

    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long currentTime = new Date().getTime();
        return (currentTime - loadTime) < (numHours * 3600000); // Convert hours to milliseconds
    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStackImmediate();
        else super.onBackPressed();
    }

    private void initWidgets() {
        blurView = findViewById(R.id.blurView);
        decorView = getWindow().getDecorView();
        rootView = decorView.findViewById(android.R.id.content);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        scanContainer = findViewById(R.id.scanContainer);
        loadingScan = findViewById(R.id.loadingScan);
        scanButton = findViewById(R.id.floating_scan);
        seriesButton = findViewById(R.id.scanSeries);
        blurBottom();
    }

    private void setUpBottomNavigationView() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId()==R.id.homeFragment){
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.from_right,R.anim.to_left,R.anim.from_left,R.anim.to_right)
                        .replace(R.id.container , homeFragment)
                        .commit();
                return true;
            }else if(item.getItemId()==R.id.searchFragment){
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.from_right,R.anim.to_left,R.anim.from_left,R.anim.to_right)
                        .replace(R.id.container , searchFragment)
                        .commit();
                return true;
            }else if(item.getItemId()==R.id.libraryFragment){
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.from_right,R.anim.to_left,R.anim.from_left,R.anim.to_right)
                        .replace(R.id.container , libraryFragment)
                        .commit();
                return true;
            }else if(item.getItemId()==R.id.settingsFragment){
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.from_right,R.anim.to_left,R.anim.from_left,R.anim.to_right)
                        .replace(R.id.container , settingsFragment)
                        .commit();
                return true;
            }
            return false;
        });

    }

    private void blurBottom() {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS , WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        final float radius = 12f;
        final Drawable windowBackground = getWindow().getDecorView().getBackground();

        blurView.setupWith(rootView , new RenderScriptBlur(this))
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius);
        blurView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        blurView.setClipToOutline(true);


    }
    private void checkForAppUpdate() {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);

        // Returns an intent object that you use to check for an update.
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                // Request the update.
                startUpdateFlow(appUpdateManager, appUpdateInfo);
            }
        });
    }

    private void startUpdateFlow(AppUpdateManager appUpdateManager, AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    this,
                    UPDATE_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                // If the update is cancelled or fails, you may want to retry
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scheduleWork(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        long nowMillis = calendar.getTimeInMillis();

        if(calendar.get(Calendar.HOUR_OF_DAY) > hour ||
                (calendar.get(Calendar.HOUR_OF_DAY) == hour && calendar.get(Calendar.MINUTE)+1 >= minute)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,minute);

        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        long diff = calendar.getTimeInMillis() - nowMillis;

        WorkManager mWorkManager = WorkManager.getInstance(context);
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        mWorkManager.cancelAllWork();
        OneTimeWorkRequest mRequest = new OneTimeWorkRequest.Builder(RefreshWorker.class)
                .setConstraints(constraints)
                .setInitialDelay(diff,TimeUnit.MILLISECONDS)
                .build();
        mWorkManager.enqueue(mRequest);

    }

}





