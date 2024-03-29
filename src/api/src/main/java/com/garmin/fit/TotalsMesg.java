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


public class TotalsMesg extends Mesg {

    
    public static final int MessageIndexFieldNum = 254;
    
    public static final int TimestampFieldNum = 253;
    
    public static final int TimerTimeFieldNum = 0;
    
    public static final int DistanceFieldNum = 1;
    
    public static final int CaloriesFieldNum = 2;
    
    public static final int SportFieldNum = 3;
    
    public static final int ElapsedTimeFieldNum = 4;
    
    public static final int SessionsFieldNum = 5;
    
    public static final int ActiveTimeFieldNum = 6;
    
    public static final int SportIndexFieldNum = 9;
    

    protected static final  Mesg totalsMesg;
    static {
        // totals
        totalsMesg = new Mesg("totals", MesgNum.TOTALS);
        totalsMesg.addField(new Field("message_index", MessageIndexFieldNum, 132, 1, 0, "", false, Profile.Type.MESSAGE_INDEX));
        
        totalsMesg.addField(new Field("timestamp", TimestampFieldNum, 134, 1, 0, "s", false, Profile.Type.DATE_TIME));
        
        totalsMesg.addField(new Field("timer_time", TimerTimeFieldNum, 134, 1, 0, "s", false, Profile.Type.UINT32));
        
        totalsMesg.addField(new Field("distance", DistanceFieldNum, 134, 1, 0, "m", false, Profile.Type.UINT32));
        
        totalsMesg.addField(new Field("calories", CaloriesFieldNum, 134, 1, 0, "kcal", false, Profile.Type.UINT32));
        
        totalsMesg.addField(new Field("sport", SportFieldNum, 0, 1, 0, "", false, Profile.Type.SPORT));
        
        totalsMesg.addField(new Field("elapsed_time", ElapsedTimeFieldNum, 134, 1, 0, "s", false, Profile.Type.UINT32));
        
        totalsMesg.addField(new Field("sessions", SessionsFieldNum, 132, 1, 0, "", false, Profile.Type.UINT16));
        
        totalsMesg.addField(new Field("active_time", ActiveTimeFieldNum, 134, 1, 0, "s", false, Profile.Type.UINT32));
        
        totalsMesg.addField(new Field("sport_index", SportIndexFieldNum, 2, 1, 0, "", false, Profile.Type.UINT8));
        
    }

    public TotalsMesg() {
        super(Factory.createMesg(MesgNum.TOTALS));
    }

    public TotalsMesg(final Mesg mesg) {
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
     * Get timestamp field
     * Units: s
     *
     * @return timestamp
     */
    public DateTime getTimestamp() {
        return timestampToDateTime(getFieldLongValue(253, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD));
    }

    /**
     * Set timestamp field
     * Units: s
     *
     * @param timestamp
     */
    public void setTimestamp(DateTime timestamp) {
        setFieldValue(253, 0, timestamp.getTimestamp(), Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get timer_time field
     * Units: s
     * Comment: Excludes pauses
     *
     * @return timer_time
     */
    public Long getTimerTime() {
        return getFieldLongValue(0, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set timer_time field
     * Units: s
     * Comment: Excludes pauses
     *
     * @param timerTime
     */
    public void setTimerTime(Long timerTime) {
        setFieldValue(0, 0, timerTime, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get distance field
     * Units: m
     *
     * @return distance
     */
    public Long getDistance() {
        return getFieldLongValue(1, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set distance field
     * Units: m
     *
     * @param distance
     */
    public void setDistance(Long distance) {
        setFieldValue(1, 0, distance, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get calories field
     * Units: kcal
     *
     * @return calories
     */
    public Long getCalories() {
        return getFieldLongValue(2, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set calories field
     * Units: kcal
     *
     * @param calories
     */
    public void setCalories(Long calories) {
        setFieldValue(2, 0, calories, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get sport field
     *
     * @return sport
     */
    public Sport getSport() {
        Short value = getFieldShortValue(3, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
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
        setFieldValue(3, 0, sport.value, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get elapsed_time field
     * Units: s
     * Comment: Includes pauses
     *
     * @return elapsed_time
     */
    public Long getElapsedTime() {
        return getFieldLongValue(4, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set elapsed_time field
     * Units: s
     * Comment: Includes pauses
     *
     * @param elapsedTime
     */
    public void setElapsedTime(Long elapsedTime) {
        setFieldValue(4, 0, elapsedTime, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get sessions field
     *
     * @return sessions
     */
    public Integer getSessions() {
        return getFieldIntegerValue(5, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set sessions field
     *
     * @param sessions
     */
    public void setSessions(Integer sessions) {
        setFieldValue(5, 0, sessions, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get active_time field
     * Units: s
     *
     * @return active_time
     */
    public Long getActiveTime() {
        return getFieldLongValue(6, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set active_time field
     * Units: s
     *
     * @param activeTime
     */
    public void setActiveTime(Long activeTime) {
        setFieldValue(6, 0, activeTime, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get sport_index field
     *
     * @return sport_index
     */
    public Short getSportIndex() {
        return getFieldShortValue(9, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set sport_index field
     *
     * @param sportIndex
     */
    public void setSportIndex(Short sportIndex) {
        setFieldValue(9, 0, sportIndex, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

}
