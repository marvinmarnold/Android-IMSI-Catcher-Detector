/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.fragments.PrefFragment;
import com.SecUpwN.AIMSICD.mapping.MappingActivitySafe;
import com.SecUpwN.AIMSICD.service.AimsicdService;


public class PrefActivity extends BaseActivity {
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
            Intent intent = new Intent(PrefActivity.this, MappingActivitySafe.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(PrefActivity.this, AIMSICD.class);
            startActivity(intent);
        }
    }

    private void loadFragment() {
        PrefFragment settingsFragment = new PrefFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(android.R.id.content, settingsFragment);
        fragmentTransaction.commit();
    }
}