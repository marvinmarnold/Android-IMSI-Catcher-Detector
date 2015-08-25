package com.SecUpwN.AIMSICD.mapping;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.SecUpwN.AIMSICD.AppAIMSICD;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.android.volley.VolleyError;

import org.stingraymappingproject.api.clientandroid.RecurringRequest;
import org.stingraymappingproject.api.clientandroid.activities.BaseStingrayActivity;
import org.stingraymappingproject.api.clientandroid.models.Factoid;
import org.stingraymappingproject.api.clientandroid.requesters.FactoidsRequester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Marvin Arnold on 15/08/15.
 */
public class MappingActivityBase extends BaseStingrayActivity {
    private final static String TAG = "MappingActivityBase";
    private static final int UPLOAD_FREQUENCY_VALUE = 20;
    private static final TimeUnit UPLOAD_FREQUENCY_UNIT = TimeUnit.SECONDS;

    private static final int FACTOIDS_FREQUENCY_VALUE = 10;
    private static final TimeUnit FACTOIDS_FREQUENCY_UNIT = TimeUnit.SECONDS;
    protected static final int NUM_PRELOADED_FACTOIDS = 5;

    private static final int NEARBY_FREQUENCY_VALUE = 30;
    private static final TimeUnit NEARBY_FREQUENCY_UNIT = TimeUnit.MINUTES;

    protected Toolbar mToolbar;
    protected Toolbar mActionToolbar;

    private final Context mContext = this;

    protected boolean mBoundToAIMSICD;
    protected AimsicdService mAimsicdService;

    protected SharedPreferences prefs;
    protected SharedPreferences.Editor prefsEditor;

    protected List<Factoid> mFactoids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

    private void startAIMSICDService() {
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
    }

    public void scheduleFactoidsRequester() {
        FactoidsRequester factoidsRequester = new FactoidsRequester(mStingrayAPIService) {
            @Override
            public void onResponse(Factoid[] response) {
                Log.d(TAG, "onResponse");
                if(response.length > 0) {
                    mFactoids = new ArrayList<>();
                    mFactoids = Arrays.asList(response);
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

    protected void loadFactoids() {
        if(mFactoids == null || mFactoids.isEmpty()) mFactoids = new ArrayList<Factoid>();
        if(mFactoids.isEmpty()) {
            for (int i = 0; i < NUM_PRELOADED_FACTOIDS; i++) {
                Factoid factoid = createPreloadedFactoid(mContext, i);
                mFactoids.add(factoid);
            }
        }
    }
}
