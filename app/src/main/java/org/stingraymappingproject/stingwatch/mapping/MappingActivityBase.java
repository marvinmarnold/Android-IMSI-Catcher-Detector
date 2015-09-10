package org.stingraymappingproject.stingwatch.mapping;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import org.stingraymappingproject.stingwatch.R;
import org.stingraymappingproject.stingwatch.mapping2.IntroSlidesMappingActivity;
import org.stingraymappingproject.stingwatch.service.AimsicdService;

/**
 * Created by Marvin Arnold on 5/09/15.
 */
public class MappingActivityBase extends AppCompatActivity {
    private final static String TAG = "MappingActivityBase";

    protected boolean mBoundToMapping = false;
    protected MappingService mMappingService;

    protected SharedPreferences prefs;
    protected SharedPreferences.Editor prefsEditor;

    protected Toolbar mToolbar;
    protected Toolbar mActionToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        prefs = getSharedPreferences(AimsicdService.SHARED_PREFERENCES_BASENAME, 0);

        startMappingService();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void startMappingService() {
        if (!mBoundToMapping) {
            // Bind to LocalService
            Intent intent = new Intent(MappingActivityBase.this, MappingService.class);
            //Start Service before binding to keep it resident when activity is destroyed
            startService(intent);
            bindService(intent, mMappingServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * Service Connection to bind the activity to the service
     */
    private final ServiceConnection mMappingServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mMappingService = ((MappingService.MappingBinder) service).getService();
            mBoundToMapping = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG, "Service Disconnected");
            mBoundToMapping = false;
        }
    };

    protected boolean mappingTermsAccepted() {
        return prefs.getBoolean(getResources().getString(R.string.mapping_pref_terms_accepted), false);
    }
    
    protected void ensureTermsAccepted() {
        if (!mappingTermsAccepted()) {
            Intent intent = new Intent(MappingActivityBase.this, IntroSlidesMappingActivity.class);
            startActivity(intent);
        }
    }
}
