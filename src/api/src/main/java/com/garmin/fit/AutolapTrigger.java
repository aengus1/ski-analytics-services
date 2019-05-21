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


public enum AutolapTrigger {
    TIME((short)0),
    DISTANCE((short)1),
    POSITION_START((short)2),
    POSITION_LAP((short)3),
    POSITION_WAYPOINT((short)4),
    POSITION_MARKED((short)5),
    OFF((short)6),
    INVALID((short)255);

    protected short value;

    private AutolapTrigger(short value) {
        this.value = value;
    }

    public static AutolapTrigger getByValue(final Short value) {
        for (final AutolapTrigger type : AutolapTrigger.values()) {
            if (value == type.value)
                return type;
        }

        return AutolapTrigger.INVALID;
    }

    /**
     * Retrieves the String Representation of the Value
     * @return The string representation of the value
     */
    public static String getStringFromValue( AutolapTrigger value ) {
        return value.name();
    }

    public short getValue() {
        return value;
    }


}
