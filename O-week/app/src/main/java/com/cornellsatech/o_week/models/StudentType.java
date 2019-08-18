package com.cornellsatech.o_week.models;

/**
 * Enum to represent whether a student is a freshman or a transfer student
 */

public enum StudentType {
    TRANSFER,
    FRESHMAN,
    NOTSET;

	/**
	 * Returns the {@link StudentType} corresponding to the pk of the category.
	 * If the pk of anything changes on the database, this must be updated.
	 *
	 * @param categoryPk {@link Category#getPk()}
	 * @return {@link #TRANSFER} or {@link #NOTSET}
	 */
	public static StudentType toStudentType(String categoryPk)
	{
		return categoryPk.equals("B8AE27DD-DCD0-EF66-FC3B05EB37B392D7") ? TRANSFER : NOTSET;
	}
}

