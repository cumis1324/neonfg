package com.theflexproject.thunder.model;

import android.net.Uri;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseManager {
    private static final String USERS_NODE = "Users";
    private static final String PROFILE_IMAGES_NODE = "profile_images";

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    public FirebaseManager() {
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference(USERS_NODE);
        storageReference = FirebaseStorage.getInstance().getReference(PROFILE_IMAGES_NODE);
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public DatabaseReference getUserReference(String userId) {
        return databaseReference.child(userId);
    }

    public StorageReference getProfileImageReference(String filename) {
        return storageReference.child(filename);
    }
}

