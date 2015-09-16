package org.stingraymappingproject.stingwatch.mapping;

import android.content.Context;

//import retrofit.Call;
//import retrofit.http.GET;


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
        if(!MappingPreferences.isOCIDKeySet(context)) {
            String ocidKey = getOCIDKey();
            if(ocidKey != null) {
                MappingPreferences.setIsOCIDKeySet(context, true);
                MappingPreferences.setOCIDKey(context, ocidKey);
            }
        }
    }

    public static String getOCIDKey() {
        return "dev-usr--0d18-47c5-9e3e-6184d4b748ab"; //dev only
//        // Create a very simple REST adapter which points the OCID API.
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(API_URL)
//                .build();
//
//        // Create an instance of our OCID API interface.
//        OCID ocid = retrofit.create(OCID.class);
//
//        // Create a call instance for looking up Retrofit key.
//        Call<OCIDKey> call = ocid.key();
//
//        // Fetch and return OCID key
//        try {
//            OCIDKey key = call.execute().body();
//            return key.key;
//        } catch (IOException e) {
//            return "dev-usr--0d18-47c5-9e3e-6184d4b748ab"; //dev only
////            return null; // production
//        }
    }

//    public static class OCIDKey {
//        public final String key;
//
//        public OCIDKey(String key) {
//            this.key = key;
//        }
//    }
//
//    public interface OCID {
//        @GET("")
//        Call<OCIDKey> key();
//    }
}
