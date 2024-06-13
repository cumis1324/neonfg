package com.theflexproject.thunder.player;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.theflexproject.thunder.R;

public class VideoPlayer extends Fragment implements View.OnClickListener, StyledPlayerView.ControllerVisibilityListener {

    private static final String KEY_TRACK_SELECTION_PARAMETERS = "track_selection_parameters";
    private static final String KEY_ITEM_INDEX = "item_index";
    private static final String KEY_POSITION = "position";
    private static final String KEY_AUTO_PLAY = "auto_play";

    private StyledPlayerView playerView;
    private @Nullable ExoPlayer player;
    private MediaItem mediaItem;

    private TextView playerTitle;
    private TextView playerEpsTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.video_player, container, false);
        playerView = rootView.findViewById(R.id.player_view);
        playerTitle = rootView.findViewById(R.id.playerTitle);
        playerEpsTitle = rootView.findViewById(R.id.playerEpsTitle);
        // Initialize other views and variables...

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize player and other components here...
        initializePlayer();
        loadTitle();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Initialize player when the fragment starts
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Release player when the fragment stops
        releasePlayer();
    }

    @Override
    public void onClick(View view) {
        // Handle click events here...
    }

    @Override
    public void onVisibilityChanged(int visibility) {
        // Handle player controller visibility changes...
        playerTitle.setVisibility(visibility == View.VISIBLE ? View.VISIBLE : View.GONE);
        playerEpsTitle.setVisibility(visibility == View.VISIBLE ? View.VISIBLE : View.GONE);
    }

    private void initializePlayer() {
        // Initialize ExoPlayer and set up media playback
        player = new ExoPlayer.Builder(requireContext())
                .build();
        player.setAudioAttributes(AudioAttributes.DEFAULT, true);
        player.setPlayWhenReady(true);
        playerView.setPlayer(player);

        // Set up media item
        Bundle bundle = new Bundle();
        assert getArguments() != null;
        String urlString = getArguments().getString("url");
        Uri uri = Uri.parse(urlString);
        mediaItem = MediaItem.fromUri(uri);
        assert player != null;
        player.setMediaItem(mediaItem);

        // Prepare the player
        player.prepare();
    }

    private void releasePlayer() {
        // Release ExoPlayer
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void loadTitle() {
        // Load title from arguments and set it to the views
        String titleString = getArguments().getString("title");
        String yearString = getArguments().getString("year");
        String seasonString = getArguments().getString("season");
        String epsnumString = getArguments().getString("number");
        String titleEpisode = getArguments().getString("episode");

        if (yearString != null) {
            playerTitle.setText(titleString + " (" + yearString + ")");
            playerEpsTitle.setVisibility(View.GONE);
        } else {
            playerTitle.setText(titleString);
            playerEpsTitle.setText("Season " + seasonString + " Episode " + epsnumString + " : " + titleEpisode);
            playerEpsTitle.setVisibility(View.VISIBLE);
        }

        playerTitle.setVisibility(View.VISIBLE);
    }
}
