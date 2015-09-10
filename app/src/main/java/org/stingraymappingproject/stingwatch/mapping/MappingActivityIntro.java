package org.stingraymappingproject.stingwatch.mapping;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import org.stingraymappingproject.stingwatch.R;

/**
 * Created by Marvin Arnold on 10/09/15.
 */
public class MappingActivityIntro extends FragmentActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping_slides_intro);

        initPager();

    }

    private void initPager() {
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
            Fragment f = new MappingFragmentIntro();
            Bundle b = new Bundle();
            switch (position) {
                case 0:
                    b.putInt(MappingFragmentIntro.FRAGMENT_KEY, R.layout.mapping_fragment_intro);
                    b.putInt(MappingFragmentIntro.BACKGROUND_KEY, R.drawable.stingwatch);
                    break;
                case 1:
                    b.putInt(MappingFragmentIntro.FRAGMENT_KEY, R.layout.mapping_fragment_intro);
                    b.putInt(MappingFragmentIntro.BACKGROUND_KEY, R.drawable.cameras_light);
                    break;
                case 2:
                    b.putInt(MappingFragmentIntro.FRAGMENT_KEY, R.layout.mapping_fragment_intro);
                    b.putInt(MappingFragmentIntro.BACKGROUND_KEY, R.drawable.stingray);
                    break;
                case 3:
                    b.putInt(MappingFragmentIntro.FRAGMENT_KEY, R.layout.mapping_fragment_intro);
                    b.putInt(MappingFragmentIntro.BACKGROUND_KEY, R.drawable.cop_car);
                    break;
                case 4:
                    b.putInt(MappingFragmentIntro.FRAGMENT_KEY, R.layout.mapping_fragment_intro_final);
                    b.putInt(MappingFragmentIntro.BACKGROUND_KEY, R.drawable.cameras_dark);
                    b.putBoolean(MappingFragmentIntro.BUTTON_KEY, true);
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
