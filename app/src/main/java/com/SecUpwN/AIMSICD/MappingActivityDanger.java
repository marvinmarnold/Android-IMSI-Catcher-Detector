package com.SecUpwN.AIMSICD;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;

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
