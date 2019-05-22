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


public class WorkoutStepMesg extends Mesg {

    
    public static final int MessageIndexFieldNum = 254;
    
    public static final int WktStepNameFieldNum = 0;
    
    public static final int DurationTypeFieldNum = 1;
    
    public static final int DurationValueFieldNum = 2;
    
    public static final int TargetTypeFieldNum = 3;
    
    public static final int TargetValueFieldNum = 4;
    
    public static final int CustomTargetValueLowFieldNum = 5;
    
    public static final int CustomTargetValueHighFieldNum = 6;
    
    public static final int IntensityFieldNum = 7;
    
    public static final int NotesFieldNum = 8;
    
    public static final int EquipmentFieldNum = 9;
    
    public static final int ExerciseCategoryFieldNum = 10;
    
    public static final int ExerciseNameFieldNum = 11;
    
    public static final int ExerciseWeightFieldNum = 12;
    
    public static final int WeightDisplayUnitFieldNum = 13;
    

    protected static final  Mesg workoutStepMesg;
    static {
        int field_index = 0;
        int subfield_index = 0;
        // workout_step
        workoutStepMesg = new Mesg("workout_step", MesgNum.WORKOUT_STEP);
        workoutStepMesg.addField(new Field("message_index", MessageIndexFieldNum, 132, 1, 0, "", false, Profile.Type.MESSAGE_INDEX));
        field_index++;
        workoutStepMesg.addField(new Field("wkt_step_name", WktStepNameFieldNum, 7, 1, 0, "", false, Profile.Type.STRING));
        field_index++;
        workoutStepMesg.addField(new Field("duration_type", DurationTypeFieldNum, 0, 1, 0, "", false, Profile.Type.WKT_STEP_DURATION));
        field_index++;
        workoutStepMesg.addField(new Field("duration_value", DurationValueFieldNum, 134, 1, 0, "", false, Profile.Type.UINT32));
        subfield_index = 0;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("duration_time", 134, 1000, 0, "s"));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 0);
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 28);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("duration_distance", 134, 100, 0, "m"));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 1);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("duration_hr", 134, 1, 0, "% or bpm"));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 2);
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 3);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("duration_calories", 134, 1, 0, "calories"));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 4);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("duration_step", 134, 1, 0, ""));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 6);
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 7);
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 8);
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 9);
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 10);
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 11);
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 12);
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 13);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("duration_power", 134, 1, 0, "% or watts"));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 14);
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 15);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("duration_reps", 134, 1, 0, ""));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 29);
        subfield_index++;
        field_index++;
        workoutStepMesg.addField(new Field("target_type", TargetTypeFieldNum, 0, 1, 0, "", false, Profile.Type.WKT_STEP_TARGET));
        field_index++;
        workoutStepMesg.addField(new Field("target_value", TargetValueFieldNum, 134, 1, 0, "", false, Profile.Type.UINT32));
        subfield_index = 0;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("target_speed_zone", 134, 1, 0, ""));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(3, 0);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("target_hr_zone", 134, 1, 0, ""));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(3, 1);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("target_cadence_zone", 134, 1, 0, ""));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(3, 3);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("target_power_zone", 134, 1, 0, ""));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(3, 4);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("repeat_steps", 134, 1, 0, ""));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 6);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("repeat_time", 134, 1000, 0, "s"));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 7);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("repeat_distance", 134, 100, 0, "m"));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 8);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("repeat_calories", 134, 1, 0, "calories"));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 9);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("repeat_hr", 134, 1, 0, "% or bpm"));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 10);
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 11);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("repeat_power", 134, 1, 0, "% or watts"));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 12);
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(1, 13);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("target_stroke_type", 0, 1, 0, ""));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(3, 11);
        subfield_index++;
        field_index++;
        workoutStepMesg.addField(new Field("custom_target_value_low", CustomTargetValueLowFieldNum, 134, 1, 0, "", false, Profile.Type.UINT32));
        subfield_index = 0;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("custom_target_speed_low", 134, 1000, 0, "m/s"));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(3, 0);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("custom_target_heart_rate_low", 134, 1, 0, "% or bpm"));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(3, 1);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("custom_target_cadence_low", 134, 1, 0, "rpm"));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(3, 3);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("custom_target_power_low", 134, 1, 0, "% or watts"));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(3, 4);
        subfield_index++;
        field_index++;
        workoutStepMesg.addField(new Field("custom_target_value_high", CustomTargetValueHighFieldNum, 134, 1, 0, "", false, Profile.Type.UINT32));
        subfield_index = 0;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("custom_target_speed_high", 134, 1000, 0, "m/s"));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(3, 0);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("custom_target_heart_rate_high", 134, 1, 0, "% or bpm"));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(3, 1);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("custom_target_cadence_high", 134, 1, 0, "rpm"));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(3, 3);
        subfield_index++;
        workoutStepMesg.fields.get(field_index).subFields.add(new SubField("custom_target_power_high", 134, 1, 0, "% or watts"));
        workoutStepMesg.fields.get(field_index).subFields.get(subfield_index).addMap(3, 4);
        subfield_index++;
        field_index++;
        workoutStepMesg.addField(new Field("intensity", IntensityFieldNum, 0, 1, 0, "", false, Profile.Type.INTENSITY));
        field_index++;
        workoutStepMesg.addField(new Field("notes", NotesFieldNum, 7, 1, 0, "", false, Profile.Type.STRING));
        field_index++;
        workoutStepMesg.addField(new Field("equipment", EquipmentFieldNum, 0, 1, 0, "", false, Profile.Type.WORKOUT_EQUIPMENT));
        field_index++;
        workoutStepMesg.addField(new Field("exercise_category", ExerciseCategoryFieldNum, 132, 1, 0, "", false, Profile.Type.EXERCISE_CATEGORY));
        field_index++;
        workoutStepMesg.addField(new Field("exercise_name", ExerciseNameFieldNum, 132, 1, 0, "", false, Profile.Type.UINT16));
        field_index++;
        workoutStepMesg.addField(new Field("exercise_weight", ExerciseWeightFieldNum, 132, 100, 0, "kg", false, Profile.Type.UINT16));
        field_index++;
        workoutStepMesg.addField(new Field("weight_display_unit", WeightDisplayUnitFieldNum, 132, 1, 0, "", false, Profile.Type.FIT_BASE_UNIT));
        field_index++;
    }

    public WorkoutStepMesg() {
        super(Factory.createMesg(MesgNum.WORKOUT_STEP));
    }

    public WorkoutStepMesg(final Mesg mesg) {
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
     * Get wkt_step_name field
     *
     * @return wkt_step_name
     */
    public String getWktStepName() {
        return getFieldStringValue(0, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set wkt_step_name field
     *
     * @param wktStepName
     */
    public void setWktStepName(String wktStepName) {
        setFieldValue(0, 0, wktStepName, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get duration_type field
     *
     * @return duration_type
     */
    public WktStepDuration getDurationType() {
        Short value = getFieldShortValue(1, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
        if (value == null) {
            return null;
        }
        return WktStepDuration.getByValue(value);
    }

    /**
     * Set duration_type field
     *
     * @param durationType
     */
    public void setDurationType(WktStepDuration durationType) {
        setFieldValue(1, 0, durationType.value, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get duration_value field
     *
     * @return duration_value
     */
    public Long getDurationValue() {
        return getFieldLongValue(2, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set duration_value field
     *
     * @param durationValue
     */
    public void setDurationValue(Long durationValue) {
        setFieldValue(2, 0, durationValue, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get duration_time field
     * Units: s
     *
     * @return duration_time
     */
    public Float getDurationTime() {
        return getFieldFloatValue(2, 0, Profile.SubFields.WORKOUT_STEP_MESG_DURATION_VALUE_FIELD_DURATION_TIME);
    }

    /**
     * Set duration_time field
     * Units: s
     *
     * @param durationTime
     */
    public void setDurationTime(Float durationTime) {
        setFieldValue(2, 0, durationTime, Profile.SubFields.WORKOUT_STEP_MESG_DURATION_VALUE_FIELD_DURATION_TIME);
    }

    /**
     * Get duration_distance field
     * Units: m
     *
     * @return duration_distance
     */
    public Float getDurationDistance() {
        return getFieldFloatValue(2, 0, Profile.SubFields.WORKOUT_STEP_MESG_DURATION_VALUE_FIELD_DURATION_DISTANCE);
    }

    /**
     * Set duration_distance field
     * Units: m
     *
     * @param durationDistance
     */
    public void setDurationDistance(Float durationDistance) {
        setFieldValue(2, 0, durationDistance, Profile.SubFields.WORKOUT_STEP_MESG_DURATION_VALUE_FIELD_DURATION_DISTANCE);
    }

    /**
     * Get duration_hr field
     * Units: % or bpm
     *
     * @return duration_hr
     */
    public Long getDurationHr() {
        return getFieldLongValue(2, 0, Profile.SubFields.WORKOUT_STEP_MESG_DURATION_VALUE_FIELD_DURATION_HR);
    }

    /**
     * Set duration_hr field
     * Units: % or bpm
     *
     * @param durationHr
     */
    public void setDurationHr(Long durationHr) {
        setFieldValue(2, 0, durationHr, Profile.SubFields.WORKOUT_STEP_MESG_DURATION_VALUE_FIELD_DURATION_HR);
    }

    /**
     * Get duration_calories field
     * Units: calories
     *
     * @return duration_calories
     */
    public Long getDurationCalories() {
        return getFieldLongValue(2, 0, Profile.SubFields.WORKOUT_STEP_MESG_DURATION_VALUE_FIELD_DURATION_CALORIES);
    }

    /**
     * Set duration_calories field
     * Units: calories
     *
     * @param durationCalories
     */
    public void setDurationCalories(Long durationCalories) {
        setFieldValue(2, 0, durationCalories, Profile.SubFields.WORKOUT_STEP_MESG_DURATION_VALUE_FIELD_DURATION_CALORIES);
    }

    /**
     * Get duration_step field
     * Comment: message_index of step to loop back to. Steps are assumed to be in the order by message_index. custom_name and intensity members are undefined for this duration type.
     *
     * @return duration_step
     */
    public Long getDurationStep() {
        return getFieldLongValue(2, 0, Profile.SubFields.WORKOUT_STEP_MESG_DURATION_VALUE_FIELD_DURATION_STEP);
    }

    /**
     * Set duration_step field
     * Comment: message_index of step to loop back to. Steps are assumed to be in the order by message_index. custom_name and intensity members are undefined for this duration type.
     *
     * @param durationStep
     */
    public void setDurationStep(Long durationStep) {
        setFieldValue(2, 0, durationStep, Profile.SubFields.WORKOUT_STEP_MESG_DURATION_VALUE_FIELD_DURATION_STEP);
    }

    /**
     * Get duration_power field
     * Units: % or watts
     *
     * @return duration_power
     */
    public Long getDurationPower() {
        return getFieldLongValue(2, 0, Profile.SubFields.WORKOUT_STEP_MESG_DURATION_VALUE_FIELD_DURATION_POWER);
    }

    /**
     * Set duration_power field
     * Units: % or watts
     *
     * @param durationPower
     */
    public void setDurationPower(Long durationPower) {
        setFieldValue(2, 0, durationPower, Profile.SubFields.WORKOUT_STEP_MESG_DURATION_VALUE_FIELD_DURATION_POWER);
    }

    /**
     * Get duration_reps field
     *
     * @return duration_reps
     */
    public Long getDurationReps() {
        return getFieldLongValue(2, 0, Profile.SubFields.WORKOUT_STEP_MESG_DURATION_VALUE_FIELD_DURATION_REPS);
    }

    /**
     * Set duration_reps field
     *
     * @param durationReps
     */
    public void setDurationReps(Long durationReps) {
        setFieldValue(2, 0, durationReps, Profile.SubFields.WORKOUT_STEP_MESG_DURATION_VALUE_FIELD_DURATION_REPS);
    }

    /**
     * Get target_type field
     *
     * @return target_type
     */
    public WktStepTarget getTargetType() {
        Short value = getFieldShortValue(3, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
        if (value == null) {
            return null;
        }
        return WktStepTarget.getByValue(value);
    }

    /**
     * Set target_type field
     *
     * @param targetType
     */
    public void setTargetType(WktStepTarget targetType) {
        setFieldValue(3, 0, targetType.value, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get target_value field
     *
     * @return target_value
     */
    public Long getTargetValue() {
        return getFieldLongValue(4, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set target_value field
     *
     * @param targetValue
     */
    public void setTargetValue(Long targetValue) {
        setFieldValue(4, 0, targetValue, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get target_speed_zone field
     * Comment: speed zone (1-10);Custom =0;
     *
     * @return target_speed_zone
     */
    public Long getTargetSpeedZone() {
        return getFieldLongValue(4, 0, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_TARGET_SPEED_ZONE);
    }

    /**
     * Set target_speed_zone field
     * Comment: speed zone (1-10);Custom =0;
     *
     * @param targetSpeedZone
     */
    public void setTargetSpeedZone(Long targetSpeedZone) {
        setFieldValue(4, 0, targetSpeedZone, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_TARGET_SPEED_ZONE);
    }

    /**
     * Get target_hr_zone field
     * Comment: hr zone (1-5);Custom =0;
     *
     * @return target_hr_zone
     */
    public Long getTargetHrZone() {
        return getFieldLongValue(4, 0, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_TARGET_HR_ZONE);
    }

    /**
     * Set target_hr_zone field
     * Comment: hr zone (1-5);Custom =0;
     *
     * @param targetHrZone
     */
    public void setTargetHrZone(Long targetHrZone) {
        setFieldValue(4, 0, targetHrZone, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_TARGET_HR_ZONE);
    }

    /**
     * Get target_cadence_zone field
     * Comment: Zone (1-?); Custom = 0;
     *
     * @return target_cadence_zone
     */
    public Long getTargetCadenceZone() {
        return getFieldLongValue(4, 0, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_TARGET_CADENCE_ZONE);
    }

    /**
     * Set target_cadence_zone field
     * Comment: Zone (1-?); Custom = 0;
     *
     * @param targetCadenceZone
     */
    public void setTargetCadenceZone(Long targetCadenceZone) {
        setFieldValue(4, 0, targetCadenceZone, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_TARGET_CADENCE_ZONE);
    }

    /**
     * Get target_power_zone field
     * Comment: Power Zone ( 1-7); Custom = 0;
     *
     * @return target_power_zone
     */
    public Long getTargetPowerZone() {
        return getFieldLongValue(4, 0, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_TARGET_POWER_ZONE);
    }

    /**
     * Set target_power_zone field
     * Comment: Power Zone ( 1-7); Custom = 0;
     *
     * @param targetPowerZone
     */
    public void setTargetPowerZone(Long targetPowerZone) {
        setFieldValue(4, 0, targetPowerZone, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_TARGET_POWER_ZONE);
    }

    /**
     * Get repeat_steps field
     * Comment: # of repetitions
     *
     * @return repeat_steps
     */
    public Long getRepeatSteps() {
        return getFieldLongValue(4, 0, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_REPEAT_STEPS);
    }

    /**
     * Set repeat_steps field
     * Comment: # of repetitions
     *
     * @param repeatSteps
     */
    public void setRepeatSteps(Long repeatSteps) {
        setFieldValue(4, 0, repeatSteps, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_REPEAT_STEPS);
    }

    /**
     * Get repeat_time field
     * Units: s
     *
     * @return repeat_time
     */
    public Float getRepeatTime() {
        return getFieldFloatValue(4, 0, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_REPEAT_TIME);
    }

    /**
     * Set repeat_time field
     * Units: s
     *
     * @param repeatTime
     */
    public void setRepeatTime(Float repeatTime) {
        setFieldValue(4, 0, repeatTime, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_REPEAT_TIME);
    }

    /**
     * Get repeat_distance field
     * Units: m
     *
     * @return repeat_distance
     */
    public Float getRepeatDistance() {
        return getFieldFloatValue(4, 0, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_REPEAT_DISTANCE);
    }

    /**
     * Set repeat_distance field
     * Units: m
     *
     * @param repeatDistance
     */
    public void setRepeatDistance(Float repeatDistance) {
        setFieldValue(4, 0, repeatDistance, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_REPEAT_DISTANCE);
    }

    /**
     * Get repeat_calories field
     * Units: calories
     *
     * @return repeat_calories
     */
    public Long getRepeatCalories() {
        return getFieldLongValue(4, 0, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_REPEAT_CALORIES);
    }

    /**
     * Set repeat_calories field
     * Units: calories
     *
     * @param repeatCalories
     */
    public void setRepeatCalories(Long repeatCalories) {
        setFieldValue(4, 0, repeatCalories, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_REPEAT_CALORIES);
    }

    /**
     * Get repeat_hr field
     * Units: % or bpm
     *
     * @return repeat_hr
     */
    public Long getRepeatHr() {
        return getFieldLongValue(4, 0, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_REPEAT_HR);
    }

    /**
     * Set repeat_hr field
     * Units: % or bpm
     *
     * @param repeatHr
     */
    public void setRepeatHr(Long repeatHr) {
        setFieldValue(4, 0, repeatHr, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_REPEAT_HR);
    }

    /**
     * Get repeat_power field
     * Units: % or watts
     *
     * @return repeat_power
     */
    public Long getRepeatPower() {
        return getFieldLongValue(4, 0, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_REPEAT_POWER);
    }

    /**
     * Set repeat_power field
     * Units: % or watts
     *
     * @param repeatPower
     */
    public void setRepeatPower(Long repeatPower) {
        setFieldValue(4, 0, repeatPower, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_REPEAT_POWER);
    }

    /**
     * Get target_stroke_type field
     *
     * @return target_stroke_type
     */
    public SwimStroke getTargetStrokeType() {
        Short value = getFieldShortValue(4, 0, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_TARGET_STROKE_TYPE);
        if (value == null) {
            return null;
        }
        return SwimStroke.getByValue(value);
    }

    /**
     * Set target_stroke_type field
     *
     * @param targetStrokeType
     */
    public void setTargetStrokeType(SwimStroke targetStrokeType) {
        setFieldValue(4, 0, targetStrokeType.value, Profile.SubFields.WORKOUT_STEP_MESG_TARGET_VALUE_FIELD_TARGET_STROKE_TYPE);
    }

    /**
     * Get custom_target_value_low field
     *
     * @return custom_target_value_low
     */
    public Long getCustomTargetValueLow() {
        return getFieldLongValue(5, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set custom_target_value_low field
     *
     * @param customTargetValueLow
     */
    public void setCustomTargetValueLow(Long customTargetValueLow) {
        setFieldValue(5, 0, customTargetValueLow, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get custom_target_speed_low field
     * Units: m/s
     *
     * @return custom_target_speed_low
     */
    public Float getCustomTargetSpeedLow() {
        return getFieldFloatValue(5, 0, Profile.SubFields.WORKOUT_STEP_MESG_CUSTOM_TARGET_VALUE_LOW_FIELD_CUSTOM_TARGET_SPEED_LOW);
    }

    /**
     * Set custom_target_speed_low field
     * Units: m/s
     *
     * @param customTargetSpeedLow
     */
    public void setCustomTargetSpeedLow(Float customTargetSpeedLow) {
        setFieldValue(5, 0, customTargetSpeedLow, Profile.SubFields.WORKOUT_STEP_MESG_CUSTOM_TARGET_VALUE_LOW_FIELD_CUSTOM_TARGET_SPEED_LOW);
    }

    /**
     * Get custom_target_heart_rate_low field
     * Units: % or bpm
     *
     * @return custom_target_heart_rate_low
     */
    public Long getCustomTargetHeartRateLow() {
        return getFieldLongValue(5, 0, Profile.SubFields.WORKOUT_STEP_MESG_CUSTOM_TARGET_VALUE_LOW_FIELD_CUSTOM_TARGET_HEART_RATE_LOW);
    }

    /**
     * Set custom_target_heart_rate_low field
     * Units: % or bpm
     *
     * @param customTargetHeartRateLow
     */
    public void setCustomTargetHeartRateLow(Long customTargetHeartRateLow) {
        setFieldValue(5, 0, customTargetHeartRateLow, Profile.SubFields.WORKOUT_STEP_MESG_CUSTOM_TARGET_VALUE_LOW_FIELD_CUSTOM_TARGET_HEART_RATE_LOW);
    }

    /**
     * Get custom_target_cadence_low field
     * Units: rpm
     *
     * @return custom_target_cadence_low
     */
    public Long getCustomTargetCadenceLow() {
        return getFieldLongValue(5, 0, Profile.SubFields.WORKOUT_STEP_MESG_CUSTOM_TARGET_VALUE_LOW_FIELD_CUSTOM_TARGET_CADENCE_LOW);
    }

    /**
     * Set custom_target_cadence_low field
     * Units: rpm
     *
     * @param customTargetCadenceLow
     */
    public void setCustomTargetCadenceLow(Long customTargetCadenceLow) {
        setFieldValue(5, 0, customTargetCadenceLow, Profile.SubFields.WORKOUT_STEP_MESG_CUSTOM_TARGET_VALUE_LOW_FIELD_CUSTOM_TARGET_CADENCE_LOW);
    }

    /**
     * Get custom_target_power_low field
     * Units: % or watts
     *
     * @return custom_target_power_low
     */
    public Long getCustomTargetPowerLow() {
        return getFieldLongValue(5, 0, Profile.SubFields.WORKOUT_STEP_MESG_CUSTOM_TARGET_VALUE_LOW_FIELD_CUSTOM_TARGET_POWER_LOW);
    }

    /**
     * Set custom_target_power_low field
     * Units: % or watts
     *
     * @param customTargetPowerLow
     */
    public void setCustomTargetPowerLow(Long customTargetPowerLow) {
        setFieldValue(5, 0, customTargetPowerLow, Profile.SubFields.WORKOUT_STEP_MESG_CUSTOM_TARGET_VALUE_LOW_FIELD_CUSTOM_TARGET_POWER_LOW);
    }

    /**
     * Get custom_target_value_high field
     *
     * @return custom_target_value_high
     */
    public Long getCustomTargetValueHigh() {
        return getFieldLongValue(6, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set custom_target_value_high field
     *
     * @param customTargetValueHigh
     */
    public void setCustomTargetValueHigh(Long customTargetValueHigh) {
        setFieldValue(6, 0, customTargetValueHigh, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get custom_target_speed_high field
     * Units: m/s
     *
     * @return custom_target_speed_high
     */
    public Float getCustomTargetSpeedHigh() {
        return getFieldFloatValue(6, 0, Profile.SubFields.WORKOUT_STEP_MESG_CUSTOM_TARGET_VALUE_HIGH_FIELD_CUSTOM_TARGET_SPEED_HIGH);
    }

    /**
     * Set custom_target_speed_high field
     * Units: m/s
     *
     * @param customTargetSpeedHigh
     */
    public void setCustomTargetSpeedHigh(Float customTargetSpeedHigh) {
        setFieldValue(6, 0, customTargetSpeedHigh, Profile.SubFields.WORKOUT_STEP_MESG_CUSTOM_TARGET_VALUE_HIGH_FIELD_CUSTOM_TARGET_SPEED_HIGH);
    }

    /**
     * Get custom_target_heart_rate_high field
     * Units: % or bpm
     *
     * @return custom_target_heart_rate_high
     */
    public Long getCustomTargetHeartRateHigh() {
        return getFieldLongValue(6, 0, Profile.SubFields.WORKOUT_STEP_MESG_CUSTOM_TARGET_VALUE_HIGH_FIELD_CUSTOM_TARGET_HEART_RATE_HIGH);
    }

    /**
     * Set custom_target_heart_rate_high field
     * Units: % or bpm
     *
     * @param customTargetHeartRateHigh
     */
    public void setCustomTargetHeartRateHigh(Long customTargetHeartRateHigh) {
        setFieldValue(6, 0, customTargetHeartRateHigh, Profile.SubFields.WORKOUT_STEP_MESG_CUSTOM_TARGET_VALUE_HIGH_FIELD_CUSTOM_TARGET_HEART_RATE_HIGH);
    }

    /**
     * Get custom_target_cadence_high field
     * Units: rpm
     *
     * @return custom_target_cadence_high
     */
    public Long getCustomTargetCadenceHigh() {
        return getFieldLongValue(6, 0, Profile.SubFields.WORKOUT_STEP_MESG_CUSTOM_TARGET_VALUE_HIGH_FIELD_CUSTOM_TARGET_CADENCE_HIGH);
    }

    /**
     * Set custom_target_cadence_high field
     * Units: rpm
     *
     * @param customTargetCadenceHigh
     */
    public void setCustomTargetCadenceHigh(Long customTargetCadenceHigh) {
        setFieldValue(6, 0, customTargetCadenceHigh, Profile.SubFields.WORKOUT_STEP_MESG_CUSTOM_TARGET_VALUE_HIGH_FIELD_CUSTOM_TARGET_CADENCE_HIGH);
    }

    /**
     * Get custom_target_power_high field
     * Units: % or watts
     *
     * @return custom_target_power_high
     */
    public Long getCustomTargetPowerHigh() {
        return getFieldLongValue(6, 0, Profile.SubFields.WORKOUT_STEP_MESG_CUSTOM_TARGET_VALUE_HIGH_FIELD_CUSTOM_TARGET_POWER_HIGH);
    }

    /**
     * Set custom_target_power_high field
     * Units: % or watts
     *
     * @param customTargetPowerHigh
     */
    public void setCustomTargetPowerHigh(Long customTargetPowerHigh) {
        setFieldValue(6, 0, customTargetPowerHigh, Profile.SubFields.WORKOUT_STEP_MESG_CUSTOM_TARGET_VALUE_HIGH_FIELD_CUSTOM_TARGET_POWER_HIGH);
    }

    /**
     * Get intensity field
     *
     * @return intensity
     */
    public Intensity getIntensity() {
        Short value = getFieldShortValue(7, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
        if (value == null) {
            return null;
        }
        return Intensity.getByValue(value);
    }

    /**
     * Set intensity field
     *
     * @param intensity
     */
    public void setIntensity(Intensity intensity) {
        setFieldValue(7, 0, intensity.value, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get notes field
     *
     * @return notes
     */
    public String getNotes() {
        return getFieldStringValue(8, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set notes field
     *
     * @param notes
     */
    public void setNotes(String notes) {
        setFieldValue(8, 0, notes, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get equipment field
     *
     * @return equipment
     */
    public WorkoutEquipment getEquipment() {
        Short value = getFieldShortValue(9, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
        if (value == null) {
            return null;
        }
        return WorkoutEquipment.getByValue(value);
    }

    /**
     * Set equipment field
     *
     * @param equipment
     */
    public void setEquipment(WorkoutEquipment equipment) {
        setFieldValue(9, 0, equipment.value, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get exercise_category field
     *
     * @return exercise_category
     */
    public Integer getExerciseCategory() {
        return getFieldIntegerValue(10, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set exercise_category field
     *
     * @param exerciseCategory
     */
    public void setExerciseCategory(Integer exerciseCategory) {
        setFieldValue(10, 0, exerciseCategory, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get exercise_name field
     *
     * @return exercise_name
     */
    public Integer getExerciseName() {
        return getFieldIntegerValue(11, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set exercise_name field
     *
     * @param exerciseName
     */
    public void setExerciseName(Integer exerciseName) {
        setFieldValue(11, 0, exerciseName, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get exercise_weight field
     * Units: kg
     *
     * @return exercise_weight
     */
    public Float getExerciseWeight() {
        return getFieldFloatValue(12, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set exercise_weight field
     * Units: kg
     *
     * @param exerciseWeight
     */
    public void setExerciseWeight(Float exerciseWeight) {
        setFieldValue(12, 0, exerciseWeight, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Get weight_display_unit field
     *
     * @return weight_display_unit
     */
    public Integer getWeightDisplayUnit() {
        return getFieldIntegerValue(13, 0, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

    /**
     * Set weight_display_unit field
     *
     * @param weightDisplayUnit
     */
    public void setWeightDisplayUnit(Integer weightDisplayUnit) {
        setFieldValue(13, 0, weightDisplayUnit, Fit.SUBFIELD_INDEX_MAIN_FIELD);
    }

}
