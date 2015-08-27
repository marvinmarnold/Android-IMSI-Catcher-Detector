package org.stingraymappingproject.stingwatch.mapping;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.stingraymappingproject.stingwatch.R;
import org.stingraymappingproject.stingwatch.service.CellTracker;
import org.stingraymappingproject.stingwatch.utils.AsyncResponse;
import org.stingraymappingproject.stingwatch.utils.Cell;
import org.stingraymappingproject.stingwatch.utils.Helpers;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.List;

/**
 * Modified from AIMSICD.java
 */
public class MappingActivitySafe extends MappingActivityBase implements AsyncResponse {
    private final static String TAG = "AIMSICD";
    private final static String mTAG = "MappingActivitySafe";

    private int currentFactoid = 0;
    Handler mFactoidSwitcherHandler;
    public static final int MILISECS_BETWEEN_FACTOIDS = 8 * 1000;

    private TextView mFactoidText;
    private Button mLearnMoreButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        if (!prefs.getBoolean(getResources().getString(R.string.mapping_pref_setup_complete), false)) {
            Intent intent = new Intent(MappingActivitySafe.this, IntroSlidesMappingActivity.class);
            startActivity(intent);
        }

        setContentView(R.layout.activity_mapping_safe);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_stingray_mapping);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("No threats detected");

        mLearnMoreButton = (Button) findViewById(R.id.activity_mapping_safe_learn_more_button);
        mLearnMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getString(R.string.mapping_information_url);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        ImageView iv = (ImageView)findViewById(R.id.mapper_safe_logo); 
        iv.setImageResource(R.drawable.stingwatch_logo);

        mFactoidText = (TextView) findViewById(R.id.activity_mapping_safe_factoid);
        loadFactoids();
        mFactoidSwitcherHandler = new Handler();
        mFactoidSwitcher.run();

        final String ocidKeySetPref = getResources().getString(R.string.mapping_pref_terms_accepted);
        if (!prefs.getBoolean(ocidKeySetPref, false)) {
//            OpenCellIdKeyDownloaderTask ocikd = new OpenCellIdKeyDownloaderTask();
//            ocikd.execute(); //starts background thread
        }
    }

    Runnable mFactoidSwitcher = new Runnable() {
        @Override
        public void run() {
            loadFactoids();
            if(++currentFactoid >= mFactoids.size()) currentFactoid = 0;
            mFactoidText.setText(mFactoids.get(currentFactoid).getFact());

            mFactoidSwitcherHandler.postDelayed(mFactoidSwitcher, MILISECS_BETWEEN_FACTOIDS);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        displayTerms();
    }

    private void displayTerms() {
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
                            Uri packageUri = Uri.parse("package:org.stingraymappingproject.stingwatch");
                            Intent uninstallIntent =
                                    new Intent(Intent.ACTION_DELETE, packageUri);
                            startActivity(uninstallIntent);
                            finish();
                        }
                    });

            AlertDialog disclaimerAlert = disclaimer.create();
            disclaimerAlert.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFactoidSwitcherHandler.removeCallbacks(mFactoidSwitcher);
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
