package com.SecUpwN.AIMSICD;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

/**
 * Modified from AIMSICD.java
 */
public class MappingActivitySafe extends MappingActivityBase {
    private final static String TAG = "MappingActivitySafe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_mapping_safe);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_stingray_mapping);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("No threats detected");

        mActionToolbar = (Toolbar) findViewById(R.id.toolbar_stingray_mapping_safe_action);
        mActionToolbar.setTitle("Learn More:");
        mActionToolbar.inflateMenu(R.menu.activity_stingray_mapping_safe);
        mActionToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()) {
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
                    }
                })
                .setNegativeButton(R.string.text_disagree, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
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
}
