package com.SecUpwN.AIMSICD;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.SecUpwN.AIMSICD.activities.BaseActivity;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.service.DataTrackerService;

/**
 * Modified from AIMSICD.java
 */
public class AIMSICDMapper extends BaseActivity {
    private final static String TAG = "AIMSICDMapper";

    private final Context mContext = this;

    private boolean mBoundToAIMSICD;
    private AimsicdService mAimsicdService;

    private boolean mBoundToDataTrackerService;
    private DataTrackerService mDataTrackerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        WebView webview = new WebView(this);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true);
        
        webview.loadUrl("http://stingray.meteor.com/");
        setContentView(webview);


        startAIMSICDService();
        startDataTrackerService();
    }

    private void startAIMSICDService() {
        if (!mBoundToAIMSICD) {
            // Bind to LocalService
            Intent intent = new Intent(AIMSICDMapper.this, AimsicdService.class);
            //Start Service before binding to keep it resident when activity is destroyed
            startService(intent);
            bindService(intent, mAIMSICDServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * Service Connection to bind the activity to the service
     */
    private final ServiceConnection mAIMSICDServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mAimsicdService = ((AimsicdService.AimscidBinder) service).getService();
            mBoundToAIMSICD = true;

            //If tracking cell details check location services are still enabled
            if (mAimsicdService.isTrackingCell()) {
                mAimsicdService.checkLocationServices();
            }

            initAndScheduleDataTracker();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG, "Service Disconnected");
            mBoundToAIMSICD = false;
        }
    };

    private final ServiceConnection mDataTrackerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(TAG, "DataTrackerService Connected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mDataTrackerService = ((DataTrackerService.LocalBinder) binder).getService();
            mBoundToDataTrackerService = true;

            if(!mBoundToAIMSICD) return;
            initAndScheduleDataTracker();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG, "DataTrackerService Disconnected");
            mBoundToDataTrackerService = false;
        }
    };

    private void initAndScheduleDataTracker() {
        if(mBoundToDataTrackerService) {
            mDataTrackerService.initUploader(mAimsicdService.getLocationTracker());
            mDataTrackerService.scheduleUploader();
        }
    }

    private void startDataTrackerService() {
        if (!mBoundToDataTrackerService) {
            Log.d(TAG, "Stated DataTrackerService");
            // Bind to LocalService
            Intent intent = new Intent(AIMSICDMapper.this, DataTrackerService.class);
            //Start Service before binding to keep it resident when activity is destroyed
            startService(intent);
            bindService(intent, mDataTrackerServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the AIMSICD service
        if (mBoundToAIMSICD) {
            unbindService(mAIMSICDServiceConnection);
            mBoundToAIMSICD = false;
        }

        // Unbind from the DataTracker service
        if (mBoundToDataTrackerService) {
            unbindService(mDataTrackerServiceConnection);
            mBoundToDataTrackerService = false;
        }

        stopService(new Intent(mContext, AimsicdService.class));
        stopService(new Intent(mContext, DataTrackerService.class));
    }

    /**
     * Triggered when GUI is opened
     */
    @Override
    public void onResume() {
        super.onResume();
        startAIMSICDService();
        startDataTrackerService();
    }

    public void onStop() {
        super.onStop();
        ((AppAIMSICD) getApplication()).detach(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AppAIMSICD) getApplication()).attach(this);
    }
}
