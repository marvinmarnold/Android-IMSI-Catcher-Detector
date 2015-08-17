package com.SecUpwN.AIMSICD.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;

/**
 * Created by Marvin Arnold on 16/08/15.
 */
public class IntroSlideMappingFragment extends Fragment {

    private Drawable backgroundImage;
    private String text;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_mapping_slide_intro, container, false);

        ImageView backgroundView = (ImageView) rootView.findViewById(R.id.fragment_mapping_slide_intro_background);
        backgroundView.setImageDrawable(backgroundImage);

        TextView textView = (TextView) rootView.findViewById(R.id.fragment_mapping_slide_intro_title);
        textView.setText(text);

        return rootView;
    }

    public void setBackground(Drawable drawable) {
        backgroundImage = drawable;
    }

    public void setText(String text) {
        this.text = text;
    }
}
