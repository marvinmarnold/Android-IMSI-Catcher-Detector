package com.SecUpwN.AIMSICD.mapping;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import com.SecUpwN.AIMSICD.R;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Marvin Arnold on 14/08/15.
 */
public class MappingActivityDanger extends MappingActivityBase {
    private final static String TAG = "MappingActivityDanger";

    private MapView mMap;
    TwitterLoginButton mTwitterLoginButton;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        mTwitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    private void postToTwitter(String consumerKey, String consumerSecret) {
        TwitterAuthConfig authConfig =  new TwitterAuthConfig(consumerKey, consumerSecret);
        Fabric.with(this, new TwitterCore(authConfig), new TweetComposer());

        TweetComposer.Builder builder = new TweetComposer.Builder(this)
                .text("down with stingrays");
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_mapping_danger);

        mTwitterLoginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        mTwitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterSession session = result.data;
                TwitterAuthToken authToken = session.getAuthToken();
                String consumerKey = authToken.token;
                String consumerSecret = authToken.secret;

                postToTwitter(consumerKey, consumerSecret);
            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
            }
        });

        mToolbar = (Toolbar) findViewById(R.id.toolbar_stingray_mapping);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("Threat detected");

        TwitterAuthConfig authConfig =  new TwitterAuthConfig("consumerKey", "consumerSecret");
        Fabric.with(this, new TwitterCore(authConfig), new TweetComposer());

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
