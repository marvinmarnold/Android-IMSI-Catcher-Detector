package org.stingraymappingproject.stingwatch.mapping;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import org.stingraymappingproject.stingwatch.R;
import org.stingraymappingproject.stingwatch.utils.Status;

/**
 * Created by Marvin Arnold on 9/09/15.
 */
public class MappingActivityUndetected extends MappingActivityBase {
    private final static String TAG = "MappingUndetected";

    protected Toolbar mActionToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_mapping_undetected);
        initToolbar();
        initActionBar();

        if (!MappingPreferences.areTermsAccepted(this)) {
            displayTerms();
        }
        goCrazy();

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

    private void initActionBar() {
        mActionToolbar = (Toolbar) findViewById(R.id.action_toolbar_stingray_mapping_undetected);
        mActionToolbar.setTitle("Take Action:");
        mActionToolbar.inflateMenu(R.menu.activity_stingray_mapping_undetected);

        mActionToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_activity_stingray_mapping_twitter:
                   String tweet = "Local police are using military grade surveillance technology against citizens. stingraymappingproject.org #stingraymapping";
                    handleTwitterPressed(tweet);
                    break;
                case R.id.menu_activity_stingray_mapping_undetected_learn:
                    handleLearnPressed();
                    break;
            }
            return true;
            }
        });
    }

}
