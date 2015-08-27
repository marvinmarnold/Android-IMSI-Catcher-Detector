package org.stingraymappingproject.stingwatch.mapping;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.stingraymappingproject.api.clientandroid.StingrayAPIClientService;
import org.stingraymappingproject.stingwatch.R;
import org.stingraymappingproject.stingwatch.service.AimsicdService;
import org.stingraymappingproject.stingwatch.utils.Status;

/**
 * Created by Marvin Arnold on 27/08/15.
 */
public class MappingStingrayAPIClientService extends StingrayAPIClientService {
    private final static String TAG = "MappingClientService";
    protected final IBinder mBinder = new MappingClientServiceBinder();
    protected SharedPreferences prefs;
    protected SharedPreferences.Editor prefsEditor;

    public class MappingClientServiceBinder extends ClientServiceBinder {
        public StingrayAPIClientService getService() {
            return MappingStingrayAPIClientService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
//        Log.d(TAG, "#onBind");
        return mBinder;
    }

    /**
     * Message reciever that handles icon update when status changes
     */
    private BroadcastReceiver mStatusChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, ": mStatusChangeReceiver " + Status.getStatus().name() + ", updating icon");
            switch(Status.getStatus().name()) {
                case("IDLE"):
                    break;
                case("NORMAL"):
                    break;
                case("MEDIUM"):
                    break;
                case("ALARM"):
                    goCrazy();
                    return;
                default:
                    break;
            }
            final String isGoingCrazy = getResources().getString(R.string.mapping_currently_going_crazy);
            prefsEditor = prefs.edit();
            prefsEditor.putBoolean(isGoingCrazy, false);
            prefsEditor.apply();
            startActivityForThreatLevel();
        }
    };

    public void startActivityForThreatLevel() {
        Intent dialogIntent = new Intent(this, MappingActivitySafe.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getApplicationContext().getSharedPreferences(AimsicdService.SHARED_PREFERENCES_BASENAME, 0);

        LocalBroadcastManager.getInstance(this).registerReceiver(mStatusChangeReceiver,
                new IntentFilter("StatusChange"));
        Log.i(TAG, "#onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mStatusChangeReceiver);

        Log.i(TAG, "#onDestroy");
    }

    public void goCrazy() {
        final String termsPref = getResources().getString(R.string.mapping_pref_terms_accepted);
        final String isGoingCrazy = getResources().getString(R.string.mapping_currently_going_crazy);
        if (prefs.getBoolean(termsPref, false) && prefs.getBoolean(isGoingCrazy, false)) {
            Log.d(TAG, "goCrazy()");
            prefsEditor = prefs.edit();
            prefsEditor.putBoolean(isGoingCrazy, true);
            prefsEditor.apply();

//            addRequest((new PostStingrayReadingRequester()).getRequest());

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

            startActivityForThreatLevel();
        }
    }
}
