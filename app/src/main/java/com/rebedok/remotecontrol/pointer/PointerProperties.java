package com.rebedok.remotecontrol.pointer;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by rebed on 17.11.2017.
 */

public class PointerProperties {
    private static String INVERT_AXIS_X = "AxisX";
    private static String INVERT_AXIS_Y = "AxisY";
    private static String CHANGE_AXIS = "ChangeAxis";
    private static String CHANGE_KEY = "ChangeKey";
    private static String DEFINITION = "Definition";

    private SharedPreferences preferences;
    private boolean invertX = false;
    private boolean invertY = false;
    private boolean changeAxis = false;
    private boolean changeKey = false;
    private int definition;
    PointerProperties(PointerActivity pointerActivity) {
        preferences = PreferenceManager
                .getDefaultSharedPreferences(pointerActivity);
        invertX = preferences.getBoolean(INVERT_AXIS_X, false);
        invertY = preferences.getBoolean(INVERT_AXIS_Y, false);
        changeAxis = preferences.getBoolean(CHANGE_AXIS, false);
        changeKey = preferences.getBoolean(CHANGE_KEY, false);
        definition = 30 + preferences.getInt(DEFINITION, 20);
    }
    
    public int getMultiplierX() {
        return invertX ? definition : - definition;
    }

    public int getMultiplierY() {
        return invertY ? - definition : definition;
    }

    public boolean isChangeAxis() {
        return changeAxis;
    }

    public String UpReleaseKey() {
        return changeKey ? "RKMRelease@" : "LKMRelease@";
    }

    public String UpPressKey() {
        return changeKey ? "RKMPress@" : "LKMPress@";
    }

    public String ReleaseKey() {
        return !changeKey ? "RKMRelease@" : "LKMRelease@";
    }

    public String PressKey() {
        return !changeKey ? "RKMPress@" : "LKMPress@";
    }
}
