package com.cornellsatech.o_week;

import android.support.annotation.NonNull;

import org.json.JSONObject;

/**
 * The category that an {@link Event} belongs in. This will be downloaded from the database via methods
 * in {@link Internet}, where new categories will be compared with saved ones. More in the constructor
 * {@link #Category(int, String, String)} below.
 *
 * @see Event
 */
public class Category implements Comparable<Category>
{
	public final int pk;
	public final String name;
	public final String description;

	/**
	 * Creates a category object in-app. This should never be done organically (without initial input
	 * from the database in some form), or else we risk becoming out-of-sync with the database.
	 *
	 * @param pk Unique positive ID given to each category starting from 1.
	 * @param name For example, "College of Engineering".
	 * @param description More information about a {@link Category}. Currently unused.
	 */
	public Category(int pk, String name, String description)
	{
		this.pk = pk;
		this.name = name;
		this.description = description;
	}
	/**
	 * Creates a category object using data downloaded from the database as a {@link JSONObject}.
	 *
	 * @param json JSON with the expected keys and values:
	 *             pk => Int
	 *             category => String
	 *             description => String
	 */
	public Category(JSONObject json)
	{
		pk = json.optInt("pk");
		name = json.optString("category");
		description = json.optString("description");
	}

	/**
	 * Returns whether this object has the same {@link #pk} as the given object.
	 *
	 * @param obj {@inheritDoc}
	 * @return See description.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Category))
			return false;
		Category other = (Category) obj;
		return other.pk == pk;
	}
	/**
	 * Returns the {@link #pk}, which is unique to each {@link Category}.
	 *
	 * @return {@link #pk}
	 */
	@Override
	public int hashCode()
	{
		return pk;
	}
	/**
	 * Compares 2 {@link Category}s using their {@link #name}. Useful for ordering alphabetically.
	 *
	 * @param o {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public int compareTo(@NonNull Category o)
	{
		return name.compareTo(o.name);
	}
	/**
	 * Returns a String containing all relevant info about this object. Each field is separated
	 * by a pipe "|", which David Chu deemed would not be regularly used in a String and thus is a good
	 * delimiter for splitting via {@link String#split(String)}. Useful for saving to file.
	 *
	 * @return {@inheritDoc}
	 * @see #fromString(String)
	 */
	@Override
	public String toString()
	{
		return name + "|" + description + "|" + pk;
	}
	/**
	 * Returns a {@link Category} from its String representation produced by {@link #toString()}.
	 *
	 * @param string String produced by {@link #toString()}.
	 * @return {@link Category} created from the String.
	 * @see #toString()
	 */
	public static Category fromString(String string)
	{
		String[] parts = string.split("\\|");
		String name = parts[0];
		String description = parts[1];
		int pk = Integer.valueOf(parts[2]);
		return new Category(pk, name, description);
	}
}
