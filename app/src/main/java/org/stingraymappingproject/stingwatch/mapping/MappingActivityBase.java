package org.stingraymappingproject.stingwatch.mapping;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

/**
 * Created by Marvin Arnold on 5/09/15.
 */
public class MappingActivityBase extends AppCompatActivity {
    private final static String TAG = "MappingActivityBase";

    protected boolean mBoundToMapping = false;
    protected MappingService mMappingService;

    protected Toolbar mToolbar;
    protected Toolbar mActionToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // Users must go through intro slides on first run
        if (!MappingPreferences.isIntroCompleted(this)) {
            Intent intent = new Intent(MappingActivityBase.this, MappingActivityIntro.class);
            startActivity(intent);
        } else {
            startMappingService();
        }
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


}
