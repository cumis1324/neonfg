package com.theflexproject.thunder;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.work.WorkerParameters;

import com.theflexproject.thunder.database.DatabaseClient;
import com.theflexproject.thunder.model.IndexLink;
import com.theflexproject.thunder.utils.IndexUtils;

import java.util.List;

public class RefreshJobService extends JobIntentService {

    private static final String TAG = "RefreshJobService";

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, RefreshJobService.class, 123, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Context ctxt = getApplicationContext();

        if (isNetworkAvailable()) {
            List<IndexLink> indexLinkList = DatabaseClient.getInstance(ctxt).getAppDatabase().indexLinksDao().getAllEnabled();
            for (IndexLink indexLink : indexLinkList) {
                IndexUtils.refreshIndex(ctxt, indexLink);
            }
        } else {
            Log.d(TAG, "Network not available");
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

