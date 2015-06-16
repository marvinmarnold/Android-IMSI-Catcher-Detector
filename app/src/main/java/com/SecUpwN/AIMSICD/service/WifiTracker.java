package com.SecUpwN.AIMSICD.service;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by Marvin Arnold on 15/06/15.
 */
public class WifiTracker {
    private final static String TAG = "WifiTracker";

    private WifiManager mWifiManager;

    public WifiTracker(Context context) {
        this.mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public int getNumHotspots() {
        int numHotspots = mWifiManager.getScanResults().size();
        Log.d(TAG, "There are this many hotspots: " + numHotspots);
        return numHotspots;
    }

    public void start() {
        mWifiManager.startScan();
    }
}