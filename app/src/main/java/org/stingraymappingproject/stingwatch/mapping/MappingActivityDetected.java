package org.stingraymappingproject.stingwatch.mapping;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.stingraymappingproject.stingwatch.R;
import org.stingraymappingproject.stingwatch.utils.GeoLocation;

/**
 * Created by Marvin Arnold on 9/09/15.
 */
public class MappingActivityDetected extends MappingActivityBase {
    private final static String TAG = "MappingDetected";

    protected Toolbar mActionToolbar;
    private MapView mMap;
    public final static double DEFAULT_MAP_LAT = 29.951287;
    public final static double DEFAULT_MAP_LONG = -90.081102;
    private MyLocationNewOverlay mMyLocationOverlay;
    private CompassOverlay mCompassOverlay;
    private ScaleBarOverlay mScaleBarOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_mapping_danger);

        initToolbar();
        initActionBar();
        initLogo();
        initMap();

    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_stingray_mapping);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("Threat detected");
    }

    private void initActionBar() {
        mActionToolbar = (Toolbar) findViewById(R.id.toolbar_stingray_mapping_danger_action);
        mActionToolbar.setTitle("Take Action:");
        mActionToolbar.inflateMenu(R.menu.activity_stingray_mapping_danger);

        mActionToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_activity_stingray_mapping_danger_airplane:
                        handleAirplanePressed();
                        break;
                    case R.id.menu_activity_stingray_mapping_twitter:
//                        handleTwitterPressed();
                        break;
                    case R.id.menu_activity_stingray_mapping_danger_learn:
                        handleLearnPressed();
                        break;
                }
                return true;
            }
        });
    }

    private void handleAirplanePressed() {
        CharSequence text = "Consider turning your phone off or putting it into Airplane Mode.";
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(getApplicationContext(), text, duration);
        toast.show();
    }


    private void initLogo() {
        ImageView iv = (ImageView)findViewById(R.id.mapper_danger_logo);
        iv.setImageResource(R.drawable.stingwatch_danger);
    }

    private void initMap() {
        mMap = (MapView) findViewById(R.id.stingray_mapping_danger_map);

        mMap.setBuiltInZoomControls(true);
        mMap.setMultiTouchControls(true);
        mMap.setMinZoomLevel(3);
        mMap.setMaxZoomLevel(19); // Latest OSM can go to 21!
        mMap.getTileProvider().createTileCache();
        mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), mMap);

        mScaleBarOverlay = new ScaleBarOverlay(this);
        mScaleBarOverlay.setScaleBarOffset(getResources().getDisplayMetrics().widthPixels / 2, 10);
        mScaleBarOverlay.setCentred(true);

        GpsMyLocationProvider imlp = new GpsMyLocationProvider(this);
        imlp.setLocationUpdateMinDistance(90); // [m]  // Set the minimum distance for location updates
        imlp.setLocationUpdateMinTime(15000);   // [ms] // Set the minimum time interval for location updates
        mMyLocationOverlay = new MyLocationNewOverlay(this, imlp, mMap);
        mMyLocationOverlay.setDrawAccuracyEnabled(true);

        mMap.getOverlays().add(mMyLocationOverlay);
        mMap.getOverlays().add(mCompassOverlay);
        mMap.getOverlays().add(mScaleBarOverlay);

        double lastLat = DEFAULT_MAP_LAT;
        double lastLong = DEFAULT_MAP_LONG;

        if(mBoundToMapping && mMappingService.lastKnownLocation() != null) {
            GeoLocation lastLoc = mMappingService.lastKnownLocation();
            lastLat = lastLoc.getLatitudeInDegrees();
            lastLong = lastLoc.getLongitudeInDegrees();
        }

        addMarkerToMap();

        mScaleBarOverlay.setCentred(true);
        mMap.getController().setZoom(14);
        GeoPoint currentLocation = new GeoPoint(lastLat, lastLong);
        mMap.getController().setCenter(currentLocation);
        mMap.invalidate();
    }

    private void addMarkerToMap() {
        double lastLat = DEFAULT_MAP_LAT;
        double lastLong = DEFAULT_MAP_LONG;
        if(mBoundToMapping && mMappingService.lastKnownLocation() != null) {
            Log.d(TAG, "addMarkerToMap");
            GeoLocation lastLoc = mMappingService.lastKnownLocation();
            lastLat = lastLoc.getLatitudeInDegrees();
            lastLong = lastLoc.getLongitudeInDegrees();
        }

        Polygon circle = new Polygon(this);
        circle.setPoints(Polygon.pointsAsCircle(new GeoPoint(lastLat, lastLong), 550.0)); // radius = 550

        circle.setFillColor(Color.RED);
        circle.setStrokeColor(Color.RED);
        circle.setStrokeWidth(2);

        mMap.getOverlays().add(circle);
    }
}
