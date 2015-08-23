package com.SecUpwN.AIMSICD.mapping;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.SecUpwN.AIMSICD.service.LocationTracker;
import com.SecUpwN.AIMSICD.utils.Status;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Marvin Arnold on 15/06/15.
 */
public class MappingDataTrackerService extends Service {
    private static final String TAG = "DataTrackerService";
    private final IBinder mBinder = new LocalBinder();
    private LocationTracker mLocationTracker;
    private ConnectivityManager mConnectivityManager;

    private boolean isInitialized;
    private ScheduledExecutorService mScheduler;

    private List<MappingDataTrackerTask> mTasks;

    private static final int UPLOAD_FREQUENCY_VALUE = 20;
    private static final TimeUnit UPLOAD_FREQUENCY_UNIT = TimeUnit.SECONDS;

    private static final int FACTOIDS_FREQUENCY_VALUE = 10;
    private static final TimeUnit FACTOIDS_FREQUENCY_UNIT = TimeUnit.SECONDS;

    private static final int NEARBY_FREQUENCY_VALUE = 30;
    private static final TimeUnit NEARBY_FREQUENCY_UNIT = TimeUnit.MINUTES;

    private final static String mUploadAPIBase ="http://api.stingraymappingproject.org/";
    private RequestQueue mQueue;
    private ArrayList<MappingDataRequestParams> mOfflineRequests;
    public static String ACTION_SYNC_DATA = "ACTION_SYNC_DATA";

    public class LocalBinder extends Binder {
        public MappingDataTrackerService getService() {
            return MappingDataTrackerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Something bound to DataTrackerService");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Start");
        if (intent != null &&
            intent.getAction() != null &&
            intent.getAction().equals(ACTION_SYNC_DATA))
            queueOfflineRequests();
        
        return START_STICKY;
    }

    public void initDataTrackerService(LocationTracker locationTracker) {
        if(isInitialized) return;
        Context context = getApplicationContext();
        this.mLocationTracker = locationTracker;
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.mQueue = Volley.newRequestQueue(context);
        this.mOfflineRequests = new ArrayList<>();

        mScheduler = Executors.newScheduledThreadPool(1);
        mTasks = new ArrayList<>();

        initUploader();
        initFactoids();
        initNearby();

        isInitialized = true;
    }

    public void initFactoids() {
        final Response.Listener<JSONArray> successListener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d(TAG, "Factoid response");
            }
        };

        final Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Factoid fail");
                Log.d(TAG, error.getMessage());
            }
        };

        Runnable mFactoidsRunnable = new Runnable() {
            @Override
            public void run() {
                MappingDataRequestParams drp = new MappingDataRequestParams(
                        new JSONObject(),
                        "factoids",
                        Request.Method.GET,
                        successListener,
                        errorListener);
                request(drp);
            }

            public void request(MappingDataRequestParams dataRequestParams) {
                mQueue.add(buildArrayRequest(dataRequestParams));
            }
        };

        mTasks.add(new MappingDataTrackerTask(FACTOIDS_FREQUENCY_VALUE, FACTOIDS_FREQUENCY_UNIT, mFactoidsRunnable));
    }

    public void initUploader() {
        final Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Uploader response");
            }
        };

        final Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Uploader error");
