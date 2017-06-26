package com.cornellsatech.o_week;

import android.support.annotation.NonNull;

import org.json.JSONObject;

public class Category implements Comparable<Category>
{
	public final int pk;
	public final String name;
	public final String description;

	public Category(int pk, String name, String description)
	{
		this.pk = pk;
		this.name = name;
		this.description = description;
	}
	public Category(JSONObject json)
	{
		pk = json.optInt("pk");
		name = json.optString("category");
		description = json.optString("description");
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Category))
			return false;
		Category other = (Category) obj;
		return other.pk == pk;
	}
	@Override
	public int hashCode()
	{
		return pk;
	}
	@Override
	public int compareTo(@NonNull Category o)
	{
		return name.compareTo(o.name);
	}
	@Override
	public String toString()
	{
		return name + "|" + description + "|" + pk;
	}
	public static Category fromString(String string)
	{
		String[] parts = string.split("\\|");
		String name = parts[0];
		String description = parts[1];
		int pk = Integer.valueOf(parts[2]);
		return new Category(pk, name, description);
	}
}
