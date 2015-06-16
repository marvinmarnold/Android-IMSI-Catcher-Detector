package com.SecUpwN.AIMSICD.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.service.DataTrackerService;

/**
 * Created by Marvin Arnold on 15/06/15.
 */
public class ConnectivityReceiver extends BroadcastReceiver {
    private static final String TAG = "ConnectivityReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // Alternative approach here https://stackoverflow.com/questions/3767591/check-intent-internet-connection
        if(action.equals("android.net.conn.CONNECTIVITY_CHANGE")){
            Log.d(TAG, "Connectivity Change");
            // inform service of updated connection state
            Intent syncDataIntent = new Intent(context, DataTrackerService.class);
            syncDataIntent.setAction(DataTrackerService.ACTION_SYNC_DATA);
            context.startService(syncDataIntent);
        }
    }
}
