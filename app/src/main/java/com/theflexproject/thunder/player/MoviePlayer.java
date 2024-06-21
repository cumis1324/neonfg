package com.theflexproject.thunder.player;



import static com.theflexproject.thunder.Constants.TMDB_BACKDROP_IMAGE_BASE_URL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Rational;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManagerProvider;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters;
import com.google.android.exoplayer2.ui.StyledPlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theflexproject.thunder.R;
import com.theflexproject.thunder.adapter.BannerRecyclerAdapter;
import com.theflexproject.thunder.adapter.FileItemAdapter;
import com.theflexproject.thunder.adapter.MoreMoviesAdapterr;
import com.theflexproject.thunder.adapter.ScaleCenterItemLayoutManager;
import com.theflexproject.thunder.database.DatabaseClient;
import com.theflexproject.thunder.fragments.CustomFileListDialogFragment;
import com.theflexproject.thunder.fragments.MovieDetailsFragment;
import com.theflexproject.thunder.model.FirebaseManager;
import com.theflexproject.thunder.model.Genre;
import com.theflexproject.thunder.model.Movie;
import com.theflexproject.thunder.model.MyMedia;
import com.theflexproject.thunder.utils.MovieQualityExtractor;
import com.theflexproject.thunder.utils.StringUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import eightbitlab.com.blurview.BlurView;

public class MoviePlayer extends AppCompatActivity implements View.OnClickListener, StyledPlayerView.ControllerVisibilityListener {

    public static final String KEY_TRACK_SELECTION_PARAMETERS = "track_selection_parameters",
            KEY_ITEM_INDEX = "item_index", KEY_POSITION = "position",
            KEY_AUTO_PLAY = "auto_play", PREFER_EXTENSION_DECODERS_EXTRA = "prefer_extension_decoders";
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 100;

    protected StyledPlayerView playerView;
    protected StyledPlayerControlView controlView;
    protected LinearLayout debugRootView;


    private DataSource.Factory dataSourceFactory;
    private MediaItem mediaItem;
    private TrackSelectionParameters trackSelectionParameters;
    private boolean startAutoPlay;
    private int startItemIndex;
    private long startPosition;
    private TextView nfgpluslog;
    private static final int REQUEST_CODE_PICTURE_IN_PICTURE = 1;

    private ImageButton buttonAspectRatio;
    private TextView playerTitle;
    private TextView playerEpsTitle;
    Intent intent;
    int uiOptions;
    View decorView;
    private String TAG = "PlayerActivity";
    private RewardedAd rewardedAd;
    FirebaseManager manager;
    private PlayerHelper playerHelper;

    private DatabaseReference databaseReference;
    TextView title, size, quality, moreMoviesTitle;
    View progressOverlay;
    Button saweria;
    BannerRecyclerAdapter moreRecomRecycler;
    MoreMoviesAdapterr moreMovieRecycler;
    RecyclerView moreRecomView, moreMovieView;
    List<Movie> moreMovies, morebyId, moreRecom;
    MoreMoviesAdapterr.OnItemClickListener moreMoviesListener;
    BannerRecyclerAdapter.OnItemClickListener moreRecomListener;
    BlurView blurView;
    ViewGroup rootView;


    int movieId;
    String movieFileName;
    ImageView logo;
    TextView namaMovie, yearText, runtime, overview;
    ImageButton play, changeSource, addToList, download;

    TableRow director, writer, genres;
    TextView directorText, writerText, genresText, ratingsText;
    ImageView dot1, ratings, backdrop;
    Movie movieDetails;
    ImageButton shareButton;


