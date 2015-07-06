package com.SecUpwN.AIMSICD.adapters;

/**
 * Created by Marvin Arnold on 1/07/15.
 */
public class NewAIMSICDDbAdapter {
    public static final int DATABASE_VERSION = 10; // Is this "pragma user_version;" ?
    private static final String TESTING_PREFIX = "new";
    private final String TAG = TESTING_PREFIX + "AIMSICD";
    private final String mTAG = TESTING_PREFIX + "DbAdapter";
    private final String DB_NAME = TESTING_PREFIX + "aimsicd.db";

    // Internal
    private static final String TABLE_INTERNAL_BASESTATIONS = "DBi_bts"; // physical
    private static final String TABLE_INTERNAL_MEASUREMENTS = "DBi_measures"; // volatile
    private static final String TABLE_INTERNAL_SECTOR_TYPES = "SectorType";


}
