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
	
	public void add(String keyword, String type) {
		keywords.add(new Keyword(keyword, type));
	}
	
	
	public static KeywordDatabase fromServer() {
		Log.i(TAG, "Getting KeywordDatabase from http://ec2-176-34-195-131.eu-west-1.compute.amazonaws.com/location_types.json");
		String file = Util.getFileFromServer("http://ec2-176-34-195-131.eu-west-1.compute.amazonaws.com/location_types.json");
		
		KeywordDatabase db = new KeywordDatabase();
		
		try {
			JSONArray jsonArray = new JSONArray(file);

			Log.i(TAG, "Number of entries " + jsonArray.length());

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				try {
					String type = jsonObject.getString("name");
					JSONArray tags = jsonObject.getJSONArray("tags");
					for (int j = 0; j < tags.length(); j++) {
						String name = tags.getJSONObject(j).getString("name");
						db.add(name, type);
					}
					
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage() + " for " + i + "/" + jsonArray.length(), e);
				}
			}
		} catch (JSONException ex) {
			Log.e(TAG, "", ex);
		}
		
		return db;
	}

	public KeywordDatabase() {	// TODO Get from server (http://ec2-176-34-195-131.eu-west-1.compute.amazonaws.com/location_types.json)
		/*keywords.add(new Keyword("post", "postbox"));
		keywords.add(new Keyword("letter", "postbox"));
		keywords.add(new Keyword("stamp", "post office"));
		keywords.add(new Keyword("withdraw", "bank"));
		keywords.add(new Keyword("money", "bank"));
		keywords.add(new Keyword("train", "train station"));
		keywords.add(new Keyword("bus", "bct"));*/
	}

	public int size() {
		return keywords.size();
	}

	public HashSet<String> getTypes(String name) {
		HashSet<String> types = new HashSet<String>();
		String lowerName = name.toLowerCase();
		for (Iterator<Keyword> iter = keywords.iterator(); iter.hasNext();) {
			Keyword keywordObj = iter.next();
			if (lowerName.contains(keywordObj.keyword)) {
				types.add(keywordObj.type);
			}
		}
		/*
		 * String[] words = name.split(" "); for (int i = 0; i < words.length; i++) { for (Iterator<Keyword> iter = keywords.iterator(); iter.hasNext();) {
		 * Keyword keywordObj = iter.next();
		 * 
		 * if (keywordObj.keyword == words[i].toLowerCase()) { return keywordObj.keyword; } } }
		 */

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