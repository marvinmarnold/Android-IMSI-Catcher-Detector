package org.stingraymappingproject.stingwatch.mapping2;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.stingraymappingproject.stingwatch.AIMSICD;
import org.stingraymappingproject.stingwatch.R;
import org.stingraymappingproject.stingwatch.mapping.MappingActivityUndetected;
import org.stingraymappingproject.stingwatch.service.AimsicdService;

/**
 * Created by Marvin Arnold on 16/08/15.
 */
public class MappingPrefActivity extends FragmentActivity {
    private final Context mContext = this;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = mContext.getSharedPreferences( AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
        loadFragment();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadFragment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!prefs.getBoolean(getResources().getString(R.string.mapping_pref_expert_key), false)) {
            Intent intent = new Intent(MappingPrefActivity.this, MappingActivityUndetected.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(MappingPrefActivity.this, AIMSICD.class);
            startActivity(intent);
        }
    }

    private void loadFragment() {
        MappingPrefFragment settingsFragment = new MappingPrefFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(android.R.id.content, settingsFragment);
        fragmentTransaction.commit();
    }
}
