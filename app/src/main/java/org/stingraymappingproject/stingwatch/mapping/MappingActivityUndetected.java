package org.stingraymappingproject.stingwatch.mapping;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.stingraymappingproject.api.clientandroid.models.Factoid;
import org.stingraymappingproject.stingwatch.AppAIMSICD;
import org.stingraymappingproject.stingwatch.R;

import java.util.List;

/**
 * Created by Marvin Arnold on 9/09/15.
 */
public class MappingActivityUndetected extends MappingActivityBase {
    private final static String TAG = "MappingUndetected";

    private int currentFactoid = 0;
    Handler mFactoidSwitcherHandler;
    public static final int MILISECS_BETWEEN_FACTOIDS = 8 * 1000;

    private TextView mFactoidText;
    private Button mLearnMoreButton;

    private String termsPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_mapping_safe);
        initToolbar();
        initLearnMoreButton();
        initLogo();

        if (!AppAIMSICD.areMappingTermsAccepted(this)) {

        } else {
            initFactoids();
        }
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_stingray_mapping);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("No threats detected");
    }

    private void initLearnMoreButton() {
        mLearnMoreButton = (Button) findViewById(R.id.activity_mapping_safe_learn_more_button);
        mLearnMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getString(R.string.mapping_information_url);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }

    private void initLogo() {
        ImageView iv = (ImageView)findViewById(R.id.mapper_safe_logo);
        iv.setImageResource(R.drawable.stingwatch_logo);
    }

    private void initFactoids() {
        mFactoidText = (TextView) findViewById(R.id.activity_mapping_safe_factoid);
//        loadFactoids();
        mFactoidSwitcherHandler = new Handler();
        mFactoidSwitcher.run();
    }

    Runnable mFactoidSwitcher = new Runnable() {
        @Override
        public void run() {
            if(mBoundToMapping) {
                List<Factoid> factoids = mMappingService.getFactoids();
                int numFactoids = factoids.size();
                if(++currentFactoid >= numFactoids) currentFactoid = 0;
                if(numFactoids > 0 ) mFactoidText.setText(factoids.get(currentFactoid).getFact());
            }

            mFactoidSwitcherHandler.postDelayed(mFactoidSwitcher, MILISECS_BETWEEN_FACTOIDS);
        }
    };
}
