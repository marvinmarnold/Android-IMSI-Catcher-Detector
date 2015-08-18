package com.SecUpwN.AIMSICD.mapping;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.service.CellTracker;
import com.SecUpwN.AIMSICD.utils.AsyncResponse;
import com.SecUpwN.AIMSICD.utils.Cell;
import com.SecUpwN.AIMSICD.utils.Helpers;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Modified from AIMSICD.java
 */
public class MappingActivitySafe extends MappingActivityBase implements AsyncResponse {
    private final static String TAG = "AIMSICD";
    private final static String mTAG = "MappingActivitySafe";
    private final Context mContext = this;

    private List<MappingFactoid> mFactoids;
    private TextView mFactoidText;
    private int currentFactoid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        if (!prefs.getBoolean(getResources().getString(R.string.mapping_pref_setup_complete), false)) {
            Intent intent = new Intent(MappingActivitySafe.this, IntroSlidesMappingActivity.class);
            startActivity(intent);
        }

        setContentView(R.layout.activity_mapping_safe);

        loadFactoids();

        mToolbar = (Toolbar) findViewById(R.id.toolbar_stingray_mapping);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("No threats detected");

        mActionToolbar = (Toolbar) findViewById(R.id.toolbar_stingray_mapping_safe_action);
        mActionToolbar.setTitle("Learn More:");
        mActionToolbar.inflateMenu(R.menu.activity_stingray_mapping_safe);
        mActionToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_activity_stingray_mapping_safe_learn:
                        String url = "https://stingraymappingproject.org";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                        break;
                }
                return true;
            }
        });

        ImageView iv = (ImageView)findViewById(R.id.mapper_safe_logo); 
        iv.setImageResource(R.drawable.logo_safe);

        // Accept terms
        final String termsPref = getResources().getString(R.string.mapping_pref_terms_accepted);
        if (!prefs.getBoolean(termsPref, false)) {
            final AlertDialog.Builder disclaimer = new AlertDialog.Builder(this)
                    .setTitle(R.string.mapping_disclaimer_title)
                    .setMessage(R.string.mapping_disclaimer)
                    .setPositiveButton(R.string.text_agree, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            prefsEditor = prefs.edit();
                            prefsEditor.putBoolean(termsPref, true);
                            prefsEditor.apply();
                        }
                    })
                    .setNegativeButton(R.string.text_disagree, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            prefsEditor = prefs.edit();
                            prefsEditor.putBoolean(termsPref, false);
                            prefsEditor.apply();
                            Uri packageUri = Uri.parse("package:com.SecUpwN.AIMSICD");
                            Intent uninstallIntent =
                                    new Intent(Intent.ACTION_DELETE, packageUri);
                            startActivity(uninstallIntent);
                            finish();
//                        if (mAimsicdService != null) mAimsicdService.onDestroy();
                        }
                    });

            AlertDialog disclaimerAlert = disclaimer.create();
            disclaimerAlert.show();
        }

        final String ocidKeySetPref = getResources().getString(R.string.mapping_pref_terms_accepted);
        if (!prefs.getBoolean(ocidKeySetPref, false)) {
//            OpenCellIdKeyDownloaderTask ocikd = new OpenCellIdKeyDownloaderTask();
//            ocikd.execute(); //starts background thread
        }
    }

    Handler mFactoidSwitcherHandler;
    public static final int MILISECS_BETWEEN_FACTOIDS = 4 * 1000;
    private void loadFactoids() {
        mFactoidText = (TextView) findViewById(R.id.activity_mapping_safe_factoid);
        mFactoids = new ArrayList<>();
//        for(int i = 1; i <= MappingFactoid.NUM_PRELOADED_FACTOIDS; i++) {
//            MappingFactoid factoid = MappingFactoid.createPreloadedFactoid(getApplicationContext(), i);
//            mFactoids.add(factoid);
//        }

        mFactoidSwitcherHandler = new Handler();
        mFactoidSwitcher.run();
    }

    Runnable mFactoidSwitcher = new Runnable() {
        @Override
        public void run() {
            currentFactoid++;
            int numFactoids;

            if(usingPreloadedFactoids()) numFactoids = MappingFactoid.NUM_PRELOADED_FACTOIDS;
            else numFactoids = mFactoids.size();

            if(currentFactoid >= numFactoids) currentFactoid = 0;

            if(usingPreloadedFactoids()) mFactoidText.setText(MappingFactoid.createPreloadedFactoid(getApplicationContext(), currentFactoid).getText());
            else mFactoidText.setText(mFactoids.get(currentFactoid).getText());

            mFactoidSwitcherHandler.postDelayed(mFactoidSwitcher, MILISECS_BETWEEN_FACTOIDS);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFactoidSwitcherHandler.removeCallbacks(mFactoidSwitcher);
    }

    private boolean usingPreloadedFactoids() {
        return mFactoids.size() == 0;
    }

    /**
     * Background thread to send and parse response from OCID
     */
    private class OpenCellIdKeyDownloaderTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                return requestNewOCIDKey();
            } catch (final Exception e) {
                Log.e(TAG, mTAG + ": " + e.getMessage());
                e.printStackTrace();

                /**
                 * In case response from OCID takes more time and user pressed back or anything else,
                 * application will crash due to 'UI modification from background thread, starting new
                 * runOnUIThread will prevent it.
                 */
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Helpers.msgLong(MappingActivitySafe.this, getString(R.string.ocid_api_error) +
                                e.getClass().getName() + " - " + e.getMessage());
                        finish();
                    }
                });

                //finish(); TODO should this finish be here or in runOnUiThread or should it even be in here at all??
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s == null || s.isEmpty())
                return;

            // Check key validity (is done on foreign server)
            if (isKeyValid(s)) {
                String ocidKeySetPref = getResources().getString(R.string.mapping_pref_terms_accepted);
                prefs.edit().putBoolean(ocidKeySetPref, true).commit();

                String opcidKey = getString(R.string.pref_ocid_key);
                prefs.edit().putString(opcidKey, s).commit();
                CellTracker.OCID_API_KEY = s;

                Helpers.msgShort(MappingActivitySafe.this, getString(R.string.ocid_api_success));

            } else if(s.contains("Error: You can not register new account")){
                Helpers.msgLong(getApplicationContext(), getString(R.string.only_one_key_per_day));

                // TODO: Add more and better toast messages here

            } else if(s.contains("Bad Request")){
                Helpers.msgShort(MappingActivitySafe.this, "Bad Request 400, 403 or 500 error ");

            } else {
                Helpers.msgShort(MappingActivitySafe.this, "Unknown error please view logcat");
            }

            finish();
        }

        // This might be extended in the future.
        // Newly obtained keys start with: "dev-usr", not sure if that's a rule.
        private boolean isKeyValid(String key) {
            return key.startsWith("dev-");
        }


        /**
         *
         * Description:     Get an API key for Open Cell ID. Do not call this from the UI/Main thread.
         *                  For the various server responses, pleas refer to the OpenCellID API wiki:
         *                  http://wiki.opencellid.org/wiki/API#Error_codes
         *                  TODO: And the github issue #303:
         *                  https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/303
         *
         *  TODO:   [ ] Add handlers for other HTTP request and OCID Server error codes:
         *
         *      OCID status codes http://wiki.opencellid.org/wiki/API#Error_codes
         *      1 	200 	Cell not found
         *      2 	401 	Invalid API key
         *      3 	400 	Invalid input data
         *      4 	403     Your API key must be white listed in order to run this operation
         *      5 	500 	Internal server error
         *      6 	503 	Too many requests. Try later again
         *      7 	429     Daily limit 1000 requests exceeded for your API key.
         *
         * @return null or newly generated key
         */
        public  String requestNewOCIDKey() throws Exception {

            // @banjaxobanjo Did you remove it??  --EVA  (If yes, remove these comments!!)
            //String htmlResponse = EntityUtils.toString(response.getEntity(), "UTF-8");

            HttpGet httpRequest = new HttpGet(getString(R.string.opencellid_api_get_key));
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(httpRequest);

            int responseCode = response.getStatusLine().getStatusCode();

            String htmlResponse = EntityUtils.toString(response.getEntity(), "UTF-8");

            // For debugging HTTP server response and codes
            Log.d(TAG,mTAG+ " Response Html=" + htmlResponse + " Response Code=" + String.valueOf(responseCode));

            if (responseCode == 200) {
                Log.d(TAG, mTAG + ": OCID Code 1: Cell Not found: " + htmlResponse);
                return htmlResponse;

            } else if (responseCode == 401) {
                Log.d(TAG, mTAG + ": OCID Code 2: Invalid API Key! :" + htmlResponse);
                return htmlResponse;

            } else if(responseCode == 400){
                Log.d(TAG, mTAG + ": OCID Code 3: Invalid input data: " + htmlResponse);
                return "Bad Request"; // For making a toast!

            } else if (responseCode == 403) {
                Log.d(TAG, mTAG + ": OCID Code 4:  Your API key must be white listed: " + htmlResponse);
                return "Bad Request"; // For making a toast!
                //return htmlResponse;

            } else if(responseCode == 500){
                Log.d(TAG, mTAG + ": OCID Code 5: Remote internal server error: " + htmlResponse);
                return "Bad Request"; // For making a toast!

            } else if (responseCode == 503) {
                Log.d(TAG, mTAG + ": OCID Code 6: Reached 24hr API key request limit: " + htmlResponse);
                return htmlResponse;

            } else if(responseCode == 429){
                Log.d(TAG, mTAG + ": OCID Code 7: Exceeded daily request limit (1000) for your API key: " + htmlResponse);
                return htmlResponse;

            } else {

                // TODO add code here or elsewhere to check for NO network exceptions...
                // See: https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/293

                Log.d(TAG, mTAG + ": OCID Returned Unknown Response: " + responseCode);
                //throw new Exception("OCID Returned " + status.getStatusCode() + " " + status.getReasonPhrase());
                return null;
            }
        }
    }

    @Override
    public void processFinish(float[] location) {
    }

    @Override
    public void processFinish(List<Cell> cells) {
    }
}
