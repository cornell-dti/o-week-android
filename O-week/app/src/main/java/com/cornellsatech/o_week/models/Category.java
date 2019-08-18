package com.cornellsatech.o_week.models;

import androidx.annotation.NonNull;

import com.cornellsatech.o_week.util.Internet;
import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The category that an {@link Event} belongs in. This will be downloaded from the database via methods
 * in {@link Internet}, where new categories will be compared with saved ones.
 *
 * @see Event
 */
@Getter
@AllArgsConstructor
public class Category implements Comparable<Category>
{
	private static final Gson GSON = new Gson();
	private final String pk;
	private final String category;

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
		return other.pk.equals(pk);
	}
	/**
	 * Returns the {@link #pk}, which is unique to each {@link Category}.
	 *
	 * @return {@link #pk}
	 */
	@Override
	public int hashCode()
	{
		return pk.hashCode();
	}
	/**
	 * Compares 2 {@link Category}s using their {@link #category}. Useful for ordering alphabetically.
	 *
	 * @param o {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public int compareTo(@NonNull Category o)
	{
		return category.compareTo(o.category);
	}

	@Override
	@NonNull
	public String toString() {
		return GSON.toJson(this);
	}

	/**
	 * Returns a {@link Category} from JSON.
	 *
	 * @param json String produced by {@link #toString()}.
	 * @return {@link Category} created from the String.
	 */
	public static Category fromJSON(String json)
	{
		return GSON.fromJson(json, Category.class);
	}

	public static Category withPk(String pk) {
		return new Category(pk, null);
	}
}
