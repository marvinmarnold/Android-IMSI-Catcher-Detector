package com.SecUpwN.AIMSICD.mapping;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Modified from AIMSICD.java
 */
public class MappingActivitySafe extends MappingActivityBase {
    private final static String TAG = "MappingActivitySafe";

    private List<MappingFactoid> mFactoids;
    private TextView mFactoidText;
    private int currentFactoid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

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
                        String url = "http://www.stingray.meteor.com";
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

        final AlertDialog.Builder disclaimer = new AlertDialog.Builder(this)
                .setTitle(R.string.mapping_disclaimer_title)
                .setMessage(R.string.mapping_disclaimer)
                .setPositiveButton(R.string.text_agree, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        prefsEditor = prefs.edit();
                        prefsEditor.putBoolean("mapping_terms_accepted", true);
                        prefsEditor.apply();
                    }
                })
                .setNegativeButton(R.string.text_disagree, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        prefsEditor = prefs.edit();
                        prefsEditor.putBoolean("mapping_terms_accepted", false);
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
}
