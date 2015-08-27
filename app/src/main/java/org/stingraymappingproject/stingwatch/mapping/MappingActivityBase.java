package org.stingraymappingproject.stingwatch.mapping;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;
import org.stingraymappingproject.api.clientandroid.RecurringRequest;
import org.stingraymappingproject.api.clientandroid.activities.BaseStingrayActivity;
import org.stingraymappingproject.api.clientandroid.models.Factoid;
import org.stingraymappingproject.api.clientandroid.models.StingrayReading;
import org.stingraymappingproject.api.clientandroid.requesters.FactoidsRequester;
import org.stingraymappingproject.api.clientandroid.requesters.NearbyRequester;
import org.stingraymappingproject.api.clientandroid.requesters.PostStingrayReadingRequester;
import org.stingraymappingproject.stingwatch.AppAIMSICD;
import org.stingraymappingproject.stingwatch.R;
import org.stingraymappingproject.stingwatch.service.AimsicdService;
import org.stingraymappingproject.stingwatch.utils.GeoLocation;
import org.stingraymappingproject.stingwatch.utils.Status;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Marvin Arnold on 15/08/15.
 */
public class MappingActivityBase extends BaseStingrayActivity {
    private final static String TAG = "MappingActivityBase";
    private static final int UPLOAD_FREQUENCY_VALUE = 10;
    private static final TimeUnit UPLOAD_FREQUENCY_UNIT = TimeUnit.MINUTES;

    private static final int FACTOIDS_FREQUENCY_VALUE = 15;
    private static final TimeUnit FACTOIDS_FREQUENCY_UNIT = TimeUnit.MINUTES;
    protected static final int NUM_PRELOADED_FACTOIDS = 5;

    private static final int NEARBY_FREQUENCY_VALUE = 60;
    private static final TimeUnit NEARBY_FREQUENCY_UNIT = TimeUnit.SECONDS;
    private static final int DEFAULT_NEARBY_EXPIRATION_VALUE = 30;
    private static final TimeUnit DEFAULT_NEARBY_EXPIRATION_UNIT = TimeUnit.DAYS;
    private int mNearbyExpirationValue = DEFAULT_NEARBY_EXPIRATION_VALUE;
    private TimeUnit mNearbyExpirationUnit = DEFAULT_NEARBY_EXPIRATION_UNIT;

    protected Toolbar mToolbar;
    protected Toolbar mActionToolbar;

    private final Context mContext = this;

    protected boolean mBoundToAIMSICD;
    protected AimsicdService mAimsicdService;

    protected SharedPreferences prefs;
    protected SharedPreferences.Editor prefsEditor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        prefs = mContext.getSharedPreferences( AimsicdService.SHARED_PREFERENCES_BASENAME, 0);

