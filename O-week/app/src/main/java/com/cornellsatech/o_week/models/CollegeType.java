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
            .put(AAP, "8D0F380C-047E-8FD3-CDA449EB7C41A466")
            .put(AS, "8D0D75B3-BE48-48D8-DF46CC38682879C3")
            .put(CALS, "8D0BC52D-C504-D514-F334BEB4E18FF455")
            .put(ENG, "8D11CBA4-D6D3-7FDB-17ECC36ACBED42A5")
            .put(HE, "8D107B76-AFFE-D1B3-D4D752BCD7ED2265")
            .put(ILR, "8D139B03-E3DE-A329-B364603149879B5A")
            .put(DYSON, "3D51CFD6-A23C-EF4E-A6DF0F01930ACB62")
            .put(HOTEL, "8D084B16-073C-2EF4-0716B6DB7034C2F6")
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