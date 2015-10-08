package org.stingraymappingproject.stingwatch.mapping;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;
import org.stingraymappingproject.api.clientandroid.RecurringRequest;
import org.stingraymappingproject.api.clientandroid.StingrayAPIClientService;
import org.stingraymappingproject.api.clientandroid.models.Factoid;
import org.stingraymappingproject.api.clientandroid.models.StingrayReading;
import org.stingraymappingproject.api.clientandroid.requesters.NearbyRequester;
import org.stingraymappingproject.api.clientandroid.requesters.PostStingrayReadingRequester;
import org.stingraymappingproject.stingwatch.R;
import org.stingraymappingproject.stingwatch.service.AimsicdService;
import org.stingraymappingproject.stingwatch.utils.GeoLocation;
import org.stingraymappingproject.stingwatch.utils.Status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Marvin Arnold on 5/09/15.
 */
public class MappingService extends StingrayAPIClientService {
    private final static String TAG = "MappingService";

    private static final int UPLOAD_FREQUENCY_VALUE = 10;
    private static final TimeUnit UPLOAD_FREQUENCY_UNIT = TimeUnit.MINUTES;

    private static final int FACTOIDS_FREQUENCY_VALUE = 10;
    private static final TimeUnit FACTOIDS_FREQUENCY_UNIT = TimeUnit.MINUTES;

    private static final int NEARBY_FREQUENCY_VALUE = 10;
    private static final TimeUnit NEARBY_FREQUENCY_UNIT = TimeUnit.MINUTES;
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        scheduleRequesters();
        scheduleRecurringRequests();

        return START_STICKY;
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

    /**
     * When this service stops, also stop AIMSICD core detection
     */
    private void stop() {
        // Unbind from the AIMSICD service
        if (mBoundToAIMSICD) {
            unbindService(mAIMSICDServiceConnection);
            mBoundToAIMSICD = false;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mStatusChangeReceiver);
        stopService(new Intent(this, AimsicdService.class));
    }

