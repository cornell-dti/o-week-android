package com.cornellsatech.o_week.models;

/**
 * Enum to represent whether a student is an international student.
 */

public enum InternationalStudentStatus {
    YES,
    NO,
    NOTSET;

    public String toString(){
        return name();
    }
}