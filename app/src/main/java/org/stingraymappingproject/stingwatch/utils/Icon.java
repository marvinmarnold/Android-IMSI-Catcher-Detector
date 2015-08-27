/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package org.stingraymappingproject.stingwatch.utils;

import org.stingraymappingproject.stingwatch.R;
/**
 * Class that holds and returns the correct icon based on requested icon format and
 * current system status.
 *
 * @author Tor Henning Ueland
 */
public class Icon {
    public enum Type {
        FLAT,
        SENSE,
        WHITE,
    }

    /*
     * Returns a icon of the Type $t, what kind of icon is returned is decided
     * from what the current status is.
     */
    // TODO: Seem we're missing the other colors here: ORANGE and BLACK (skull)
    // See: https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Status-Icons
    // Change names from "IDLE,NORMAL,MEDIUM,ALARM" to:"GRAY,GREEN,YELLOW,ORANGE,RED,BLACK",
    // to reflect detection Icon colors.
    // Dependencies:  Status.java, CellTracker.java, Icon.java ( + others?)
    // They should be based on the detection scores here: <TBA>
    // -- E:V:A 2015-01-19
    public static int getIcon(Type t) {
        switch(t) {
            case FLAT:
                switch (Status.getStatus()) {
                    case IDLE:
                        return R.drawable.stingwatch_safe;

                    case NORMAL:
                        return R.drawable.stingwatch_safe;

                    case MEDIUM:
                        return R.drawable.stingwatch_safe;

                    case ALARM:
                        return R.drawable.stingwatch_danger;

                    default:
                        return R.drawable.stingwatch_safe;
                }

            case SENSE:
                switch (Status.getStatus()) {
                    case IDLE:
                        return R.drawable.stingwatch_safe;

                    case NORMAL:
                        return R.drawable.stingwatch_safe;

                    case MEDIUM:
                        return R.drawable.stingwatch_safe;

                    case ALARM:
                        return R.drawable.stingwatch_danger;

                    default:
                        return R.drawable.stingwatch_safe;
                }

            case WHITE:
                switch (Status.getStatus()) {
                    case IDLE:
                        return R.drawable.stingwatch_safe;

                    case NORMAL:
                        return R.drawable.stingwatch_safe;

                    case MEDIUM:
                        return R.drawable.stingwatch_safe;

                    case ALARM:
                        return R.drawable.stingwatch_danger;

                    default:
                        return R.drawable.stingwatch_safe;
                }
        }
        return -1;
    }
}
