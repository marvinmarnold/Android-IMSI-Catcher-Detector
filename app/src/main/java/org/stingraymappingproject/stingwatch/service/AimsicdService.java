/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */

/*
 * Portions of this software have been copied and modified from
 * https://github.com/illarionov/SamsungRilMulticlient
 * Copyright (C) 2014 Alexey Illarionov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.stingraymappingproject.stingwatch.service;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.WindowManager;

import org.stingraymappingproject.stingwatch.R;
import org.stingraymappingproject.stingwatch.mapping.MappingActivityDanger;
import org.stingraymappingproject.stingwatch.rilexecutor.RilExecutor;
import org.stingraymappingproject.stingwatch.smsdetection.SmsDetector;
import org.stingraymappingproject.stingwatch.utils.Cell;
import org.stingraymappingproject.stingwatch.utils.GeoLocation;
import org.stingraymappingproject.stingwatch.utils.Status;

/**
 *  Description:    This starts the (background?) AIMSICD service to check for SMS and track
 *                  cells with or without GPS enabled.
 *                  TODO: better and more detailed explanation!
 */
public class AimsicdService extends Service {

    //private final String TAG = "AimsicdService";
    private final String TAG = "AIMSICD";
    private final String mTAG = "AimsicdService";

    // /data/data/com.SecUpwN.AIMSICD/shared_prefs/com.SecUpwN.AIMSICD_preferences.xml
    public static final String SHARED_PREFERENCES_BASENAME = "com.SecUpwN.AIMSICD_preferences";
    public static final String UPDATE_DISPLAY = "UPDATE_DISPLAY";

    /*
     * System and helper declarations
     */
    private final AimscidBinder mBinder = new AimscidBinder();
    private final Handler timerHandler = new Handler();

    private CellTracker mCellTracker;
    private AccelerometerMonitor mAccelerometerMonitor;
    private SignalStrengthTracker signalStrengthTracker;
    private LocationTracker mLocationTracker;
    private RilExecutor mRilExecutor;
    private SmsDetector smsdetector;

    private boolean isLocationRequestShowing = false;

    protected SharedPreferences prefs;
    protected SharedPreferences.Editor prefsEditor;

