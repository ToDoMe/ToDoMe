package com.todome;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "tags")
public class Tag {
	
	public final static String TAG_FIELD_NAME = "tag";

	@DatabaseField(id = true, canBeNull = false)
	private final String tag;

	public Tag(String tag) {
		this.tag = tag;
	}

	public static Tag getTagFromString(String tag) {
		return new Tag(tag);
	}

	public static String getStringFromTag(Tag tag) {
		return tag.toString();
	}

	public String toString() {
		return tag;
	}

}
