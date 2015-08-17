package com.SecUpwN.AIMSICD.mapping;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import com.SecUpwN.AIMSICD.R;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

/**
 * Created by Marvin Arnold on 14/08/15.
 */
public class MappingActivityDanger extends MappingActivityBase {
    private final static String TAG = "MappingActivityDanger";

    private MapView mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_mapping_danger);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_stingray_mapping);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("Threat detected");

        mActionToolbar = (Toolbar) findViewById(R.id.toolbar_stingray_mapping_danger_action);
        mActionToolbar.setTitle("Take Action:");
        mActionToolbar.inflateMenu(R.menu.activity_stingray_mapping_danger);
        mActionToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_activity_stingray_mapping_danger_airplane:
                        // read the airplane mode setting
                        boolean isEnabled = Settings.System.getInt(
                                getContentResolver(),
                                Settings.Global.AIRPLANE_MODE_ON, 0) == 1;

                        // toggle airplane mode
                        Settings.System.putInt(
                                getContentResolver(),
                                Settings.Global.AIRPLANE_MODE_ON, isEnabled ? 0 : 1);

                        // Post an intent to reload
                        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                        intent.putExtra("state", !isEnabled);
                        sendBroadcast(intent);
                        break;
                    case R.id.menu_activity_stingray_mapping_danger_twitter:

                        break;
                }
                return true;
            }
        });

        ImageView iv = (ImageView)findViewById(R.id.mapper_danger_logo);
        iv.setImageResource(R.drawable.logo_danger);

        mMap = (MapView) findViewById(R.id.stingray_mapping_danger_map);
        mMap.getController().setZoom(16);
        mMap.setBuiltInZoomControls(true);
        mMap.setMultiTouchControls(true);
        mMap.setMinZoomLevel(3);
        mMap.setMaxZoomLevel(19);

        mMap.getController().animateTo(new GeoPoint(38.731407, -96.386617));

    }
}
