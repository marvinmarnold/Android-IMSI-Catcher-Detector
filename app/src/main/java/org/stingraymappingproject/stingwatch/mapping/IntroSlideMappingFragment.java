package org.stingraymappingproject.stingwatch.mapping;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.stingraymappingproject.stingwatch.R;


/**
 * Created by Marvin Arnold on 16/08/15.
 */
public class IntroSlideMappingFragment extends Fragment {
    public static final String BACKGROUND_KEY = "background_key";
    public static final String FRAGMENT_KEY = "fragment_key";
    public static final String TEXT_KEY = "text_key";


    private int backgroundImage;
    private String text;
    private int fragmentLayout;

    @Override
    public void setArguments(Bundle bundle) {
        this.fragmentLayout =  bundle.getInt(FRAGMENT_KEY);
        this.backgroundImage = bundle.getInt(BACKGROUND_KEY);
        this.text =  bundle.getString(TEXT_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                fragmentLayout, container, false);

        ImageView backgroundView = (ImageView) rootView.findViewById(R.id.fragment_mapping_slide_intro_background);
        backgroundView.setImageDrawable(getResources().getDrawable(backgroundImage));

        TextView textView = (TextView) rootView.findViewById(R.id.fragment_mapping_slide_intro_title);
        textView.setText(text);

        return rootView;
    }

}