    List<Movie> movieFileList;
    List<MyMedia> moreMovieList;
    FileItemAdapter fileAdapter;
    FileItemAdapter.OnItemClickListener listenerFileItem;
    Movie largestFile, selectedFile;
    RelativeLayout relativeContainer;
    Activity mActivity;
    String mediaUri;
    ImageButton fullscreenButton;
    boolean isFullscreen = false;
    private RelativeLayout frameBackdrop;
    private RelativeLayout otherUIElements;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = MoviePlayer.this;
        setContentView(R.layout.activity_movie_player);
        intent = getIntent();
        manager = new FirebaseManager();
        String tmdbId = intent.getStringExtra("tmdbId");
        movieId = Integer.parseInt(getIntent().getStringExtra("movieId"));
        databaseReference = FirebaseDatabase.getInstance().getReference("History/"+tmdbId);
        decorView = getWindow().getDecorView();
        playerView = findViewById(R.id.player_view2);
        fullscreenButton = findViewById(R.id.fullScreen_movie);
        playerHelper = new PlayerHelper();
        mediaUri = getIntent().getStringExtra("mediaUri");
        playerHelper.initializePlayer(mActivity, playerView, mediaUri);
        String userId = manager.getCurrentUser().getUid();
        DatabaseReference userReference = databaseReference.child(userId).child("lastPosition");
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the last position from the database
                    Long lastPosition = dataSnapshot.getValue(Long.class);
                    if (lastPosition != null) {
                        // Update the startPosition with the retrieved value
                        startPosition = lastPosition;

                        // Seek the player to the last position
                        playerHelper.seekTo(startPosition);
                        String formattedPosition = formatDuration(startPosition);
                        Toast.makeText(getApplicationContext(), "Resuming to your last position " + formattedPosition, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });


        playerView.setControllerVisibilityListener(this);
        playerView.requestFocus();

