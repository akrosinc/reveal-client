package org.smartregister.reveal.task;

import android.content.Context;
import android.os.AsyncTask;

import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.OfflineMapHelper;

import timber.log.Timber;

public class FileHttpServerTask extends AsyncTask<Void, Void, Boolean>
{
    private final Context context;
    private final OnServerStartedListener listener;

    public interface OnServerStartedListener {
        void onServerStarted();
    }

    public FileHttpServerTask(Context context, OnServerStartedListener listener) {
        // Always use application context in AsyncTasks to avoid leaking Activities
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            OfflineMapHelper.initializeFileHTTPServer(context, Constants.DG_ID_PLACEHOLDER);
            return true;
        } catch (Exception e) {
            Timber.e(e, "FileHttpServerTask failed to start server");

            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success && listener != null) {
            listener.onServerStarted();
        }
    }
}
//package org.smartregister.reveal.task;
//
//import android.content.Context;
//import android.os.AsyncTask;
//
//import org.smartregister.reveal.util.Constants;
//import org.smartregister.reveal.util.OfflineMapHelper;
//
///**
// * Created by Richard Kareko on 2/4/20.
// */
//
//public class FileHttpServerTask extends AsyncTask<Void, Void, Void> {
//    private Context context;
//
//    public FileHttpServerTask(Context context) {
//        this.context = context;
//    }
//
//    @Override
//    protected Void doInBackground(Void... params) {
//        OfflineMapHelper.initializeFileHTTPServer(context, Constants.DG_ID_PLACEHOLDER);
//        return null;
//    }
//}
