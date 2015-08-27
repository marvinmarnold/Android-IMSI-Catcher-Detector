package org.stingraymappingproject.stingwatch.mapping;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import org.stingraymappingproject.stingwatch.R;
import org.stingraymappingproject.stingwatch.service.AimsicdService;

/**
 * Created by Marvin Arnold on 16/08/15.
 */
public class IntroSlidesMappingActivity extends FragmentActivity {
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 5;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    private ViewPager.OnPageChangeListener mListener;

    protected SharedPreferences prefs;
    private final Context mContext = this;
    protected SharedPreferences.Editor prefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping_slides_intro);

        prefs = mContext.getSharedPreferences( AimsicdService.SHARED_PREFERENCES_BASENAME, 0);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        mListener = new ViewPager.OnPageChangeListener() {
            int selectedIndex;
            boolean mPageEnd;

            @Override
            public void onPageSelected(int arg0) {
                // TODO Auto-generated method stub
                selectedIndex = arg0;

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub
                if(selectedIndex == mPagerAdapter.getCount() - 1)
                {
                    mPageEnd = true;
                }
            }
        };

        mPager.addOnPageChangeListener(mListener);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }


    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f = new IntroSlideMappingFragment();
            Bundle b = new Bundle();
            switch (position) {
                case 0:
                    b.putInt(IntroSlideMappingFragment.FRAGMENT_KEY, R.layout.fragment_mapping_slide_intro);
                    b.putInt(IntroSlideMappingFragment.BACKGROUND_KEY, R.drawable.stingwatch);
                    b.putString(IntroSlideMappingFragment.TEXT_KEY, getString(R.string.mapping_slide_0_title));
                    break;
                case 1:
                    b.putInt(IntroSlideMappingFragment.FRAGMENT_KEY, R.layout.fragment_mapping_slide_intro);
                    b.putInt(IntroSlideMappingFragment.BACKGROUND_KEY, R.drawable.cameras_light);
                    b.putString(IntroSlideMappingFragment.TEXT_KEY, getString(R.string.mapping_slide_1_title));
                    break;
                case 2:
                    b.putInt(IntroSlideMappingFragment.FRAGMENT_KEY, R.layout.fragment_mapping_slide_intro);
                    b.putInt(IntroSlideMappingFragment.BACKGROUND_KEY, R.drawable.stingray);
                    b.putString(IntroSlideMappingFragment.TEXT_KEY, getString(R.string.mapping_slide_2_title));
                    break;
                case 3:
                    b.putInt(IntroSlideMappingFragment.FRAGMENT_KEY, R.layout.fragment_mapping_slide_3);
                    b.putInt(IntroSlideMappingFragment.BACKGROUND_KEY, R.drawable.cop_car);
                    b.putString(IntroSlideMappingFragment.TEXT_KEY, getString(R.string.mapping_slide_3_title));
                    break;
                case 4:
                    b.putInt(IntroSlideMappingFragment.FRAGMENT_KEY, R.layout.fragment_mapping_slide_4);
                    b.putInt(IntroSlideMappingFragment.BACKGROUND_KEY, R.drawable.cameras_dark);
                    b.putString(IntroSlideMappingFragment.TEXT_KEY, getString(R.string.mapping_slide_4_title));
                    b.putBoolean(IntroSlideMappingFragment.BUTTON_KEY, true);
                    break;
            }
            f.setArguments(b);
            return f;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}