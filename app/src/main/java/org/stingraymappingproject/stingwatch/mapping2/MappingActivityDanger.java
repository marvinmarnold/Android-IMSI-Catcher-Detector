package org.stingraymappingproject.stingwatch.mapping2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

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
import org.stingraymappingproject.stingwatch.utils.Status;

import io.fabric.sdk.android.Fabric;
import retrofit.Response;

/**
 * Created by Marvin Arnold on 14/08/15.
 */
public class MappingActivityDanger extends MappingActivityBase {
    private final static String TAG = "MappingActivityDanger";

    private MapView mMap;
    TwitterAuthClient mTwitterAuthClient;
    private MyLocationNewOverlay mMyLocationOverlay;
    private CompassOverlay mCompassOverlay;
    private ScaleBarOverlay mScaleBarOverlay;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
    }

    private void postToTwitter(String consumerKey, String consumerSecret) {
        String location;
//        if(mBoundToStingrayAPIService) {
//            location = mStingrayAPIService.getStingrayReadings()
//        } else {
            location = "me";
//        }
        TwitterAuthConfig authConfig =  new TwitterAuthConfig(consumerKey, consumerSecret);
        Fabric.with(this, new TwitterCore(authConfig), new TweetComposer());

        TweetComposer.Builder builder = new TweetComposer.Builder(this)
                .text("The police may be using a Stingray surveillance near " + location + " #stingraymapping");
        builder.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_mapping_danger);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_stingray_mapping);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("Threat detected");

        setupActionBar();
        setupMap();
    }

    private void setupMap() {
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

        GpsMyLocationProvider imlp = new GpsMyLocationProvider(MappingActivityDanger.this.getBaseContext());
        imlp.setLocationUpdateMinDistance(90); // [m]  // Set the minimum distance for location updates
        imlp.setLocationUpdateMinTime(15000);   // [ms] // Set the minimum time interval for location updates
        mMyLocationOverlay = new MyLocationNewOverlay(MappingActivityDanger.this.getBaseContext(), imlp, mMap);
        mMyLocationOverlay.setDrawAccuracyEnabled(true);

        mMap.getOverlays().add(mMyLocationOverlay);
        mMap.getOverlays().add(mCompassOverlay);
        mMap.getOverlays().add(mScaleBarOverlay);

        double lastLat = DEFAULT_MAP_LAT;
        double lastLong = DEFAULT_MAP_LONG;

        if(mBoundToAIMSICD && mAimsicdService.lastKnownLocation() != null) {
            GeoLocation lastLoc = mAimsicdService.lastKnownLocation();
            lastLat = lastLoc.getLatitudeInDegrees();
            lastLong = lastLoc.getLongitudeInDegrees();
        }

        // use lat, long coordinates
        if(Status.getStatus().name().equals("ALARM"))
            addMarkerToMap();

        mScaleBarOverlay.setCentred(true);
        mMap.getController().setZoom(12);
        mMap.getController().animateTo(new GeoPoint(lastLat + 0.0001, lastLong));
        mMap.invalidate();
    }

    private void addMarkerToMap() {
        double lastLat = DEFAULT_MAP_LAT;
        double lastLong = DEFAULT_MAP_LONG;
        if(mBoundToAIMSICD && mAimsicdService.lastKnownLocation() != null) {
            Log.d(TAG, "addMarkerToMap");
            GeoLocation lastLoc = mAimsicdService.lastKnownLocation();
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


    private void setupActionBar() {
        mTwitterAuthClient = new TwitterAuthClient();
        TwitterAuthConfig authConfig =  new TwitterAuthConfig("consumerKey", "consumerSecret");
        Fabric.with(this, new TwitterCore(authConfig), new TweetComposer());

        mActionToolbar = (Toolbar) findViewById(R.id.toolbar_stingray_mapping_danger_action);
        mActionToolbar.setTitle("Take Action:");
        mActionToolbar.inflateMenu(R.menu.activity_stingray_mapping_danger);
        final Activity that = this;
        mActionToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_activity_stingray_mapping_danger_airplane:
                        // read the airplane mode setting
//                        boolean isEnabled = Settings.System.getInt(
//                                getContentResolver(),
//                                Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
//
//                        // toggle airplane mode
//                        Settings.System.putInt(
//                                getContentResolver(),
//                                Settings.Global.AIRPLANE_MODE_ON, isEnabled ? 0 : 1);
//
//                        // Post an intent to reload
//                        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
//                        intent.putExtra("state", !isEnabled);
//                        sendBroadcast(intent);
                        CharSequence text = "Consider turning your phone off or putting it into Airplane Mode.";
                        int duration = Toast.LENGTH_LONG;

                        Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                        toast.show();
                        break;
                    case R.id.menu_activity_stingray_mapping_danger_twitter:
                        mTwitterAuthClient.authorize(that, new com.twitter.sdk.android.core.Callback<TwitterSession>() {

                            @Override
                            public void onResponse(Response<TwitterSession> response) {

                            }

                            @Override
                            public void onFailure(Throwable t) {

                            }

                            @Override
                            public void success(Result<TwitterSession> result) {
                                TwitterSession session = result.data;
                                TwitterAuthToken authToken = session.getAuthToken();
                                String consumerKey = authToken.token;
                                String consumerSecret = authToken.secret;

                                postToTwitter(consumerKey, consumerSecret);
                            }

                            @Override
                            public void failure(TwitterException e) {
                                e.printStackTrace();
                            }
                        });
                        break;
                    case R.id.menu_activity_stingray_mapping_danger_learn:
                        String url = getString(R.string.mapping_information_url);
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                        break;
                }
                return true;
            }
        });

        ImageView iv = (ImageView)findViewById(R.id.mapper_danger_logo);
        iv.setImageResource(R.drawable.stingwatch_danger);
    }

    @Override
    public void onResume() {
        super.onResume();
//        startActivityForThreatLevel(MappingActivityDanger.this);
        setupMap();
    }
}
