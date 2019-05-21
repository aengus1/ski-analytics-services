////////////////////////////////////////////////////////////////////////////////
// The following FIT Protocol software provided may be used with FIT protocol
// devices only and remains the copyrighted property of Garmin Canada Inc.
// The software is being provided on an "as-is" basis and as an accommodation,
// and therefore all warranties, representations, or guarantees of any kind
// (whether express, implied or statutory) including, without limitation,
// warranties of merchantability, non-infringement, or fitness for a particular
// purpose, are specifically disclaimed.
//
// Copyright 2018 Garmin Canada Inc.
////////////////////////////////////////////////////////////////////////////////
// ****WARNING****  This file is auto-generated!  Do NOT edit this file.
// Profile Version = 20.78Release
// Tag = production/akw/20.78.00-0-gef63ea6
////////////////////////////////////////////////////////////////////////////////


package com.garmin.fit;

import java.util.HashMap;
import java.util.Map;

public class DeadliftExerciseName {
    public static final int BARBELL_DEADLIFT = 0;
    public static final int BARBELL_STRAIGHT_LEG_DEADLIFT = 1;
    public static final int DUMBBELL_DEADLIFT = 2;
    public static final int DUMBBELL_SINGLE_LEG_DEADLIFT_TO_ROW = 3;
    public static final int DUMBBELL_STRAIGHT_LEG_DEADLIFT = 4;
    public static final int KETTLEBELL_FLOOR_TO_SHELF = 5;
    public static final int ONE_ARM_ONE_LEG_DEADLIFT = 6;
    public static final int RACK_PULL = 7;
    public static final int ROTATIONAL_DUMBBELL_STRAIGHT_LEG_DEADLIFT = 8;
    public static final int SINGLE_ARM_DEADLIFT = 9;
    public static final int SINGLE_LEG_BARBELL_DEADLIFT = 10;
    public static final int SINGLE_LEG_BARBELL_STRAIGHT_LEG_DEADLIFT = 11;
    public static final int SINGLE_LEG_DEADLIFT_WITH_BARBELL = 12;
    public static final int SINGLE_LEG_RDL_CIRCUIT = 13;
    public static final int SINGLE_LEG_ROMANIAN_DEADLIFT_WITH_DUMBBELL = 14;
    public static final int SUMO_DEADLIFT = 15;
    public static final int SUMO_DEADLIFT_HIGH_PULL = 16;
    public static final int TRAP_BAR_DEADLIFT = 17;
    public static final int WIDE_GRIP_BARBELL_DEADLIFT = 18;
    public static final int INVALID = Fit.UINT16_INVALID;

    private static final Map<Integer, String> stringMap;

    static {
        stringMap = new HashMap<Integer, String>();
        stringMap.put(BARBELL_DEADLIFT, "BARBELL_DEADLIFT");
        stringMap.put(BARBELL_STRAIGHT_LEG_DEADLIFT, "BARBELL_STRAIGHT_LEG_DEADLIFT");
        stringMap.put(DUMBBELL_DEADLIFT, "DUMBBELL_DEADLIFT");
        stringMap.put(DUMBBELL_SINGLE_LEG_DEADLIFT_TO_ROW, "DUMBBELL_SINGLE_LEG_DEADLIFT_TO_ROW");
        stringMap.put(DUMBBELL_STRAIGHT_LEG_DEADLIFT, "DUMBBELL_STRAIGHT_LEG_DEADLIFT");
        stringMap.put(KETTLEBELL_FLOOR_TO_SHELF, "KETTLEBELL_FLOOR_TO_SHELF");
        stringMap.put(ONE_ARM_ONE_LEG_DEADLIFT, "ONE_ARM_ONE_LEG_DEADLIFT");
        stringMap.put(RACK_PULL, "RACK_PULL");
        stringMap.put(ROTATIONAL_DUMBBELL_STRAIGHT_LEG_DEADLIFT, "ROTATIONAL_DUMBBELL_STRAIGHT_LEG_DEADLIFT");
        stringMap.put(SINGLE_ARM_DEADLIFT, "SINGLE_ARM_DEADLIFT");
        stringMap.put(SINGLE_LEG_BARBELL_DEADLIFT, "SINGLE_LEG_BARBELL_DEADLIFT");
        stringMap.put(SINGLE_LEG_BARBELL_STRAIGHT_LEG_DEADLIFT, "SINGLE_LEG_BARBELL_STRAIGHT_LEG_DEADLIFT");
        stringMap.put(SINGLE_LEG_DEADLIFT_WITH_BARBELL, "SINGLE_LEG_DEADLIFT_WITH_BARBELL");
        stringMap.put(SINGLE_LEG_RDL_CIRCUIT, "SINGLE_LEG_RDL_CIRCUIT");
        stringMap.put(SINGLE_LEG_ROMANIAN_DEADLIFT_WITH_DUMBBELL, "SINGLE_LEG_ROMANIAN_DEADLIFT_WITH_DUMBBELL");
        stringMap.put(SUMO_DEADLIFT, "SUMO_DEADLIFT");
        stringMap.put(SUMO_DEADLIFT_HIGH_PULL, "SUMO_DEADLIFT_HIGH_PULL");
        stringMap.put(TRAP_BAR_DEADLIFT, "TRAP_BAR_DEADLIFT");
        stringMap.put(WIDE_GRIP_BARBELL_DEADLIFT, "WIDE_GRIP_BARBELL_DEADLIFT");
    }


    /**
     * Retrieves the String Representation of the Value
     * @return The string representation of the value, or empty if unknown
     */
    public static String getStringFromValue( Integer value ) {
        if( stringMap.containsKey( value ) ) {
            return stringMap.get( value );
        }

        return "";
    }

    /**
     * Retrieves a value given a string representation
     * @return The value or INVALID if unkwown
     */
    public static Integer getValueFromString( String value ) {
        for( Map.Entry<Integer, String> entry : stringMap.entrySet() ) {
            if( entry.getValue().equals( value ) ) {
                return entry.getKey();
            }
        }

        return INVALID;
    }

}
