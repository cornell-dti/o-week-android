package com.cornellsatech.o_week.models;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

/**
 * Enum to represent whether a student is a freshman or a transfer student
 */

public enum StudentType {
    TRANSFER,
    FRESHMAN,
	NOTSET;
	

    public static BiMap<StudentType, String> studentTypeToCategory = ImmutableBiMap
			.<StudentType, String>builder()
            .put(FRESHMAN, "First-Year Students")
            .put(TRANSFER, "Transfer Students")
            .build();
}

