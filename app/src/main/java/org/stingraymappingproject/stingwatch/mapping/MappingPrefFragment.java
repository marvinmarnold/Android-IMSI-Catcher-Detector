package org.stingraymappingproject.stingwatch.mapping;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.stingraymappingproject.stingwatch.R;
import org.stingraymappingproject.stingwatch.service.AimsicdService;

/**
 * Created by Marvin Arnold on 16/08/15.
 */
public class MappingPrefFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(AimsicdService.SHARED_PREFERENCES_BASENAME);
        addPreferencesFromResource(R.xml.mapping_preferences);
    }

}