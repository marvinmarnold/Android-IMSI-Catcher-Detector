package org.stingraymappingproject.stingwatch.mapping;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import org.mockito.Mockito;
import org.stingraymappingproject.stingwatch.BuildConfig;
import org.stingraymappingproject.stingwatch.R;
import org.stingraymappingproject.stingwatch.api.OCIDAPIHelper;
import org.stingraymappingproject.stingwatch.service.AimsicdService;

/**
 * Created by Marvin Arnold on 6/09/15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class OCIDAPIHelperTest extends AndroidTestCase {
    String testKey = "test Key 123";

    private SharedPreferences prefs;
    private Context context;
    String ocidKeySetPref;
    String ocidKeyPref;


    public void testSetKey() {
        resetOCIDKey();
        OCIDAPIHelper h = Mockito.mock(OCIDAPIHelper.class);
        Mockito.when(h.getKey()).thenReturn(testKey);
        h.setOCIDKey(context);

        assertTrue(prefs.getBoolean(ocidKeySetPref, false));
        assertEquals(prefs.getString(ocidKeyPref, null), testKey);
    }

    private void resetOCIDKey() {
        context = getContext();

        prefs = context.getSharedPreferences(AimsicdService.SHARED_PREFERENCES_BASENAME, 0);

        ocidKeySetPref = context.getResources().getString(R.string.mapping_pref_terms_accepted);
        prefs.edit().putBoolean(ocidKeySetPref, false).commit();

        ocidKeyPref = context.getString(R.string.pref_ocid_key);
        prefs.edit().putString(ocidKeyPref, null).commit();
    }
}
