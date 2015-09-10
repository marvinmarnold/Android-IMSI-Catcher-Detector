package org.stingraymappingproject.stingwatch.mapping;

import android.content.Context;

import org.stingraymappingproject.stingwatch.AppAIMSICD;
import org.stingraymappingproject.stingwatch.R;

import retrofit.Call;
import retrofit.http.GET;


/**
 * Created by Marvin Arnold on 5/09/15.
 */

public class OCIDAPIHelper {
    public static String API_URL = "http://opencellid.org/gsmCell/user/generateApiKey";

    /**
     * Get and set OCID key if undefined
     * @param context
     */
    public static void setOCIDKey(Context context) {
        // If OCID key has never been set
        if(!isOCIDKeySet(context)) {
            String ocidKey = getOCIDKey();
            if(ocidKey != null) {
                MappingPreferences.getSharedPrefences(context).edit().putString(getOCIDPrefString(context), ocidKey).commit();
            }
        }
    }

    public static boolean isOCIDKeySet(Context context) {
        return MappingPreferences.getSharedPrefences(context).getBoolean(getOCIDPrefString(context), false);
    }

    public static String getOCIDPrefString(Context context) {
        return context.getResources().getString(R.string.pref_ocid_key);
    }


    public static String getOCIDKey() {
        return "dev-usr--0d18-47c5-9e3e-6184d4b748ab"; //dev only
        /*
        // Create a very simple REST adapter which points the OCID API.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .build();

        // Create an instance of our OCID API interface.
        OCID ocid = retrofit.create(OCID.class);

        // Create a call instance for looking up Retrofit key.
        Call<OCIDKey> call = ocid.key();

        // Fetch and return OCID key
        try {
            OCIDKey key = call.execute().body();
            return key.key;
        } catch (IOException e) {
            return null;
        }*/
    }

    public static class OCIDKey {
        public final String key;

        public OCIDKey(String key) {
            this.key = key;
        }
    }

    public interface OCID {
        @GET("")
        Call<OCIDKey> key();
    }
}
