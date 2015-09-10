package org.stingraymappingproject.stingwatch.mapping;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.stingraymappingproject.api.clientandroid.models.Factoid;
import org.stingraymappingproject.stingwatch.R;
import org.stingraymappingproject.stingwatch.service.AimsicdService;
import org.stingraymappingproject.stingwatch.utils.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Marvin Arnold on 5/09/15.
 */
public class MappingService extends Service {
    private final static String TAG = "MappingService";

    private static final int UPLOAD_FREQUENCY_VALUE = 10;
    private static final TimeUnit UPLOAD_FREQUENCY_UNIT = TimeUnit.MINUTES;

    private static final int FACTOIDS_FREQUENCY_VALUE = 60;
    private static final TimeUnit FACTOIDS_FREQUENCY_UNIT = TimeUnit.MINUTES;
    protected static final int NUM_PRELOADED_FACTOIDS = 5;

    private static final int NEARBY_FREQUENCY_VALUE = 60;
    private static final TimeUnit NEARBY_FREQUENCY_UNIT = TimeUnit.SECONDS;
    private static final int DEFAULT_NEARBY_EXPIRATION_VALUE = 3; // How recent a nearby reading must be in order to be \
    private static final TimeUnit DEFAULT_NEARBY_EXPIRATION_UNIT = TimeUnit.HOURS; // considered a threat
    private int mNearbyExpirationValue = DEFAULT_NEARBY_EXPIRATION_VALUE;
    private TimeUnit mNearbyExpirationUnit = DEFAULT_NEARBY_EXPIRATION_UNIT;

    private boolean mBoundToAIMSICD = false;
    private AimsicdService mAimsicdService;

    private final MappingBinder mBinder = new MappingBinder();

    public List<Factoid> getFactoids() {
        if(mFactoids == null) mFactoids = new ArrayList<>();
        return mFactoids;
    }

    private List<Factoid> mFactoids;

    @Override
    public void onCreate() {
        super.onCreate();

        if(MappingPreferences.areTermsAccepted(this)) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mStatusChangeReceiver,
                    new IntentFilter("StatusChange"));

            OCIDAPIHelper.setOCIDKey(this);
            startAIMSICDService();
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MappingBinder extends Binder {
        public MappingService getService() {
            return MappingService.this;
        }
    }

    private void startAIMSICDService() {
        if (!mBoundToAIMSICD) {
            // Bind to LocalService
            Intent intent = new Intent(MappingService.this, AimsicdService.class);
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
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG, "Service Disconnected");
            mBoundToAIMSICD = false;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    /**
     * When this service stops, also stop AIMSICD core detection
     */
    private void stop() {
        // Unbind from the AIMSICD service
        if (mBoundToAIMSICD) {
            unbindService(mAIMSICDServiceConnection);
            mBoundToAIMSICD = false;
        }
        stopService(new Intent(this, AimsicdService.class));
    }

    /**
     * Reciever that handles updates to status changes
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

            startMappingActivityUndetected();
        }
    };


    public void goCrazy() {
        startMappingActivityDetected();

//            addRequest((new PostStingrayReadingRequester()).getRequest());

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.stingwatch_danger)
                .setContentTitle("Stingray Detected")
                .setContentText("Open StingWatch to learn what this means.");
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MappingActivityDetected.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MappingActivityDetected.class);
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
    }

    private void startMappingActivityUndetected() {
        Intent dialogIntent = new Intent(this, MappingActivityUndetected.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
    }

    private void startMappingActivityDetected() {
        Intent dialogIntent = new Intent(this, MappingActivityDetected.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
    }
}
