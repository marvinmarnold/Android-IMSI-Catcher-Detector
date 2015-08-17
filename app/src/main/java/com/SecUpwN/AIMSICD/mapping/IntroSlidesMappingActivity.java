package com.SecUpwN.AIMSICD.mapping;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.SecUpwN.AIMSICD.R;

/**
 * Created by Marvin Arnold on 16/08/15.
 */
public class IntroSlidesMappingActivity extends FragmentActivity {
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 4;

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
            boolean callHappened;
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub
                if( mPageEnd && arg0 == selectedIndex && !callHappened)
                {
                    Intent i = new Intent(IntroSlidesMappingActivity.this, MappingActivityDanger.class);
                    startActivity(i);

                    Log.d(getClass().getName(), "Okay");
                    mPageEnd = false;//To avoid multiple calls.
                    callHappened = true;
                }else
                {
                    mPageEnd = false;
                }
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
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
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
            IntroSlideMappingFragment f = new IntroSlideMappingFragment();
            switch (position) {
                case 0:
                    f.setBackground(getDrawable(R.drawable.police));
                    f.setText(getString(R.string.mapping_slide_1_title));
                    break;
                case 1:
                    f.setBackground(getDrawable(R.drawable.fight));
                    f.setText(getString(R.string.mapping_slide_2_title));
                    break;
                case 2:
                    f.setBackground(getDrawable(R.drawable.stingray));
                    f.setText(getString(R.string.mapping_slide_3_title));
                    break;
                case 3:
                    f.setBackground(getDrawable(R.drawable.actions));
                    f.setText(getString(R.string.mapping_slide_4_title));
                    break;
            }
            return f;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}