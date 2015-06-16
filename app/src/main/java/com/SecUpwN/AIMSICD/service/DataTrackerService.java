package com.SecUpwN.AIMSICD.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.SecUpwN.AIMSICD.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Marvin Arnold on 15/06/15.
 */
public class DataTrackerService extends Service {
    private static final String TAG = "DataTrackerService";
    private final IBinder mBinder = new LocalBinder();
    private boolean hasScheduledUploader;
    private boolean isInitialized;
    private ScheduledExecutorService mScheduler;
    private Runnable mUploaderRunnable;
    private ScheduledFuture mUploader;
    private static final int UPLOAD_FREQUENCY_VALUE = 10;
    private static final TimeUnit UPLOAD_FREQUENCY_UNIT = TimeUnit.SECONDS;

    public class LocalBinder extends Binder {
        public DataTrackerService getService() {
            return DataTrackerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void initUploader() {
        if(isInitialized) return;

        mScheduler = Executors.newScheduledThreadPool(1);
        mUploaderRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "UPLOAD HERE");
            }
        };

        isInitialized = true;
    }

    public void scheduleUploader() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
        String optInString = getResources().getString(R.string.pref_opt_in_data_tracker_key);

        if (!prefs.getBoolean(optInString, false)) return;
        if (!isInitialized) return;
        if (hasScheduledUploader) return;

        mUploader = mScheduler.scheduleAtFixedRate(mUploaderRunnable, 0, UPLOAD_FREQUENCY_VALUE, UPLOAD_FREQUENCY_UNIT);
        hasScheduledUploader = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!isInitialized) return;
        mScheduler = null;
        mUploaderRunnable = null;
        isInitialized = false;

        if(!hasScheduledUploader) return;
        mUploader.cancel(true);
        mUploader = null;
        hasScheduledUploader = false;
    }
}