        startAIMSICDService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stingray_mapping_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.menu_stingray_mapping_toolbar_settings:
                Intent intent = new Intent(MappingActivityBase.this, MappingPrefActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void startAIMSICDService() {
        if (!mBoundToAIMSICD) {
            // Bind to LocalService
            Intent intent = new Intent(MappingActivityBase.this, AimsicdService.class);
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
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the AIMSICD service
        if (mBoundToAIMSICD) {
            unbindService(mAIMSICDServiceConnection);
            mBoundToAIMSICD = false;
        }
        stopService(new Intent(mContext, AimsicdService.class));
    }

    /**
     * Triggered when GUI is opened
     */
    @Override
    public void onResume() {
        super.onResume();
        startAIMSICDService();
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

    @Override
    protected void scheduleRequesters() {
        scheduleFactoidsRequester();
        scheduleNearbyRequester();
        schedulePostStingrayReadingRequester();
    }

    protected double lastKnownLatDegrees() throws Exception {
        if(mBoundToAIMSICD && mAimsicdService != null && mAimsicdService.lastKnownLocation() != null) {
            GeoLocation lastLoc = mAimsicdService.lastKnownLocation();
            return lastLoc.getLatitudeInDegrees();
        }
        throw new Exception("Pervious latitude available is not available.");
    }

    protected double lastKnownLongDegrees() throws Exception {
        if(mBoundToAIMSICD && mAimsicdService.lastKnownLocation() != null) {
            GeoLocation lastLoc = mAimsicdService.lastKnownLocation();
            return lastLoc.getLongitudeInDegrees();
        }
        throw new Exception("Pervious longitude available is not available.");
    }

    protected void startActivityForThreatLevel(Activity activity) {
        Log.d(TAG, "startActivityForThreatLevel: " + Status.getStatus().name());
        switch(Status.getStatus().name()) {
            case("IDLE"):
                startSafe(activity);
            case("NORMAL"):
                startSafe(activity);
            case("MEDIUM"):
                startSafe(activity);
            case("ALARM"):
                startSafe(activity);

//                startDanger(activity);
            default:
                startSafe(activity);
        }
    }

    private void startSafe(Activity activity) {
        if(!activity.getClass().equals(MappingActivitySafe.class)) {
            Intent i = new Intent(getApplicationContext(), MappingActivitySafe.class);
            startActivity(i);
        }
    }

    private void startDanger(Activity activity) {
        if(!activity.getClass().equals(MappingActivityDanger.class)) {
            Intent i = new Intent(getApplicationContext(), MappingActivityDanger.class);
            startActivity(i);
        }
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

    @Override
    public void startStingrayClientService() {
        Log.d(TAG, "startStingrayClientService");
        if (!mBoundToStingrayAPIService) {
            // Bind to LocalService
            Intent intent = new Intent(MappingActivityBase.this, MappingStingrayAPIClientService.class);
            //Start Service before binding to keep it resident when activity is destroyed
            startService(intent);
            bindService(intent, mStingrayAPIServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void scheduleNearbyRequester() {
        NearbyRequester nearbyRequester = new NearbyRequester(mStingrayAPIService) {
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

                Log.d(TAG, "scheduleNearbyRequester: " + timeAndSpaceField);
                return timeAndSpaceField.toString();
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(StingrayReading[] response) {
                Log.d(TAG, "scheduleNearbyRequester:onResponse");
                if(response.length > 0 && mBoundToStingrayAPIService) {
                    for(StingrayReading stingrayReading : response) {
                        if(isNewStingrayReading(stingrayReading))
                            mStingrayAPIService.addStingrayReading(stingrayReading);
                    }
                }
            }
        };
        RecurringRequest recurringRequest = new RecurringRequest(NEARBY_FREQUENCY_VALUE, NEARBY_FREQUENCY_UNIT, nearbyRequester);
        mStingrayAPIService.addRecurringRequest(recurringRequest);
    }

    private boolean isNewStingrayReading(StingrayReading stingrayReading) {
        return true;
    }

    /**
     *
     */
    private void scheduleFactoidsRequester() {
        FactoidsRequester factoidsRequester = new FactoidsRequester(mStingrayAPIService) {
            @Override
            public void onResponse(Factoid[] response) {
                Log.d(TAG, "scheduleFactoidsRequester:onResponse");
                if(response.length > 0 && mBoundToStingrayAPIService) {
                    mStingrayAPIService.setFactoids(response);
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse");
            }
        };
        RecurringRequest recurringRequest = new RecurringRequest(FACTOIDS_FREQUENCY_VALUE, FACTOIDS_FREQUENCY_UNIT, factoidsRequester);
        mStingrayAPIService.addRecurringRequest(recurringRequest);
    }

    public static Factoid createPreloadedFactoid(Context context, int n) {
        if(n < 0 || n >= NUM_PRELOADED_FACTOIDS) return null;
        switch(n) {
            case 0:
                return new Factoid(context.getString(R.string.mapping_factoids_1));
            case 1:
                return new Factoid(context.getString(R.string.mapping_factoids_2));
            case 2:
                return new Factoid(context.getString(R.string.mapping_factoids_3));
            case 3:
                return new Factoid(context.getString(R.string.mapping_factoids_4));
            case 4:
                return new Factoid(context.getString(R.string.mapping_factoids_5));
        }
        return null;
    }

    /**
     *
     */
    private void schedulePostStingrayReadingRequester() {
        Log.d(TAG, "schedulePostStingrayReadingRequester");
        PostStingrayReadingRequester postStingrayReadingRequester = new PostStingrayReadingRequester(mStingrayAPIService) {
            @Override
            protected String getRequestParams() {
                return getReqParamsAndAddNewReading();
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onResponse(StingrayReading response) {
                Log.d(TAG, "schedulePostStingrayReadingRequester:onResponse");
                if(mBoundToStingrayAPIService)
                    mStingrayAPIService.addStingrayReading(response);
            }


        };
        RecurringRequest recurringRequest = new RecurringRequest(UPLOAD_FREQUENCY_VALUE, UPLOAD_FREQUENCY_UNIT, postStingrayReadingRequester);
        mStingrayAPIService.addRecurringRequest(recurringRequest);
    }

    private String getReqParamsAndAddNewReading() {
        JSONObject attributeFields = new JSONObject();
        JSONObject stingrayJSON = new JSONObject();

        Date _observed_at = new Date();
        int _threat_level = getStatus();
        double _lat = 1.1;
        double _long = 2.2;
        String _version = getVersion();
        StingrayReading stingrayReading = new StingrayReading(_threat_level, _observed_at, _lat, _long, null, _version);
        if(mBoundToStingrayAPIService)
            mStingrayAPIService.addStingrayReading(stingrayReading);

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

    protected void loadFactoids() {
        Log.d(TAG, "loadFactoids");
        if(mBoundToStingrayAPIService) {
            Log.d(TAG, "loadFactoids:mBoundToStingrayAPIService");
            List<Factoid> factoids = mStingrayAPIService.getFactoids();
            if (factoids.isEmpty()) {
                for (int i = 0; i < NUM_PRELOADED_FACTOIDS; i++) {
                    Factoid factoid = createPreloadedFactoid(mContext, i);
                    mStingrayAPIService.addFactoid(factoid);
                }
            }
        }
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