        initWidgets();
        fullscreenConfig();
        //loadTitle();
        Rational aspectRatio = new Rational(playerView.getWidth(), playerView.getHeight());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            PictureInPictureParams params = new PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                    .build();
        }


    }

    public void initWidgets() {
        uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //playerTitle = findViewById(R.id.playerTitle);
        //nfgpluslog = findViewById(R.id.nfgpluslogo);
        //playerEpsTitle = findViewById(R.id.playerEpsTitle);
        WebView webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://stream.trakteer.id/running-text-default.html?rt_count=5&rt_speed=fast&rt_1_clr1=rgba%280%2C+0%2C+0%2C+1%29&rt_septype=image&rt_txtshadow=true&rt_showsuppmsg=true&creator_name=nfgplus-official&page_url=trakteer.id/nfgplusofficial&mod=3&key=trstream-hV0jDdrlk82mv3aZnzpA&hash=a6z74q7pkgn3mlqy");
        title = findViewById(R.id.title3);
        progressOverlay = findViewById(R.id.progress_overlay);
        size = findViewById(R.id.sizeTextInFileItem1);
        quality = findViewById(R.id.fakebutton);
        relativeContainer = findViewById(R.id.relativeContainer);
        moreMovieView = findViewById(R.id.recyclerEpisodes3);
        saweria = findViewById(R.id.saweria);
        namaMovie = findViewById(R.id.namaMovie);
        logo = findViewById(R.id.movieLogo);
        yearText = findViewById(R.id.year_text);
        runtime = findViewById(R.id.RuntimeText);
        overview = findViewById(R.id.overviewdesc);
        backdrop = findViewById(R.id.movieBackdrop);
        genres = findViewById(R.id.Genres);
        genresText = findViewById(R.id.GenresText);
        ratings = findViewById(R.id.ratings);
        dot1 = findViewById(R.id.dot);
        ratingsText = findViewById(R.id.ratingsText);
        play = findViewById(R.id.play);
        download = findViewById(R.id.downloadButton);
        shareButton = findViewById(R.id.shareButton);
        addToList = findViewById(R.id.addToListButton);
        changeSource = findViewById(R.id.changeSourceButton);
        otherUIElements = findViewById(R.id.other_ui);
        frameBackdrop = findViewById(R.id.frameBackdrop);
//        changeTMDB = view.findViewById(R.id.changeTMDBId);
        loadDetails();

    }

    public void exitFullscreen(){
        uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        frameBackdrop.getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.framebackdrop);
        otherUIElements.setVisibility(View.VISIBLE);
        isFullscreen = false;
    }
    public void enterFullscreen(){
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        frameBackdrop.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        otherUIElements.setVisibility(View.GONE);
        //fullscreenButton.setImageResource(R.drawable.baseline_fullscreen_exit_24);
        isFullscreen = true;
    }
    public void fullscreenConfig(){
        fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFullscreen) {
                    exitFullscreen();
                } else {
                    enterFullscreen();
                }
            }
        });
    }


    private void loadReward(){
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(MoviePlayer.this, "ca-app-pub-7142401354409440/7652952632",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.toString());
                        rewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                        Log.d(TAG, "Ad was loaded.");
                        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.
                                Log.d(TAG, "Ad was clicked.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d(TAG, "Ad dismissed fullscreen content.");
                                rewardedAd = null;
                                if (playerHelper != null) {
                                    playerHelper.setPlayWhenReady(true);
                                }
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.e(TAG, "Ad failed to show fullscreen content.");
                                rewardedAd = null;
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                Log.d(TAG, "Ad recorded an impression.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad showed fullscreen content.");
                                if (playerHelper != null) {
                                    playerHelper.setPlayWhenReady(false);
                                }
                            }
                        });
                        if (rewardedAd != null) {
                            if (playerView != null){
                                playerView.onPause();
                            }
                            rewardedAd.show(MoviePlayer.this, new OnUserEarnedRewardListener() {
                                @Override
                                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                    // Handle the reward.
                                    Log.d(TAG, "The user earned the reward.");
                                    int rewardAmount = rewardItem.getAmount();
                                    String rewardType = rewardItem.getType();

                                }
                            });
                        }
                    }

                });

    }
    private void loadTitle(){
        String titleString = intent.getStringExtra("title");
        String yearString = intent.getStringExtra("year");
        String seasonString = intent.getStringExtra("season");
        String epsnumString = intent.getStringExtra("number");
        String titleEpisode = intent.getStringExtra("episode");
        if (yearString!=null) {
            playerTitle.setText(titleString + " (" + yearString + ")");
            playerEpsTitle.setVisibility(View.GONE);
        }else {
            playerTitle.setText(titleString);
            playerEpsTitle.setText("Season " + seasonString + " Episode " + epsnumString + " : " + titleEpisode);
            playerEpsTitle.setVisibility(View.VISIBLE);
        }

        playerTitle.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Check if device orientation is landscape
        if (newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            // Show rewarded ad if loaded
            loadReward();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPictureInPictureMode(new PictureInPictureParams.Builder().build());
        }
    }
    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        }
        if (isInPictureInPictureMode) {
            // Hide unnecessary UI elements for Picture-in-Picture mode
            // Example: controlView.setVisibility(View.GONE);

            nfgpluslog.setVisibility(View.GONE);
        } else {
            // Restore UI elements when exiting Picture-in-Picture mode
            // Example: controlView.setVisibility(View.VISIBLE);
            nfgpluslog.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        playerView.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        playerView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onBackPressed() {
        if (isFullscreen){
            exitFullscreen();
        }else {
            releasePlayer();
            super.onBackPressed();

        }

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateTrackSelectorParameters();
        updateStartPosition();
        outState.putBundle(KEY_TRACK_SELECTION_PARAMETERS, trackSelectionParameters.toBundle());
        outState.putBoolean(KEY_AUTO_PLAY, startAutoPlay);
        outState.putInt(KEY_ITEM_INDEX, startItemIndex);
        outState.putLong(KEY_POSITION, startPosition);
    }

    // Activity input

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // See whether the player view wants to handle media or DPAD keys events.
        return playerView.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }


    @Override
    public void onVisibilityChanged(int visibility) {
       // playerTitle.setVisibility(visibility == View.VISIBLE ? View.VISIBLE : View.GONE);
        // playerEpsTitle.setVisibility(visibility == View.VISIBLE ? View.VISIBLE : View.GONE);
    }
    @Override
    public void onClick(View view) {

    }

    public void addToPlayed(){
        Integer tmdbId = Integer.valueOf(intent.getStringExtra("tmdbId"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        String currentDateTime = ZonedDateTime.now(java.time.ZoneId.of("GMT+07:00")).format(formatter);
        // Update the played field in your local database asynchronously
        AsyncTask.execute(() -> {
            String yearString = intent.getStringExtra("year");
            if(yearString!=null) {
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase().movieDao().updatePlayed(tmdbId, currentDateTime + " added");
            }else{
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase().episodeDao().updatePlayed(tmdbId, currentDateTime + " added");
            }
        });
    }

    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }


    protected void releasePlayer() {
        if (playerHelper != null) {
            updateTrackSelectorParameters();
            updateStartPosition();
            playerHelper.releasePlayer();
            playerHelper = null;
            playerView.setPlayer(/* player= */ null);

        }
    }

    private void updateTrackSelectorParameters() {
        if (playerView != null) {
            trackSelectionParameters = playerHelper.getTrackSelectionParameters();
        }
    }

    private void updateStartPosition() {
        if (playerView != null) {
            addToPlayed();
            startAutoPlay = playerHelper.getPlayWhenReady();
            startItemIndex = playerHelper.getCurrentMediaItemIndex();
            assert playerHelper.player != null;
            startPosition = Math.max(0, playerHelper.player.getContentPosition());
            String userId = manager.getCurrentUser().getUid();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            String currentDateTime = ZonedDateTime.now(java.time.ZoneId.of("GMT+07:00")).format(formatter);
            DatabaseReference userReference = databaseReference.child(userId);
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("lastPosition", startPosition);
            userMap.put("lastPlayed", currentDateTime);
            userReference.setValue(userMap);


        }
    }




    // DETAILS MOVIEEE //
    private void loadDetails() {
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(" ", "in thread");

                    movieFileList = DatabaseClient
                            .getInstance(mActivity)
                            .getAppDatabase()
                            .movieDao()
                            .getAllById(movieId);

                    Log.i("movieId", movieId + "");

                    movieDetails = DatabaseClient
                            .getInstance(mActivity)
                            .getAppDatabase()
                            .movieDao()
                            .byId(movieId);
                    if (movieDetails == null) {
                        movieDetails = DatabaseClient
                                .getInstance(mActivity)
                                .getAppDatabase()
                                .movieDao()
                                .getByFileName(movieFileName);
                    }

                    if (movieDetails != null) {
                        loadMoreMovies();
                        Log.i("insideLoadDetails", movieDetails.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String titleText = movieDetails.getTitle();
                                title.setText(titleText);

                                String tmdbId = String.valueOf(movieDetails.getId());
                                databaseReference = FirebaseDatabase.getInstance().getReference("History/" + tmdbId);
                                String userId = manager.getCurrentUser().getUid();
                                DatabaseReference userReference = databaseReference.child(userId).child("lastPosition");
                                DatabaseReference lastP = databaseReference.child(userId).child("lastPlayed");

                                // Listener for lastPosition
                                userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            Long lastPosition = dataSnapshot.getValue(Long.class);
                                            if (lastPosition != null) {
                                                long runtime = (long) movieDetails.getRuntime() * 60 * 1000;
                                                double progress = (double) lastPosition / runtime;
                                                int progressWidth = (int) (backdrop.getWidth() * progress);
                                                progressOverlay.getLayoutParams().width = progressWidth;
                                                progressOverlay.requestLayout();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        // Handle onCancelled event
                                    }
                                });

                                // Listener for lastPlayed
                                lastP.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            String lastPlayed = dataSnapshot.getValue(String.class);
                                            if (lastPlayed != null) {
                                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                                                String currentDateTime = ZonedDateTime.now(java.time.ZoneId.of("GMT+07:00")).format(formatter);
                                                // Update the played field in your local database asynchronously
                                                AsyncTask.execute(() -> {
                                                    DatabaseClient.getInstance(getApplicationContext()).getAppDatabase().movieDao().updatePlayed(movieDetails.getId(), lastPlayed + " added");
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        // Handle onCancelled event
                                    }
                                });

                                size.setText(movieDetails.getOriginal_language());
                                size.setVisibility(View.VISIBLE);
                                quality.setText(movieDetails.getOriginal_title());
                                quality.setVisibility(View.VISIBLE);

                                String logoLink = movieDetails.getLogo_path();
                                System.out.println("Logo Link" + logoLink);

                                if (logoLink != null && !logoLink.equals("")) {
                                    //logo.setVisibility(View.VISIBLE);
                                    namaMovie.setVisibility(View.VISIBLE);
                                    namaMovie.setText(titleText);

                                    Glide.with(MoviePlayer.this)
                                            .load(logoLink)
                                            .apply(new RequestOptions()
                                                    .fitCenter()
                                                    .override(Target.SIZE_ORIGINAL))
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .placeholder(new ColorDrawable(Color.TRANSPARENT));
                                    //.into(logo);
                                } else {
                                    if (logoLink != null && logoLink.equals("") && movieDetails.getTitle() != null) {
                                        namaMovie.setVisibility(View.VISIBLE);
                                        namaMovie.setText(titleText);
                                        logo.setVisibility(View.GONE);
                                    } else {
                                        namaMovie.setVisibility(View.VISIBLE);
                                        namaMovie.setText(movieFileName);
                                    }
                                }

                                if (movieDetails.getRuntime() > 0) {
                                    String result = StringUtils.runtimeIntegerToString(movieDetails.getRuntime());
                                    runtime.setVisibility(View.VISIBLE);
                                    runtime.setText(result);
                                }
                                if (movieDetails.getGenres() != null) {
                                    genresText.setVisibility(View.VISIBLE);
                                    ArrayList<Genre> genres = movieDetails.getGenres();
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = 0; i < genres.size(); i++) {
                                        Genre genre = genres.get(i);
                                        if (i == genres.size() - 1 && genre != null) {
                                            sb.append(genre.getName());
                                        } else if (genre != null) {
                                            sb.append(genre.getName()).append(", ");
                                        }
                                    }
                                    genresText.setText(sb.toString());
                                }
                                if (movieDetails.getVote_average() != 0) {
                                    dot1.setVisibility(View.VISIBLE);
                                    ratingsText.setVisibility(View.VISIBLE);
                                    String rating = (int) (movieDetails.getVote_average() * 10) + "%";
                                    ratingsText.setText(rating);
                                }
                                String year = movieDetails.getRelease_date();
                                if (year != null && year.length() > 1) {
                                    yearText.setVisibility(View.VISIBLE);
                                    yearText.setText(year.substring(0, year.indexOf('-')));
                                }
                                if (movieDetails.getOverview() != null) {
                                    overview.setVisibility(View.VISIBLE);
                                    overview.setText(movieDetails.getOverview());
                                }
                                if (movieDetails.getPoster_path() != null) {
                                    // Uncomment and adjust the code as necessary
                                    // Glide.with(mActivity)
                                    //        .load(TMDB_IMAGE_BASE_URL + movieDetails.getPoster_path())
                                    //        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    //        .placeholder(new ColorDrawable(Color.TRANSPARENT))
                                    //        .into(poster);
                                }
                                if (movieDetails.getBackdrop_path() != null) {
                                    Glide.with(MoviePlayer.this)
                                            .load(TMDB_BACKDROP_IMAGE_BASE_URL + movieDetails.getBackdrop_path())
                                            .apply(new RequestOptions()
                                                    .fitCenter()
                                                    .override(Target.SIZE_ORIGINAL))
                                            .placeholder(new ColorDrawable(Color.TRANSPARENT))
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .into(backdrop);
                                } else {
                                    if (movieDetails.getPoster_path() != null) {
                                        Glide.with(MoviePlayer.this)
                                                .load(TMDB_BACKDROP_IMAGE_BASE_URL + movieDetails.getPoster_path())
                                                .apply(new RequestOptions()
                                                        .fitCenter()
                                                        .override(Target.SIZE_ORIGINAL))
                                                .placeholder(new ColorDrawable(Color.TRANSPARENT))
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .into(backdrop);
                                    }
                                }

                            }
                        });
                    }

                }
            });
            thread.start();
        } catch (NullPointerException exception) {
            Log.i("Error", exception.toString());
        }
        setMyOnClickListeners();
    }
    private void  loadMoreMovies() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                morebyId = DatabaseClient
                        .getInstance(mActivity)
                        .getAppDatabase()
                        .movieDao()
                        .getmorebyid(movieId);
                moreMovies = DatabaseClient
                        .getInstance(mActivity)
                        .getAppDatabase()
                        .movieDao()
                        .getMoreMovied();
                moreRecom = DatabaseClient
                        .getInstance(mActivity)
                        .getAppDatabase()
                        .movieDao()
                        .getrecomendation();
                moreMovieList = new ArrayList<>();
                moreMovieList.addAll(morebyId);
                moreMovieList.addAll(moreMovies);
                moreMovieList.addAll(moreRecom);
                if(moreMovieList!=null && moreMovieList.size()>0) {
                    if (mActivity != null) {
                        mActivity.runOnUiThread(new Runnable() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void run() {
                                moreMovieView.setVisibility(View.VISIBLE);
                                ScaleCenterItemLayoutManager linearLayoutManager = new ScaleCenterItemLayoutManager(getApplicationContext() , LinearLayoutManager.VERTICAL , false);
                                moreMovieView.setLayoutManager(linearLayoutManager);
                                moreMovieView.setHasFixedSize(true);
                                moreMovieRecycler = new MoreMoviesAdapterr(mActivity, (List<MyMedia>)(List<?>) moreMovieList, moreMoviesListener);
                                moreMovieView.setAdapter(moreMovieRecycler);
                                moreMovieRecycler.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }});
        thread.start();

    }
    private void setMyOnClickListeners() {
        saweria.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://trakteer.id/nfgplusofficial/tip"))));

        Thread thread = new Thread(() -> {
            largestFile = DatabaseClient.getInstance(mActivity)
                    .getAppDatabase()
                    .movieDao()
                    .byIdLargest(movieId);
        });
        thread.start();

        SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        boolean savedEXT = sharedPreferences.getBoolean("EXTERNAL_SETTING", false);

        if (savedEXT) {
            // External Player
            play.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(largestFile.getUrlString()));
                intent.setDataAndType(Uri.parse(largestFile.getUrlString()), "video/*");
                startActivity(intent);
                loadReward();
            });
        } else {
            // Play video
            play.setOnClickListener(v -> {
                try {
                    CustomFileListDialogFragment dialog = new CustomFileListDialogFragment(mActivity, changeSource, (List<MyMedia>) (List<?>) movieFileList);

                    getSupportFragmentManager()
                            .beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .add(android.R.id.content, dialog)
                            .addToBackStack(null)
                            .commit();

                    dialog.mOnInputListener = selection -> {
                        selectedFile = movieFileList.get(selection);
                        String huntu = MovieQualityExtractor.extractQualtiy(selectedFile.getFileName());
                        System.out.println("selected file" + selectedFile.getFileName());
                        Intent in = new Intent(mActivity, PlayerActivity.class);
                        in.putExtra("url", selectedFile.getUrlString());
                        in.putExtra("title", selectedFile.getTitle());
                        String tmdbId = String.valueOf(selectedFile.getId());
                        in.putExtra("tmdbId", tmdbId);
                        String inYear = selectedFile.getRelease_date();
                        in.putExtra("year", inYear.substring(0, inYear.indexOf('-')));
                        startActivity(in);
                        Toast.makeText(mActivity, "Playing " + movieDetails.getTitle() + " " + huntu, Toast.LENGTH_LONG).show();
                        Toast.makeText(mActivity, selectedFile.getTitle() + huntu + " Selected", Toast.LENGTH_LONG).show();
                    };

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Start download
        download.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT < 32) {
                // Check if the app has the WRITE_EXTERNAL_STORAGE permission
                if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Request the permission if it is not granted
                    ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION);
                } else {
                    // Permission is already granted, proceed with the download
                    startDownload();
                }
            } else {
                // Permission is already granted, proceed with the download
                startDownload();
            }
        });

        addToList.setOnClickListener(v -> {
            if (movieDetails.getAddToList() != 1) {
                new Thread(() -> {
                    DatabaseClient.getInstance(mActivity).getAppDatabase().movieDao().updateAddToList(movieDetails.getId());
                }).start();
                Toast.makeText(mActivity, "Added To List", Toast.LENGTH_LONG).show();
            } else {
                new Thread(() -> {
                    DatabaseClient.getInstance(mActivity).getAppDatabase().movieDao().updateRemoveFromList(movieId);
                }).start();
                Toast.makeText(mActivity, "Removed From List", Toast.LENGTH_LONG).show();
            }
        });

        changeSource.setOnClickListener(v -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            CustomFileListDialogFragment dialog = new CustomFileListDialogFragment(mActivity, changeSource, (List<MyMedia>) (List<?>) movieFileList);

            getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .add(android.R.id.content, dialog)
                    .addToBackStack(null)
                    .commit();

            dialog.mOnInputListener = selection -> {
                selectedFile = movieFileList.get(selection);
                String huntu = MovieQualityExtractor.extractQualtiy(selectedFile.getFileName());
                System.out.println("selected file" + selectedFile.getFileName());
                mediaUri = selectedFile.getUrlString();
                playerHelper.releasePlayer();
                playerHelper.initializePlayer(mActivity, playerView, mediaUri);
                playerHelper.seekTo(startPosition);
                Toast.makeText(mActivity, selectedFile.getTitle() + huntu + " Selected", Toast.LENGTH_LONG).show();
            };
        });

        shareButton.setOnClickListener(v -> {
            String itemId = String.valueOf(movieDetails.getId()); // Replace with the actual item ID
            generateAndShareDynamicLink(itemId);
        });

        moreMoviesListener = (view, position) -> {
            Movie more = (Movie) moreMovieList.get(position);
            MovieDetailsFragment movieDetailsFragment = new MovieDetailsFragment(more.getId());
            // Assuming you want to load the fragment here

        };
    }

    private void startDownload() {
        CustomFileListDialogFragment downdialog = new CustomFileListDialogFragment(mActivity, changeSource, (List<MyMedia>) (List<?>) movieFileList);

        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(android.R.id.content, downdialog)
                .addToBackStack(null)
                .commit();

        downdialog.mOnInputListener = selection -> {
            selectedFile = movieFileList.get(selection);
            String huntu = MovieQualityExtractor.extractQualtiy(selectedFile.getFileName());
            System.out.println("selected file" + selectedFile.getFileName());

            String customFolderPath = "/nfgplus/movies/";
            DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(selectedFile.getUrlString());
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, customFolderPath + selectedFile.getFileName());
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setDescription("Downloading " + selectedFile.getTitle() + " " + huntu);
            long reference = manager.enqueue(request);
            Toast.makeText(mActivity, "Download Started", Toast.LENGTH_LONG).show();
        };
    }

    // Handle the permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with the download
                startDownload();
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Permission denied to write to external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void generateAndShareDynamicLink(String itemId) {
        // Set up the dynamic link components
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://nfgplus.page.link/share/" + itemId))
                .setDomainUriPrefix("https://nfgplus.page.link/share/")
                .buildDynamicLink();

        // Generate the short dynamic link
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(dynamicLink.getUri())
                .buildShortDynamicLink()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Short link created successfully
                        Uri shortLink = task.getResult().getShortLink();
                        // Now you can use the 'shortLink' to share with others
                        shareDynamicLink(shortLink.toString());
                    } else {
                        // Handle the error
                        Exception e = task.getException();
                        // Log or display an error message
                        // For now, you can still proceed with the long link if short link creation fails
                        shareDynamicLink(dynamicLink.getUri().toString());
                    }
                });
    }

    // Method to share the dynamic link
    private void shareDynamicLink(String dynamicLink) {
        // Create a share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        // Include movie details in the share text
        String shareText =
                movieDetails.getTitle()
                        + "\n \n Watch this movie in your app "
                        + "\n \n Overview: " + movieDetails.getOverview()
                        + "\n \n" + dynamicLink;

        // Set the share text as the data
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        // Start the share activity
        startActivity(Intent.createChooser(shareIntent, "Share This Movie"));
    }
}