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

	/**
	 * Returns the {@link StudentType} corresponding to the pk of the category.
	 * If the pk of anything changes on the database, this must be updated.
	 *
	 * @param categoryPk {@link Category#pk}
	 * @return {@link #TRANSFER} or {@link #NOTSET}
	 */
	public static StudentType toStudentType(int categoryPk)
	{
		if (categoryPk == 14)
			return TRANSFER;
		return NOTSET;
	}
}

