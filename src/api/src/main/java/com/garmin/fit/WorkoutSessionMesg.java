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
import java.math.BigInteger;


public class WorkoutSessionMesg extends Mesg {

    
    public static final int MessageIndexFieldNum = 254;
    
    public static final int SportFieldNum = 0;
    
    public static final int SubSportFieldNum = 1;
    
    public static final int NumValidStepsFieldNum = 2;
    
    public static final int FirstStepIndexFieldNum = 3;
    
    public static final int PoolLengthFieldNum = 4;
    
    public static final int PoolLengthUnitFieldNum = 5;
    

    protected static final  Mesg workoutSessionMesg;
    static {
        // workout_session
        workoutSessionMesg = new Mesg("workout_session", MesgNum.WORKOUT_SESSION);
        workoutSessionMesg.addField(new Field("message_index", MessageIndexFieldNum, 132, 1, 0, "", false, Profile.Type.MESSAGE_INDEX));
        
        workoutSessionMesg.addField(new Field("sport", SportFieldNum, 0, 1, 0, "", false, Profile.Type.SPORT));
        
        workoutSessionMesg.addField(new Field("sub_sport", SubSportFieldNum, 0, 1, 0, "", false, Profile.Type.SUB_SPORT));
        
        workoutSessionMesg.addField(new Field("num_valid_steps", NumValidStepsFieldNum, 132, 1, 0, "", false, Profile.Type.UINT16));
        
        workoutSessionMesg.addField(new Field("first_step_index", FirstStepIndexFieldNum, 132, 1, 0, "", false, Profile.Type.UINT16));
        
        workoutSessionMesg.addField(new Field("pool_length", PoolLengthFieldNum, 132, 100, 0, "m", false, Profile.Type.UINT16));
        
        workoutSessionMesg.addField(new Field("pool_length_unit", PoolLengthUnitFieldNum, 0, 1, 0, "", false, Profile.Type.DISPLAY_MEASURE));
        
    }

    public WorkoutSessionMesg() {
        super(Factory.createMesg(MesgNum.WORKOUT_SESSION));
    }

    public WorkoutSessionMesg(final Mesg mesg) {
        super(mesg);
    }


    /**
     * Get message_index field
     *
     * @return message_index
     */
    public Integer getMessageIndex() {
        return getFieldIntegerValue(254, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set message_index field
     *
     * @param messageIndex
     */
    public void setMessageIndex(Integer messageIndex) {
        setFieldValue(254, 0, messageIndex, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get sport field
     *
     * @return sport
     */
    public Sport getSport() {
        Short value = getFieldShortValue(0, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
        if (value == null) {
            return null;
        }
        return Sport.getByValue(value);
    }

    /**
     * Set sport field
     *
     * @param sport
     */
    public void setSport(Sport sport) {
        setFieldValue(0, 0, sport.value, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get sub_sport field
     *
     * @return sub_sport
     */
    public SubSport getSubSport() {
        Short value = getFieldShortValue(1, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
        if (value == null) {
            return null;
        }
        return SubSport.getByValue(value);
    }

    /**
     * Set sub_sport field
     *
     * @param subSport
     */
    public void setSubSport(SubSport subSport) {
        setFieldValue(1, 0, subSport.value, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get num_valid_steps field
     *
     * @return num_valid_steps
     */
    public Integer getNumValidSteps() {
        return getFieldIntegerValue(2, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set num_valid_steps field
     *
     * @param numValidSteps
     */
    public void setNumValidSteps(Integer numValidSteps) {
        setFieldValue(2, 0, numValidSteps, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get first_step_index field
     *
     * @return first_step_index
     */
    public Integer getFirstStepIndex() {
        return getFieldIntegerValue(3, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set first_step_index field
     *
     * @param firstStepIndex
     */
    public void setFirstStepIndex(Integer firstStepIndex) {
        setFieldValue(3, 0, firstStepIndex, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get pool_length field
     * Units: m
     *
     * @return pool_length
     */
    public Float getPoolLength() {
        return getFieldFloatValue(4, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set pool_length field
     * Units: m
     *
     * @param poolLength
     */
    public void setPoolLength(Float poolLength) {
        setFieldValue(4, 0, poolLength, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get pool_length_unit field
     *
     * @return pool_length_unit
     */
    public DisplayMeasure getPoolLengthUnit() {
        Short value = getFieldShortValue(5, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
        if (value == null) {
            return null;
        }
        return DisplayMeasure.getByValue(value);
    }

    /**
     * Set pool_length_unit field
     *
     * @param poolLengthUnit
     */
    public void setPoolLengthUnit(DisplayMeasure poolLengthUnit) {
        setFieldValue(5, 0, poolLengthUnit.value, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

}
