package org.stingraymappingproject.stingwatch.mapping;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import org.stingraymappingproject.api.clientandroid.models.Factoid;
import org.stingraymappingproject.stingwatch.R;
import org.stingraymappingproject.stingwatch.utils.Status;

import java.util.List;

/**
 * Created by Marvin Arnold on 9/09/15.
 */
public class MappingActivityUndetected extends MappingActivityBase {
    private final static String TAG = "MappingUndetected";

    private int currentFactoid = 0;
    Handler mFactoidSwitcherHandler;
    public static final int MILISECS_BETWEEN_FACTOIDS = 8 * 1000;
    protected static final int NUM_PRELOADED_FACTOIDS = 5;

    private TextView mFactoidText;

    private List<Factoid> mFactoids;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_mapping_safe);
        initToolbar();
//        initLearnMoreButton();

        if (!MappingPreferences.areTermsAccepted(this)) {
            displayTerms();
        }
//        goCrazy();
    }

    private void goCrazy() {
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "goCrazy");
                Status.setCurrentStatus(Status.Type.ALARM, getApplicationContext());
            }
        }, 5 * 1000);
    }
    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_stingray_mapping);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("No threats detected");
    }

//    private void initLearnMoreButton() {
//        Button mLearnMoreButton = (Button) findViewById(R.id.activity_mapping_safe_learn_more_button);
//        mLearnMoreButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String url = getString(R.string.mapping_information_url);
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(url));
//                startActivity(i);
//            }
//        });
//    }


    private void displayTerms() {
        // Accept terms
        if (!MappingPreferences.areTermsAccepted(this)) {
            final AlertDialog.Builder disclaimer = new AlertDialog.Builder(this)
                    .setTitle(R.string.mapping_disclaimer_title)
                    .setMessage(R.string.mapping_disclaimer)
                    .setCancelable(false)
                    .setPositiveButton(R.string.text_agree, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            MappingPreferences.setAreTermsAccepted(getApplicationContext(), true);
                        }
                    })
                    .setNegativeButton(R.string.text_disagree, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
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


}
