package com.theflexproject.thunder.player;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManagerProvider;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters;
import com.google.android.exoplayer2.ui.StyledPlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.firebase.database.DatabaseReference;
import com.theflexproject.thunder.model.FirebaseManager;

// PlayerHelper.java
public class PlayerHelper {

    public static final String KEY_TRACK_SELECTION_PARAMETERS = "track_selection_parameters";
    public static final String KEY_ITEM_INDEX = "item_index";
    public static final String KEY_POSITION = "position";
    public static final String KEY_AUTO_PLAY = "auto_play";

    public static final String PREFER_EXTENSION_DECODERS_EXTRA = "prefer_extension_decoders";

    protected StyledPlayerView playerView;
    protected StyledPlayerControlView controlView;
    protected LinearLayout debugRootView;
    protected @Nullable ExoPlayer player;

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
    private DatabaseReference databaseReference;

    public void initializePlayer(Context context, StyledPlayerView playerView, String videoUrl) {
        this.playerView = playerView;
        player = new ExoPlayer.Builder(context).build();
        playerView.setPlayer(player);
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(Uri.parse(videoUrl))
                .build();
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }
    private MediaSource.Factory createMediaSourceFactory() {
        DefaultDrmSessionManagerProvider drmSessionManagerProvider =
                new DefaultDrmSessionManagerProvider();
        drmSessionManagerProvider.setDrmHttpDataSourceFactory(
                DemoUtil.getHttpDataSourceFactory(/* context= */ playerView.getContext()));
        return new DefaultMediaSourceFactory(/* context= */ playerView.getContext())
                .setDataSourceFactory(dataSourceFactory)
                .setDrmSessionManagerProvider(drmSessionManagerProvider);
    }
    private void setRenderersFactory(
            ExoPlayer.Builder playerBuilder, boolean preferExtensionDecoders) {
        RenderersFactory renderersFactory =
                DemoUtil.buildRenderersFactory(/* context= */ playerView.getContext(), preferExtensionDecoders);
        playerBuilder.setRenderersFactory(renderersFactory);
    }

    public void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    public long getCurrentPosition() {
        return player != null ? player.getCurrentPosition() : 0;
    }

    public void seekTo(long position) {
        if (player != null) {
            player.seekTo(position);
        }
    }

    public boolean getPlayWhenReady() {
        return false;
    }

    public int getCurrentMediaItemIndex() {
        return 0;
    }

    public int getContentPosition() {
        return 0;
    }

    public void setPlayWhenReady(boolean b) {
    }

    public void seekToDefaultPosition() {
    }

    public void prepare() {
    }


    public TrackSelectionParameters getTrackSelectionParameters() {
        return null;
    }
}
