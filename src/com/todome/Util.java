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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.location.Location;
import android.util.Base64;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class Util {
	
	// Server comms
	
	public static String getFileFromServer(String request) {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(request);
		Log.i("Util.getFileFromServer", "Request used: " + request);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e("", "Failed to download file");
			}
		} catch (Exception ex) {
			Log.e("Util.getFileFromServer", ex.getClass().toString() + " " + ex.getMessage());
		}
		
		return builder.toString();
	}
	
	// Data conversion

	static int doubleToIntE6(double dub) {
		return (int) (dub * 1e6);
	}

	static double E6IntToDouble(int integer) {
		return (double) (integer / 1e6);
	}

	static GeoPoint locationToGeoPoint(Location loc) {
		return new GeoPoint(doubleToIntE6(loc.getLatitude()), doubleToIntE6(loc.getLongitude()));
	}

	static Location geoPointToLocation(GeoPoint point) {
		Location loc = new Location("");
		loc.setLatitude(E6IntToDouble(point.getLatitudeE6()));
		loc.setLongitude(E6IntToDouble(point.getLongitudeE6()));
		return loc;
	}

	// Serialization
	
	static String getTaskArrayString(ArrayList<Task> tasks) {
		return getStringFromObject(tasks);
	}

	static String getLocationDatabaseString(LocationDatabase ld) {
		return getStringFromObject(ld);
	}

	static String getKeywordDatabaseString(KeywordDatabase kd) {
		return getStringFromObject(kd);
	}

	static String getStringFromObject(Serializable obj) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
		} catch (IOException e) {
			Log.e("Util.getStringFromObject", "IOException: " + e.getMessage());
		}
		return null;
	}

	static LocationDatabase getLocationDatabaseFromString(String str) throws StreamCorruptedException, IOException, ClassNotFoundException {
		return (LocationDatabase) getObjectFromString(str);
	}

	@SuppressWarnings("unchecked")
	static ArrayList<Task> getTaskListFromString(String str) throws StreamCorruptedException, IOException, ClassNotFoundException {
		return (ArrayList<Task>) getObjectFromString(str);
	}

	static KeywordDatabase getKeyboardDatabaseFromString(String str) throws StreamCorruptedException, IOException, ClassNotFoundException {
		return (KeywordDatabase) getObjectFromString(str);
	}

	static Object getObjectFromString(String str) throws StreamCorruptedException, IOException, ClassNotFoundException {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(Base64.decode(str, Base64.DEFAULT)));
		} catch (EOFException ex) {
			Log.i("Util.getObjectFromString", "EOFException");
		}
		return ois.readObject();
	}
}
