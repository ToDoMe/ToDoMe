package com.todome;

import java.util.ArrayList;
import java.util.Iterator;

public class TagList extends ArrayList<Tag> {

	
	public static TagList parse(String tagList) {
		return null; // TODO Parse a string for tags, throw exceptions if errors
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Iterator<Tag> tagIter = this.iterator(); tagIter.hasNext();) {
			builder.append(tagIter.next().toString());
			builder.append(" ");
		}
		return builder.toString();
	}
}
