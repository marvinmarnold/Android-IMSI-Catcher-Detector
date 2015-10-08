/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package org.stingraymappingproject.stingwatch.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.stingraymappingproject.stingwatch.R;
import org.stingraymappingproject.stingwatch.service.AimsicdService;

public class PrefFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(AimsicdService.SHARED_PREFERENCES_BASENAME);

        addPreferencesFromResource(R.xml.preferences);
    }

}