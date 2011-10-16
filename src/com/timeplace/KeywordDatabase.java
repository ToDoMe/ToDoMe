package com.timeplace;

import java.util.ArrayList;
import java.util.Iterator;

public class KeywordDatabase {

	ArrayList<Keyword> keywords = new ArrayList<Keyword>();

	public void Keyword() {
		keywords.add(new Keyword("post", "postbox"));
	}

	public String getType(String name) {
		String[] words = name.split(" ");
		for (int i = 0; i < words.length; i++)
		{
			for (Iterator<Keyword> iter = keywords.iterator(); iter.hasNext();) {
				Keyword keywordObj = iter.next();
	
				if (keywordObj.keyword == words[i].toLowerCase()) {
					return "postbox";
				}
			}
		}

		return "postbox";
	}

	private class Keyword {
		String keyword;
		String type;

		Keyword(String keyword, String type) {
			this.keyword = keyword;
			this.type = type;
		}
	}

}