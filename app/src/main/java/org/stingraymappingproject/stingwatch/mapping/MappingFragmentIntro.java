package org.stingraymappingproject.stingwatch.mapping;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import org.stingraymappingproject.stingwatch.R;
import org.stingraymappingproject.stingwatch.service.AimsicdService;

/**
 * Created by Marvin Arnold on 10/09/15.
 */
public class MappingFragmentIntro extends Fragment {
    public static final String BACKGROUND_KEY = "background_key";
    public static final String FRAGMENT_KEY = "fragment_key";
    public static final String BUTTON_KEY = "button_key";

    private int backgroundImage;
    private int fragmentLayout;
    private boolean buttonPresent = false;

    protected SharedPreferences prefs;
    protected SharedPreferences.Editor prefsEditor;

    @Override
    public void setArguments(Bundle bundle) {
        this.fragmentLayout =  bundle.getInt(FRAGMENT_KEY);
        this.backgroundImage = bundle.getInt(BACKGROUND_KEY);
        this.buttonPresent = bundle.getBoolean(BUTTON_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                fragmentLayout, container, false);

        prefs = rootView.getContext().getSharedPreferences(AimsicdService.SHARED_PREFERENCES_BASENAME, 0);


        ImageView backgroundView = (ImageView) rootView.findViewById(R.id.fragment_mapping_slide_intro_background);
        backgroundView.setImageDrawable(getResources().getDrawable(backgroundImage));

        if(buttonPresent) {
            Button button = (Button) rootView.findViewById(R.id.fragment_mapping_slide_intro_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MappingPreferences.setIntroCompleted(getActivity(), true);
                    Intent i = new Intent(getActivity(), MappingActivityUndetected.class);
                    startActivity(i);
                }
            });
        }

        return rootView;
    }
}
