package com.cornellsatech.o_week.models;

import androidx.annotation.StringRes;

import com.cornellsatech.o_week.R;

/**
 * Enum to represent the college a student is in.
 */

public enum CollegeType {
    ALS,
    JOHNSON,
    AAP,
    AS,
    ENG,
    ILR,
    HE,
    NOTSET;

    public String toString()
    {
        return name();
    }

    public static CollegeType toCollegeType(int fromPk) {
        switch(fromPk) {
                case 7:
                    return CollegeType.ALS;
                case 13:
                    return CollegeType.AAP;
                case 8:
                    return CollegeType.AS;
                case 9:
                    return CollegeType.ENG;
                case 12:
                    return CollegeType.JOHNSON;
                case 11:
                    return CollegeType.ILR;
                case 10:
                    return CollegeType.HE;
                default:
                    return CollegeType.NOTSET;
        }
    }

    public int toPk() {
        switch(this) {
            case ALS: return 7;
            case AAP: return 13;
            case AS: return 8;
            case ENG: return 9;
            case JOHNSON: return 12;
            case ILR: return 11;
            case HE: return 10;
            default: return -1;
        }
    }

	@StringRes
	public int toStringRes()
	{
		switch (this)
		{
			case ALS:
				return R.string.college_als;
			case AAP:
				return R.string.college_aap;
			case AS:
				return R.string.college_arts;
			case ENG:
				return R.string.college_eng;
			case JOHNSON:
				return R.string.college_johnson;
			case ILR:
				return R.string.college_ilr;
			case HE:
				return R.string.college_he;
			default:
				return R.string.college_not_set;
		}
	}
}