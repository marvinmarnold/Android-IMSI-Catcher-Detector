package com.SecUpwN.AIMSICD.mapping;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.SecUpwN.AIMSICD.R;

/**
 * Created by Marvin Arnold on 16/08/15.
 */
public class MappingPrefFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.mapping_preferences);
    }

}