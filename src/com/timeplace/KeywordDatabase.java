package com.timeplace;

import java.util.ArrayList;
import java.util.Iterator;

public class KeywordDatabase {

	ArrayList<Keyword> keywords = new ArrayList<Keyword>();

	public KeywordDatabase() {
		keywords.add(new Keyword("post", "postbox"));
		keywords.add(new Keyword("letter", "postbox"));
		keywords.add(new Keyword("stamp", "post office"));
		keywords.add(new Keyword("withdraw", "bank"));
		keywords.add(new Keyword("money", "bank"));
		keywords.add(new Keyword("train", "train station"));
	}
	
	public int size() {
		return keywords.size();
	}
	
	public String getType(String name) {
		String keyword = null;
		String lowerName = name.toLowerCase();
		for (Iterator<Keyword> iter = keywords.iterator(); iter.hasNext();) {
			Keyword keywordObj = iter.next();
			if (lowerName.contains(keywordObj.keyword))
			{
				keyword = keywordObj.type;
			}
		}
		/*String[] words = name.split(" ");
		for (int i = 0; i < words.length; i++)
		{
			for (Iterator<Keyword> iter = keywords.iterator(); iter.hasNext();) {
				Keyword keywordObj = iter.next();
	
				if (keywordObj.keyword == words[i].toLowerCase()) {
					return keywordObj.keyword;
				}
			}
		}*/

		return keyword;
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