//                Log.d(TAG, error.toString());
            }
        };

        Runnable mUploaderRunnable = new Runnable() {
            @Override
            public void run() {
                MappingDataRequestParams drp = new MappingDataRequestParams(
                        null,
                        "stingray_readings",
                        Request.Method.POST,
                        successListener,
                        errorListener);
                upload(drp);
            }

            public void upload(MappingDataRequestParams dataRequestParams) {
                mQueue.add(buildObjectRequest(dataRequestParams));
            }

            JSONObject getImsiJSON() {
                JSONObject imsiFields = new JSONObject();
                JSONObject datum = new JSONObject();

                try {
                    imsiFields = addComonFields(imsiFields);
                    imsiFields.put("threat_level", getStatus());
                    datum.put("stingray_reading", imsiFields);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                return datum;
            }

            JSONObject addComonFields(JSONObject jsonObject) throws JSONException, PackageManager.NameNotFoundException {
                jsonObject.put("lat", mLocationTracker.lastKnownLocation().getLatitudeInDegrees());
                jsonObject.put("long", mLocationTracker.lastKnownLocation().getLongitudeInDegrees());
                jsonObject.put("observed_at", DateFormat.getDateTimeInstance().format(new Date()));

                PackageManager pm = getPackageManager();
                PackageInfo pInfo = pm.getPackageInfo(getPackageName(), 0);
                String version = pInfo.versionName;
                jsonObject.put("version", version);

                return jsonObject;
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
        };

        mTasks.add(new MappingDataTrackerTask(UPLOAD_FREQUENCY_VALUE, UPLOAD_FREQUENCY_UNIT, mUploaderRunnable));
    }

    public void initNearby() {
        final Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Nearby response");
            }
        };

        final Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Nearby fail");
//                Log.d(TAG, error.toString());
            }
        };

        Runnable mNearbyRunnable = new Runnable() {
            @Override
            public void run() {
                MappingDataRequestParams drp = new MappingDataRequestParams(
                        getNearbyJSON(),
                        "nearby",
                        Request.Method.GET,
                        successListener,
                        errorListener);
                request(drp);
            }

            public void request(MappingDataRequestParams dataRequestParams) {
                mQueue.add(buildObjectRequest(dataRequestParams));
            }

            JSONObject getNearbyJSON() {
                JSONObject nearbyFields = new JSONObject();
                JSONObject nearbyRequest = new JSONObject();

                try {
                    nearbyFields.put("lat", mLocationTracker.lastKnownLocation().getLatitudeInDegrees());
                    nearbyFields.put("long", mLocationTracker.lastKnownLocation().getLongitudeInDegrees());
                    nearbyRequest.put("reference_information", nearbyFields);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                return nearbyRequest;
            }
        };

        mTasks.add(new MappingDataTrackerTask(NEARBY_FREQUENCY_VALUE, NEARBY_FREQUENCY_UNIT, mNearbyRunnable));
    }

    public void scheduleAll() {
        if (!isInitialized) return;
        for(MappingDataTrackerTask t : mTasks) {
            t.schedule(mScheduler);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!isInitialized) return;
        mScheduler = null;
        isInitialized = false;

        if (!isInitialized) return;
        for(MappingDataTrackerTask t : mTasks) {
            t.cancel();
        }
        mTasks = null;
    }

    public boolean isOnline() {
        NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public void queueOfflineRequests() {
        Log.d(TAG, "Syncing data");
        if(isInitialized && isOnline()) {
            ArrayList<MappingDataRequestParams> t = (ArrayList<MappingDataRequestParams>) mOfflineRequests.clone();
            mOfflineRequests.clear();

            for(MappingDataRequestParams offlineDatum : t) {
                Log.d(TAG, "Syncing a datum");
                mQueue.add(buildObjectRequest(offlineDatum));
            }
        }
    }

    public JsonObjectRequest buildObjectRequest(final MappingDataRequestParams mappingDataRequestParams) {
        final String requestUrl = mUploadAPIBase + mappingDataRequestParams.getEndpoint();

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                mappingDataRequestParams.getMethod(),
                requestUrl,
                mappingDataRequestParams.getJsonObject(),
                mappingDataRequestParams.getSuccessListener(),
                mappingDataRequestParams.getErrorListener());

        return jsonRequest;
    }

    public JsonArrayRequest buildArrayRequest(final MappingDataRequestParams mappingDataRequestParams) {
        final String requestUrl = mUploadAPIBase + mappingDataRequestParams.getEndpoint();

        final JsonArrayRequest jsonRequest = new JsonArrayRequest(
                mappingDataRequestParams.getMethod(),
                requestUrl,
                mappingDataRequestParams.getJsonObject(),
                mappingDataRequestParams.getSuccessListener(),
                mappingDataRequestParams.getErrorListener());

        return jsonRequest;
    }
}

