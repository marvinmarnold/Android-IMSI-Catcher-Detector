package org.stingraymappingproject.stingwatch.mapping;

import android.content.Context;
import android.content.SharedPreferences;

import org.stingraymappingproject.stingwatch.R;
import org.stingraymappingproject.stingwatch.service.AimsicdService;

/**
 * Created by Marvin Arnold on 10/09/15.
 */
public class MappingPreferences {

    public static SharedPreferences getSharedPrefences(Context context) {
        return context.getSharedPreferences(AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
    }

    public static SharedPreferences.Editor getSharedPreferencesEditor(Context context) {
        return getSharedPrefences(context).edit();
    }

    public static boolean isGoingCrazy(Context context) {
        final String isGoingCrazyString = context.getResources().getString(R.string.mapping_currently_going_crazy);
        return getSharedPrefences(context).getBoolean(isGoingCrazyString, false);
    }


    /**
     * Intro completed
     * @param context
     * @param isCompleted
     */
    public static void setIntroCompleted(Context context, boolean isCompleted) {
        String mappingIntroCompletedString = isIntroCompletedString(context);
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        editor.putBoolean(mappingIntroCompletedString, isCompleted);
        editor.apply();
    }

    private static String isIntroCompletedString(Context context) {
        return context.getResources().getString(R.string.mapping_pref_setup_complete);
    }

    public static boolean isIntroCompleted(Context context) {
        String mappingIntroCompletedString = isIntroCompletedString(context);
        return getSharedPrefences(context).getBoolean(mappingIntroCompletedString, false);
    }

    /**
     * Terms accepted
     * @param context
     * @return
     */
    private static String areTermsAcceptedString(Context context) {
        return context.getResources().getString(R.string.mapping_pref_terms_accepted);
    }

    public static boolean areTermsAccepted(Context context) {
        String termsAcceptedString = areTermsAcceptedString(context);
        return getSharedPrefences(context).getBoolean(termsAcceptedString, false);
    }

    public static void setAreTermsAccepted(Context context, boolean areTermsAccepted) {
        String termsAcceptedString = areTermsAcceptedString(context);
        SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
        editor.putBoolean(termsAcceptedString, areTermsAccepted);
        editor.apply();
    }
}
