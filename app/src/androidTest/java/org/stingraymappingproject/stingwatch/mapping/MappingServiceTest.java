package org.stingraymappingproject.stingwatch.mapping;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

import org.stingraymappingproject.stingwatch.R;
import org.stingraymappingproject.stingwatch.service.AimsicdService;

/**
 * Created by Marvin Arnold on 5/09/15.
 */
public class MappingServiceTest extends ServiceTestCase<MappingService> {
    private String testKey = "test key";
    private SharedPreferences prefs;
    private Context context;
    String ocidKeySetPref;
    String ocidKey;

    public MappingServiceTest() {
        super(MappingService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testServiceTestCaseSetUpProperly();
    }

    private void resetOCIDKey() {
        context = getContext();

        prefs = context.getSharedPreferences(AimsicdService.SHARED_PREFERENCES_BASENAME, 0);

        ocidKeySetPref = context.getResources().getString(R.string.mapping_pref_terms_accepted);
        prefs.edit().putBoolean(ocidKeySetPref, false).commit();

        ocidKey = context.getString(R.string.pref_ocid_key);
        prefs.edit().putString(ocidKey, null).commit();
    }

    public void testOCIDSetOnCreate() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), MappingService.class);
        startService(startIntent);
        try {
            Thread.sleep(1000);                 //1000 milliseconds is one second.
            assertTrue(prefs.getBoolean(ocidKeySetPref, false));
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
            assertTrue(false);
        }
    }

    /**
     * Test basic startup/shutdown of Service
     */
    @SmallTest
    public void testStartable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), MappingService.class);
        startService(startIntent);
    }

    /**
     * Test binding to service
     */
    @MediumTest
    public void testBindable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), MappingService.class);
        IBinder service = bindService(startIntent);
    }
}
