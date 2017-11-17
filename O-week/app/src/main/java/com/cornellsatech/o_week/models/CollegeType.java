package com.cornellsatech.o_week.models;

/**
 * Enum to represent the college a student is in.
 */

public enum CollegeType {
    ALS,
    AEM,
    AAP,
    AS,
    ENG,
    HA,
    ILR,
    HE,
    NOTSET;

    public String toString(){
        return name();
    }
}