/*
 * Copyright (C) 2011  Chris Baines
 * Copyright (C) 2011  Rebecca Brannum
 * Copyright (C) 2011  Harry Cutts
 * Copyright (C) 2011  John Preston
 * Copyright (C) 2011  James Robinson
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.todome;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class KeywordDatabase implements Serializable {

	private static final long serialVersionUID = -8902088060528474897L;

	HashSet<Keyword> keywords = new HashSet<Keyword>();

	private static final String TAG = "KeywordDatabase";
	
	/*
	 * Blacklist of certain types such as brothel and baby_box, 
	 * which could be misinterpreted with the current algorithm.
	 */
	public static final HashSet<String> blacklistedTypes = new HashSet<String>();

	public void add(String keyword, String type, String description) {
		keywords.add(new Keyword(keyword, type, description));
	}

	public KeywordDatabase() {
		blacklistedTypes.add("amenity.baby_hatch");
		blacklistedTypes.add("amenity.brothel");
		blacklistedTypes.add("shop.erotic");
		blacklistedTypes.add("amenity.baby_hatch");
	}

	public int size() {
		return keywords.size();
	}

	/*
	 * Match the words in the task name with the keywords in the dictionary,
	 * The original implementation did a String.contains, which gave interesting 
	 * results, for instance "Catch bus" would match with bct, bcs, shop.pet (as the cat in Catch) ...
	 */
	public HashSet<String> getTypes(String name) {
		HashSet<String> types = new HashSet<String>();
		
		String lowerName = name.toLowerCase();
		String[] words = lowerName.split(" ");
		
		for (Iterator<Keyword> iter = keywords.iterator(); iter.hasNext();) {
			Keyword keywordObj = iter.next();
			for (int i = 0; i < words.length; i++) {
				if (words[i].equals(keywordObj.keyword)) {
					types.add(keywordObj.type);
				}
			}
		}

		return types;
	}
	
	public HashSet<Keyword> getAllKeywords() {
		return keywords;
	}

	public String getDescriptionForType(String type) {
		for (Iterator<Keyword> iter = keywords.iterator(); iter.hasNext();) {
			Keyword keyword = iter.next();
			
			if (keyword.type == type) {
				return keyword.description;
			}
		}
		
		return null;
	}

	public HashSet<String> getAllTypes() {
		HashSet<String> types = new HashSet<String>();
		for (Iterator<Keyword> iter = keywords.iterator(); iter.hasNext();) {
			types.add(iter.next().type);
		}
		return types;
	}

	private class Keyword implements Serializable {
		String keyword;
		String type;
		String description;

		Keyword(String keyword, String type, String description) {
			this.keyword = keyword;
			this.type = type;
			this.description = description;
		}
	}
}