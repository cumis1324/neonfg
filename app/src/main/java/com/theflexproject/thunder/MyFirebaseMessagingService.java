package com.theflexproject.thunder;

import android.content.Intent;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.theflexproject.thunder.fragments.MovieDetailsFragment;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            // Extract data from the FCM message
            String itemId = remoteMessage.getData().get("itemId");

            // Check if itemId is not null or empty
            if (itemId != null && !itemId.isEmpty()) {
                // Use the itemId to inflate MovieDetailsFragment
                openMovieDetailsFragment(itemId);
            }
        }
    }

    private void openMovieDetailsFragment(String itemId) {
        // Create a Bundle to pass data to the fragment
        Bundle bundle = new Bundle();
        bundle.putString("itemId", itemId);

        // Create an instance of your MovieDetailsFragment
        MovieDetailsFragment movieDetailsFragment = new MovieDetailsFragment();
        movieDetailsFragment.setArguments(bundle);

        // Broadcast the message to the activity or fragment
        Intent intent = new Intent("MovieDetailsFragment");
        intent.putExtra("itemId", itemId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
