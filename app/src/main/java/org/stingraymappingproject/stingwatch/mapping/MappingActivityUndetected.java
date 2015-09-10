package org.stingraymappingproject.stingwatch.mapping;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.stingraymappingproject.api.clientandroid.models.Factoid;
import org.stingraymappingproject.stingwatch.R;

import java.util.ArrayList;
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
    private Button mLearnMoreButton;

    private String termsPref;
    private List<Factoid> mFactoids;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_mapping_safe);
        initToolbar();
        initLearnMoreButton();
        initLogo();

        if (!MappingPreferences.areTermsAccepted(this)) {
            displayTerms();
        } else {
            initFactoids();
        }
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_stingray_mapping);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("No threats detected");
    }

    private void initLearnMoreButton() {
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
    }

    private void initLogo() {
        ImageView iv = (ImageView)findViewById(R.id.mapper_safe_logo);
        iv.setImageResource(R.drawable.stingwatch_logo);
    }

    private void initFactoids() {
        mFactoidText = (TextView) findViewById(R.id.activity_mapping_safe_factoid);
        loadFactoids();
        mFactoidSwitcherHandler = new Handler();
        mFactoidSwitcher.run();
    }

    Runnable mFactoidSwitcher = new Runnable() {
        @Override
        public void run() {
            if(mBoundToMapping) {
                List<Factoid> factoids = mMappingService.getFactoids();
                int numFactoids = factoids.size();
                if(++currentFactoid >= numFactoids) currentFactoid = 0;
                if(numFactoids > 0 ) mFactoidText.setText(factoids.get(currentFactoid).getFact());
            }

            mFactoidSwitcherHandler.postDelayed(mFactoidSwitcher, MILISECS_BETWEEN_FACTOIDS);
        }
    };

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
//                            startActivityForThreatLevel(MappingActivitySafe.this);
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

    private void loadFactoids() {
        if(mFactoids == null) mFactoids = new ArrayList<>();
        if(mBoundToMapping) {
            List<Factoid> factoids = mMappingService.getFactoids();
            if (!factoids.isEmpty()) {
                mFactoids = factoids;
                return;
            }
        }
        loadLocalFactoids();
    }

    private void loadLocalFactoids() {
        for (int i = 0; i < NUM_PRELOADED_FACTOIDS; i++) {
            mFactoids.add(createPreloadedFactoid(i));
        }
    }

    public Factoid createPreloadedFactoid(int n) {
        Log.d(TAG, "created a factoid");
        if(n < 0 || n >= NUM_PRELOADED_FACTOIDS) return null;
        switch(n) {
            case 0:
                return new Factoid(getString(R.string.mapping_factoids_1));
            case 1:
                return new Factoid(getString(R.string.mapping_factoids_2));
            case 2:
                return new Factoid(getString(R.string.mapping_factoids_3));
            case 3:
                return new Factoid(getString(R.string.mapping_factoids_4));
            case 4:
                return new Factoid(getString(R.string.mapping_factoids_5));
        }
        return null;
    }
}
