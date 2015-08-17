package com.SecUpwN.AIMSICD.mapping;

import android.content.Context;

import com.SecUpwN.AIMSICD.R;

/**
 * Created by Marvin Arnold on 17/08/15.
 */
public class MappingFactoid {
    public static final int NUM_PRELOADED_FACTOIDS = 5;
    public static final int MILISECS_BETWEEN_FACTOIDS = 4 * 1000;

    public String getText() {
        return text;
    }

    private final String text;

    public MappingFactoid(String text) {
        this.text = text;
    }

    public static MappingFactoid createPreloadedFactoid(Context context, int n) {
        if(n < 1 || n > NUM_PRELOADED_FACTOIDS) return null;
        switch(n) {
            case 1:
                return new MappingFactoid(context.getString(R.string.mapping_factoids_1));
            case 2:
                return new MappingFactoid(context.getString(R.string.mapping_factoids_2));
            case 3:
                return new MappingFactoid(context.getString(R.string.mapping_factoids_3));
            case 4:
                return new MappingFactoid(context.getString(R.string.mapping_factoids_4));
            case 5:
                return new MappingFactoid(context.getString(R.string.mapping_factoids_5));
        }
        return null;
    }
}
