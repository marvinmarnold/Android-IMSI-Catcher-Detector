package org.stingraymappingproject.stingwatch.mapping;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import org.stingraymappingproject.stingwatch.AppAIMSICD;
import org.stingraymappingproject.stingwatch.R;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Marvin Arnold on 5/09/15.
 */
public class MappingActivityBase extends AppCompatActivity {
    private final static String TAG = "MappingActivityBase";
    TwitterAuthClient mTwitterAuthClient;
    protected boolean mBoundToMapping = false;
    protected MappingService mMappingService;
    protected Toolbar mToolbar;

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

        mTwitterAuthClient = new TwitterAuthClient();
        TwitterAuthConfig authConfig = ((AppAIMSICD) getApplication()).getTwitterAuthConfig();
        Fabric.with(this, new TwitterCore(authConfig), new TweetComposer());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stingray_mapping_toolbar, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        ((AppAIMSICD) getApplication()).detach(this);
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

    @Override
    public void onStart() {
        super.onStart();
        ((AppAIMSICD) getApplication()).attach(this);
    }

    private void startMappingService() {
        Log.d(TAG, "startMappingService#" + mBoundToMapping);
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
            Log.d(TAG, "onServiceConnected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mMappingService = ((MappingService.MappingBinder) service).getService();
            mBoundToMapping = true;
            onConnectedToMappingService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG, "Service Disconnected");
            mBoundToMapping = false;
        }
    };

    protected void onConnectedToMappingService() {
        return;
    }

    protected void handleLearnPressed() {
        String url = getString(R.string.mapping_information_url);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    protected void handleTwitterPressed(final String tweet) {
        mTwitterAuthClient.authorize(this, new com.twitter.sdk.android.core.Callback<TwitterSession>() {

            @Override
            public void success(Result<TwitterSession> result) {
                TwitterSession session = result.data;
                TwitterAuthToken authToken = session.getAuthToken();
                String consumerKey = authToken.token;
                String consumerSecret = authToken.secret;

                postToTwitter(consumerKey, consumerSecret, tweet);
            }

            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
            }
        });
    }

    protected void postToTwitter(String consumerKey, String consumerSecret, String tweet) {
        TwitterAuthConfig authConfig =  new TwitterAuthConfig(consumerKey, consumerSecret);
        Fabric.with(this, new TwitterCore(authConfig), new TweetComposer());

        TweetComposer.Builder builder = new TweetComposer.Builder(this)
                .text(tweet);
        builder.show();
    }
}
