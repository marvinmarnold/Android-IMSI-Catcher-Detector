package com.SecUpwN.AIMSICD.mapping;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.SecUpwN.AIMSICD.service.LocationTracker;
import com.SecUpwN.AIMSICD.service.WifiTracker;
import com.SecUpwN.AIMSICD.utils.Status;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Marvin Arnold on 15/06/15.
 */
public class MappingDataTrackerService extends Service {
    private static final String TAG = "DataTrackerService";
    private final IBinder mBinder = new LocalBinder();


    private LocationTracker mLocationTracker;
    private WifiTracker mWifiTracker;
    private ConnectivityManager mConnectivityManager;

    private boolean hasScheduledUploader;
    private boolean isInitialized;
    private ScheduledExecutorService mScheduler;
    private Runnable mUploaderRunnable;
    private ScheduledFuture mUploader;
    private static final int UPLOAD_FREQUENCY_VALUE = 10;
    private static final TimeUnit UPLOAD_FREQUENCY_UNIT = TimeUnit.MINUTES;

    private final static String mUploadAPIBase ="https://stingray-mapping-server.herokuapp.com/";
    private RequestQueue mQueue;
    private ArrayList<DataRequestParams> mOfflineRequests;
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
        if (intent == null &&
            intent.getAction() != null &&
            intent.getAction().equals(ACTION_SYNC_DATA))
                syncData();
        
        return START_STICKY;
    }

    public void initUploader(LocationTracker locationTracker) {
        if(isInitialized) return;
        Context context = getApplicationContext();
        this.mLocationTracker = locationTracker;
        this.mWifiTracker = new WifiTracker(context);
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.mQueue = Volley.newRequestQueue(context);
        this.mOfflineRequests = new ArrayList<>();

        mScheduler = Executors.newScheduledThreadPool(1);
        mUploaderRunnable = new Runnable() {
            @Override
            public void run() {
                upload(new DataRequestParams(getImsiJSON(), "stingray_readings"));
            }

            public void upload(DataRequestParams dataRequestParams) {
                mQueue.add(buildRequest(dataRequestParams));
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
                }

                return datum;
            }

            JSONObject addComonFields(JSONObject jsonObject) throws JSONException {
                jsonObject.put("lat", mLocationTracker.lastKnownLocation().getLatitudeInDegrees());
                jsonObject.put("long", mLocationTracker.lastKnownLocation().getLongitudeInDegrees());
                jsonObject.put("observed_at", DateFormat.getDateTimeInstance().format(new Date()));

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

        isInitialized = true;
    }

    public void scheduleUploader() {
        Log.d(TAG, "start scheduleUploader");

        if (!isInitialized) return;
        if (hasScheduledUploader) return;

        mUploader = mScheduler.scheduleAtFixedRate(mUploaderRunnable, 0, UPLOAD_FREQUENCY_VALUE, UPLOAD_FREQUENCY_UNIT);
        hasScheduledUploader = true;
        Log.d(TAG, "end scheduleUploader");
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

    public boolean isOnline() {
        NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    private class DataRequestParams {
        public JSONObject getJsonObject() {
            return mJsonObject;
        }

        public String getEndpoint() {
            return mEndpoint;
        }

        private final JSONObject mJsonObject;
        private final String mEndpoint;

        public DataRequestParams(JSONObject jsonObject, String endpoint) {
            this.mJsonObject = jsonObject;
            this.mEndpoint = endpoint;
        }
    }

    public void syncData() {
        Log.d(TAG, "Syncing data");
        if(isInitialized && isOnline()) {
            ArrayList<DataRequestParams> t = (ArrayList<DataRequestParams>) mOfflineRequests.clone();
            mOfflineRequests.clear();

            for(DataRequestParams offlineDatum : t) {
                Log.d(TAG, "Syncing a datum");
                mQueue.add(buildRequest(offlineDatum));
            }
        }
    }

    public JsonObjectRequest buildRequest(final DataRequestParams dataRequestParams) {
        final String requestUrl = mUploadAPIBase + dataRequestParams.getEndpoint();

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.POST,
                requestUrl,
                dataRequestParams.getJsonObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Successfully uploaded data");
                        syncData();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mOfflineRequests.add(dataRequestParams);
                        Log.e(TAG, "Unsuccessful data upload to: " + requestUrl, error);
                    }
                });

        return jsonRequest;
    }
}
