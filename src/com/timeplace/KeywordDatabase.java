package com.timeplace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class KeywordDatabase implements Serializable {

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
	
	public ArrayList<String> getTypes(String name) {
		ArrayList<String> types = new ArrayList<String>();
		String lowerName = name.toLowerCase();
		for (Iterator<Keyword> iter = keywords.iterator(); iter.hasNext();) {
			Keyword keywordObj = iter.next();
			if (lowerName.contains(keywordObj.keyword))
			{
				types.add(keywordObj.type);
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

		return types;
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