package com.todome;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Tag implements Serializable {
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