    /**
     * Reciever that handles updates to status changes
     */
    private BroadcastReceiver mStatusChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG, ": mStatusChangeReceiver " + Status.getStatus().name() + ", updating icon");
            switch(Status.getStatus().name()) {
                case("IDLE"):
                    break;
                case("NORMAL"):
                    break;
                case("MEDIUM"):
                    break;
                case("ALARM"):
                    newPostStingrayReadingRequest().run();
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
                .setContentText("Open StingWatch to take action.");
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

    public GeoLocation lastKnownLocation() {
        if(mBoundToAIMSICD) return mAimsicdService.lastKnownLocation();
        return null;
    }

    protected void scheduleRequesters() {
//        Log.d(TAG, "scheduleRequesters");
//        scheduleFactoidsRequester();
        scheduleNearbyRequester();
        schedulePostStingrayReadingRequester();
    }

    protected double lastKnownLatDegrees() throws Exception {
        if(mBoundToAIMSICD && mAimsicdService != null && mAimsicdService.lastKnownLocation() != null) {
            GeoLocation lastLoc = mAimsicdService.lastKnownLocation();
            return lastLoc.getLatitudeInDegrees();
        }
        throw new Exception("Previous latitude available is not available.");
    }

    protected double lastKnownLongDegrees() throws Exception {
        if(mBoundToAIMSICD && mAimsicdService.lastKnownLocation() != null) {
            GeoLocation lastLoc = mAimsicdService.lastKnownLocation();
            return lastLoc.getLongitudeInDegrees();
        }
        throw new Exception("Previous longitude available is not available.");
    }

    protected Date now() {
        return new Date();
    }

    /**
     * Returns the date agoValue (eg 2) agoUnit (eg days) ago (eg 2 days ago)
     * @param agoValue
     * @param agoUnit
     * @return
     */
    protected Date then(int agoValue, TimeUnit agoUnit) {
        return new Date(now().getTime() - agoUnit.toMillis(agoValue));
    }

    private void scheduleNearbyRequester() {
//        Log.d(TAG, "scheduleNearbyRequester");
        NearbyRequester nearbyRequester = new NearbyRequester(this) {
            @Override
            protected String getRequestParams() {
                JSONObject nearbyFields = new JSONObject();
                JSONObject timeAndSpaceField = new JSONObject();
                try {
                    //:lat,:long,:since)
                    nearbyFields.put("lat", lastKnownLatDegrees());
                    nearbyFields.put("long", lastKnownLongDegrees());
                    nearbyFields.put("since", then(mNearbyExpirationValue, mNearbyExpirationUnit));
                    timeAndSpaceField.put("time_and_space", nearbyFields);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                Log.d(TAG, "scheduleNearbyRequester: " + timeAndSpaceField);
                return timeAndSpaceField.toString();
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(StingrayReading[] response) {
//                Log.d(TAG, "scheduleNearbyRequester:onResponse");
                if(response.length > 0) {
//                    Log.d(TAG, "scheduleNearbyRequester:gocrazy");
                    Status.setCurrentStatus(Status.Type.ALARM, getApplicationContext());
//                    ((MappingStingrayAPIClientService) mStingrayAPIService).goCrazy();
                    for(StingrayReading stingrayReading : response) {
                        if(isNewStingrayReading(stingrayReading)) {
                            addStingrayReading(stingrayReading);
                        }
                    }
                }
            }
        };

        RecurringRequest recurringRequest = new RecurringRequest(NEARBY_FREQUENCY_VALUE, NEARBY_FREQUENCY_UNIT, nearbyRequester);
        addRecurringRequest(recurringRequest);
    }

    private boolean isNewStingrayReading(StingrayReading stingrayReading) {
        return true;
    }

//    /**
//     *
//     */
//    private void scheduleFactoidsRequester() {
////        Log.d(TAG, "scheduleFactoidsRequester");
//        FactoidsRequester factoidsRequester = new FactoidsRequester(this) {
//            @Override
//            public void onResponse(Factoid[] response) {
////                Log.d(TAG, "scheduleFactoidsRequester:onResponse");
//                if(response.length > 0) {
//                    setFactoids(response);
//                }
//            }
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
////                Log.d(TAG, "onErrorResponse");
//            }
//        };
//        RecurringRequest recurringRequest = new RecurringRequest(FACTOIDS_FREQUENCY_VALUE, FACTOIDS_FREQUENCY_UNIT, factoidsRequester);
//        addRecurringRequest(recurringRequest);
//    }

    private PostStingrayReadingRequester newPostStingrayReadingRequest() {
        return new PostStingrayReadingRequester(this) {
            @Override
            protected String getRequestParams() {
                return getReqParamsAndAddNewReading();
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(StingrayReading response) {
//                Log.d(TAG, "schedulePostStingrayReadingRequester:onResponse");
                addStingrayReading(response);
            }
        };
    }

    /**
     *
     */
    private void schedulePostStingrayReadingRequester() {
//        Log.d(TAG, "schedulePostStingrayReadingRequester");
        PostStingrayReadingRequester postStingrayReadingRequester = newPostStingrayReadingRequest();
        RecurringRequest recurringRequest = new RecurringRequest(UPLOAD_FREQUENCY_VALUE, UPLOAD_FREQUENCY_UNIT, postStingrayReadingRequester);
        addRecurringRequest(recurringRequest);
    }

    private String getReqParamsAndAddNewReading() {
        JSONObject attributeFields = new JSONObject();
        JSONObject stingrayJSON = new JSONObject();
        double _lat = MappingActivityDetected.DEFAULT_MAP_LAT;
        double _long = MappingActivityDetected.DEFAULT_MAP_LONG;
        if(mBoundToAIMSICD && mAimsicdService.lastKnownLocation() != null) {
            GeoLocation lastLoc = mAimsicdService.lastKnownLocation();
            _lat = lastLoc.getLatitudeInDegrees();
            _long = lastLoc.getLongitudeInDegrees();
        }
        Date _observed_at = new Date();
        int _threat_level = getStatus();
        String _version = getVersion();
        StingrayReading stingrayReading = new StingrayReading(_threat_level, _observed_at, _lat, _long, null, _version);
        addStingrayReading(stingrayReading);

        try {
            //:lat,:long,:since)
            attributeFields.put("threat_level", _threat_level);
            attributeFields.put("lat", _lat);
            attributeFields.put("long", _long);
            attributeFields.put("observed_at", _observed_at);
            attributeFields.put("unique_token", stingrayReading.getUniqueToken());
            attributeFields.put("version", _version);
            stingrayJSON.put("stingray_reading", attributeFields);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        Log.d(TAG, "schedulePostStingrayReadingRequester: " + stingrayJSON);
        return stingrayJSON.toString();
    }

    public String getVersion() {
        PackageManager pm = getPackageManager();
        PackageInfo pInfo = null;
        try {
            pInfo = pm.getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            return "App could not detect version number.";
        }
        return pInfo.versionName;
    }

    public int getStatus() {
        return statusToAPIInt(Status.getStatus());
    }

    public int statusToAPIInt(Status.Type statusType) {
        switch(statusType.toString()) {
            case("IDLE"):
                return 0;
            case("NORMAL"):
                return 5;
            case("MEDIUM"):
                return 10;
            case("ALARM"):
                return 15;
            default:
                return -1;
        }
    }
}
