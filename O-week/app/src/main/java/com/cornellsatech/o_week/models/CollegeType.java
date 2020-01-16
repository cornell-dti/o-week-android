package com.cornellsatech.o_week.models;

import androidx.annotation.StringRes;

import com.cornellsatech.o_week.R;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

/**
 * Enum to represent the college a student is in.
 */

public enum CollegeType {
    AAP,
    AS,
    CALS,
    ENG,
    HE,
    ILR,
    DYSON,
    HOTEL,
    NOTSET;

    public static BiMap<CollegeType, String> collegeToPk = ImmutableBiMap
            .<CollegeType, String>builder()
            .put(AAP, "noop - AAP")  // These "noop" IDs don't exist because they have no events this semester.
            .put(AS, "Arts & Sciences")
            .put(CALS, "Agriculture & Life Sciences")
            .put(ENG, "noop - ENG")
            .put(HE, "Human Ecology")
            .put(ILR, "ILR School")
            .put(DYSON, "noop - Dyson")
            .put(HOTEL, "SC Johnson College of Business - Hotel Administration")
            .build();

	@StringRes
	public int toStringRes()
	{
		switch (this)
		{
			case CALS:
				return R.string.college_als;
			case AAP:
				return R.string.college_aap;
			case AS:
				return R.string.college_arts;
			case ENG:
				return R.string.college_eng;
			case DYSON:
				return R.string.college_johnson;
			case HOTEL:
				return R.string.college_hotel;
			case ILR:
				return R.string.college_ilr;
			case HE:
				return R.string.college_he;
			default:
				return R.string.college_not_set;
		}
	}
}