    /**
     * Message reciever that handles icon update when status changes
     */
    private BroadcastReceiver mStatusChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, mTAG + ": StatusWatcher received status change to " + Status.getStatus().name() + ", updating icon");
            switch(Status.getStatus().name()) {
                case("IDLE"):
                    return;
                case("NORMAL"):
                    return;
                case("MEDIUM"):
                    return;
                case("ALARM"):
                    goCrazy();
                default:
                    return;
            }
        }
    };


    private void goCrazy() {
        Log.d(TAG, "goCrazy()");
        final String termsPref = getResources().getString(R.string.mapping_pref_terms_accepted);
        final String isGoingCrazy = getResources().getString(R.string.mapping_currently_going_crazy);
        if (prefs.getBoolean(termsPref, false) && prefs.getBoolean(termsPref, false)) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.stingwatch_danger)
                    .setContentTitle("Stingray Detected")
                    .setContentText("Open StingWatch to learn what this means.");
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(this, MappingActivityDanger.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MappingActivityDanger.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);

            mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
            //Vibration
            mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

            //LED
            mBuilder.setLights(Color.RED, 3000, 3000);

            //Ton
            mBuilder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stingwatch_sound));

            // mId allows you to update the notification later on.
            mNotificationManager.notify(1, mBuilder.build());

            prefsEditor = prefs.edit();
            prefsEditor.putBoolean(isGoingCrazy, true);
            prefsEditor.apply();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class AimscidBinder extends Binder {

        public AimsicdService getService() {
            return AimsicdService.this;
        }
    }

    public void onCreate() {

        signalStrengthTracker = new SignalStrengthTracker(getBaseContext());

        mAccelerometerMonitor = new AccelerometerMonitor(this, new Runnable() {
            @Override
            public void run() {
                // movement detected, so enable GPS
                mLocationTracker.start();
                signalStrengthTracker.onSensorChanged();


                // check again in a while to see if GPS should be disabled
                // this runnable also re-enables this movement sensor
                timerHandler.postDelayed(batterySavingRunnable, AccelerometerMonitor.MOVEMENT_THRESHOLD_MS);
            }
        });

        mLocationTracker = new LocationTracker(this, mLocationListener);
        mRilExecutor = new RilExecutor(this);
        mCellTracker = new CellTracker(this, signalStrengthTracker);

        LocalBroadcastManager.getInstance(this).registerReceiver(mStatusChangeReceiver,
                new IntentFilter("StatusChange"));
        prefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_BASENAME, 0);
        Log.i(TAG, mTAG + ": Service launched successfully.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCellTracker.stop();
        mLocationTracker.stop();
        mAccelerometerMonitor.stop();
        mRilExecutor.stop();

        if (SmsDetector.getSmsDetectionState()) {
            smsdetector.stopSmsDetection();
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mStatusChangeReceiver);

        Log.i(TAG, mTAG +  ": Service destroyed.");
    }

    public GeoLocation lastKnownLocation() {
        return mLocationTracker.lastKnownLocation();
    }

    public RilExecutor getRilExecutor() {
        return mRilExecutor;
    }

    public CellTracker getCellTracker() {
        return mCellTracker;
    }

    public Cell getCell() {
        return mCellTracker.getDevice().mCell;
    }

    public void setCell(Cell cell) {
        mCellTracker.getDevice().mCell = cell;
    }

    public boolean isTrackingCell() {
        return mCellTracker.isTrackingCell();
    }

    public boolean isMonitoringCell() {
        return mCellTracker.isMonitoringCell();
    }

    public void setCellMonitoring(boolean monitor) {
        mCellTracker.setCellMonitoring(monitor);
    }

    public boolean isTrackingFemtocell() {
        return mCellTracker.isTrackingFemtocell();
    }

    public void setTrackingFemtocell(boolean track) {
        if (track) mCellTracker.startTrackingFemto();
        else mCellTracker.stopTrackingFemto();
    }

    // SMS Detection Thread
    public boolean isSmsTracking() {
        return SmsDetector.getSmsDetectionState();
    }

    public void startSmsTracking() {
        if(!isSmsTracking()) {
            Log.i(TAG, mTAG +  ": Sms Detection Thread Started");
            smsdetector = new SmsDetector(this);
            smsdetector.startSmsDetection();
        }
    }

    public void stopSmsTracking() {
        if(isSmsTracking()) {
            smsdetector.stopSmsDetection();
            Log.i(TAG, mTAG +  ": Sms Detection Thread Stopped");
        }
    }

    // while tracking a cell, manage the power usage by switching off GPS if no movement
    private final Runnable batterySavingRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCellTracker.isTrackingCell()) {
                //Log.d(TAG, mTAG +  ": (power): Checking to see if GPS should be disabled");
                // if no movement in a while, shut off GPS. Gets re-enabled when there is movement
                if (mAccelerometerMonitor.notMovedInAWhile() ||
                        mLocationTracker.notMovedInAWhile()) {
                    //Log.d(TAG, mTAG +  ": (power): Disabling GPS");
                    mLocationTracker.stop();
                }
                mAccelerometerMonitor.start();
            }
        }
    };

    /**
     * Cell Information Tracking and database logging
     *
     * @param track Enable/Disable tracking
     */
    public void setCellTracking(boolean track) {
        mCellTracker.setCellTracking(track);

        if (track) {
            mLocationTracker.start();
            mAccelerometerMonitor.start();
        } else {
            mLocationTracker.stop();
            mAccelerometerMonitor.stop();
        }
    }

    public void checkLocationServices() {
        if (mCellTracker.isTrackingCell() && !mLocationTracker.isGPSOn()) {
            enableLocationServices();
        }
    }

    public LocationTracker getLocationTracker() {
        return this.mLocationTracker;
    }

    private void enableLocationServices() {
        if (isLocationRequestShowing) return; // only show dialog once

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.location_error_message)
                .setTitle(R.string.location_error_title)
                .setCancelable(false)
                .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        isLocationRequestShowing = false;
                        Intent gpsSettings = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        gpsSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(gpsSettings);
                    }
                })
                .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        isLocationRequestShowing = false;
                        setCellTracking(false);
                    }
                });
        AlertDialog alert = builder.create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
        isLocationRequestShowing = true;
    }

    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location loc) {
            timerHandler.postDelayed(batterySavingRunnable, AccelerometerMonitor.MOVEMENT_THRESHOLD_MS);
            mCellTracker.onLocationChanged(loc);
        }

        @Override
        public void onProviderDisabled(String provider) {
            if (mCellTracker.isTrackingCell() && provider.equals(LocationManager.GPS_PROVIDER)) {
                enableLocationServices();
            }
        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }
    };
}
