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


public class SegmentPointMesg extends Mesg {

    
    public static final int MessageIndexFieldNum = 254;
    
    public static final int PositionLatFieldNum = 1;
    
    public static final int PositionLongFieldNum = 2;
    
    public static final int DistanceFieldNum = 3;
    
    public static final int AltitudeFieldNum = 4;
    
    public static final int LeaderTimeFieldNum = 5;
    

    protected static final  Mesg segmentPointMesg;
    static {
        // segment_point
        segmentPointMesg = new Mesg("segment_point", MesgNum.SEGMENT_POINT);
        segmentPointMesg.addField(new Field("message_index", MessageIndexFieldNum, 132, 1, 0, "", false, Profile.Type.MESSAGE_INDEX));
        
        segmentPointMesg.addField(new Field("position_lat", PositionLatFieldNum, 133, 1, 0, "semicircles", false, Profile.Type.SINT32));
        
        segmentPointMesg.addField(new Field("position_long", PositionLongFieldNum, 133, 1, 0, "semicircles", false, Profile.Type.SINT32));
        
        segmentPointMesg.addField(new Field("distance", DistanceFieldNum, 134, 100, 0, "m", false, Profile.Type.UINT32));
        
        segmentPointMesg.addField(new Field("altitude", AltitudeFieldNum, 132, 5, 500, "m", false, Profile.Type.UINT16));
        
        segmentPointMesg.addField(new Field("leader_time", LeaderTimeFieldNum, 134, 1000, 0, "s", false, Profile.Type.UINT32));
        
    }

    public SegmentPointMesg() {
        super(Factory.createMesg(MesgNum.SEGMENT_POINT));
    }

    public SegmentPointMesg(final Mesg mesg) {
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
     * Get position_lat field
     * Units: semicircles
     *
     * @return position_lat
     */
    public Integer getPositionLat() {
        return getFieldIntegerValue(1, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set position_lat field
     * Units: semicircles
     *
     * @param positionLat
     */
    public void setPositionLat(Integer positionLat) {
        setFieldValue(1, 0, positionLat, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get position_long field
     * Units: semicircles
     *
     * @return position_long
     */
    public Integer getPositionLong() {
        return getFieldIntegerValue(2, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set position_long field
     * Units: semicircles
     *
     * @param positionLong
     */
    public void setPositionLong(Integer positionLong) {
        setFieldValue(2, 0, positionLong, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get distance field
     * Units: m
     * Comment: Accumulated distance along the segment at the described point
     *
     * @return distance
     */
    public Float getDistance() {
        return getFieldFloatValue(3, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set distance field
     * Units: m
     * Comment: Accumulated distance along the segment at the described point
     *
     * @param distance
     */
    public void setDistance(Float distance) {
        setFieldValue(3, 0, distance, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get altitude field
     * Units: m
     * Comment: Accumulated altitude along the segment at the described point
     *
     * @return altitude
     */
    public Float getAltitude() {
        return getFieldFloatValue(4, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set altitude field
     * Units: m
     * Comment: Accumulated altitude along the segment at the described point
     *
     * @param altitude
     */
    public void setAltitude(Float altitude) {
        setFieldValue(4, 0, altitude, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    public Float[] getLeaderTime() {
        
        return getFieldFloatValues(5, Fit.SUBFIELD_INDEX_MAIN_FIELD);
        
    }

    /**
     * @return number of leader_time
     */
    public int getNumLeaderTime() {
        return getNumFieldValues(5, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get leader_time field
     * Units: s
     * Comment: Accumualted time each leader board member required to reach the described point. This value is zero for all leader board members at the starting point of the segment.
     *
     * @param index of leader_time
     * @return leader_time
     */
    public Float getLeaderTime(int index) {
        return getFieldFloatValue(5, index, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set leader_time field
     * Units: s
     * Comment: Accumualted time each leader board member required to reach the described point. This value is zero for all leader board members at the starting point of the segment.
     *
     * @param index of leader_time
     * @param leaderTime
     */
    public void setLeaderTime(int index, Float leaderTime) {
        setFieldValue(5, index, leaderTime, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

}
