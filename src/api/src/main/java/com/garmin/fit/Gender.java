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


public enum Gender {
    FEMALE((short)0),
    MALE((short)1),
    INVALID((short)255);

    protected short value;

    private Gender(short value) {
        this.value = value;
    }

    public static Gender getByValue(final Short value) {
        for (final Gender type : Gender.values()) {
            if (value == type.value)
                return type;
        }

        return Gender.INVALID;
    }

    /**
     * Retrieves the String Representation of the Value
     * @return The string representation of the value
     */
    public static String getStringFromValue( Gender value ) {
        return value.name();
    }

    public short getValue() {
        return value;
    }


}
