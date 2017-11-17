package com.cornellsatech.o_week.models;

/**
 * Enum to represent whether a student is a freshman or a transfer student
 */

public enum StudentType {
    TRANSFER,
    FRESHMAN,
    NOTSET;

    public String toString(){
        return name();
    }
}

