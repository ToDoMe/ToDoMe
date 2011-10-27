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
import java.util.HashSet;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.location.Location;
import android.util.Base64;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class Util {
	
	public static ArrayList<Task> getReleventTasks(ArrayList<Task> tasks, PointOfInterest poi) {
		ArrayList<Task> releventTasks = new ArrayList<Task>();
		for (Iterator<Task> iter = tasks.iterator(); iter.hasNext();) {
			Task task = iter.next();

			HashSet<String> poiTypes = poi.getLocationTypes();
			HashSet<String> taskTypes = task.getTypes();

			if (poiTypes != null && taskTypes != null && taskTypes.size() != 0 && poiTypes.size() != 0) {
				for (Iterator<String> taskTypesIter = taskTypes.iterator(); taskTypesIter.hasNext();) {

					String taskType = taskTypesIter.next();
					if (poiTypes.contains(taskType)) {
						releventTasks.add(task);
						break;
					}
				}
			}
		}
		return releventTasks;
	}

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

	public static int doubleToIntE6(double dub) {
		return (int) (dub * 1e6);
	}

	public static double E6IntToDouble(int integer) {
		return (double) (integer / 1e6);
	}

	public static GeoPoint locationToGeoPoint(Location loc) {
		return new GeoPoint(doubleToIntE6(loc.getLatitude()), doubleToIntE6(loc.getLongitude()));
	}

	public static Location geoPointToLocation(GeoPoint point) {
		Location loc = new Location("");
		loc.setLatitude(E6IntToDouble(point.getLatitudeE6()));
		loc.setLongitude(E6IntToDouble(point.getLongitudeE6()));
		return loc;
	}

	// Serialization

	public static String getTaskArrayString(ArrayList<Task> tasks) {
		return getStringFromObject(tasks);
	}

	public static String getLocationDatabaseString(LocationDatabase ld) {
		return getStringFromObject(ld);
	}

	public static String getKeywordDatabaseString(KeywordDatabase kd) {
		return getStringFromObject(kd);
	}

	public static String getStringFromObject(Serializable obj) {
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

	public static LocationDatabase getLocationDatabaseFromString(String str) throws StreamCorruptedException, IOException, ClassNotFoundException {
		return (LocationDatabase) getObjectFromString(str);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Task> getTaskListFromString(String str) throws StreamCorruptedException, IOException, ClassNotFoundException {
		return (ArrayList<Task>) getObjectFromString(str);
	}

	public static KeywordDatabase getKeywordDatabaseFromString(String str) throws StreamCorruptedException, IOException, ClassNotFoundException {
		return (KeywordDatabase) getObjectFromString(str);
	}

	public static Object getObjectFromString(String str) throws StreamCorruptedException, IOException, ClassNotFoundException {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(Base64.decode(str, Base64.DEFAULT)));
		} catch (EOFException ex) {
			Log.i("Util.getObjectFromString", "EOFException");
		}
		return ois.readObject();
	}

	public static boolean isPointsWithinRange(GeoPoint point1, GeoPoint point2, double radius) {
		return (getDistanceBetween(point1, point2) <= radius);
	}

	public static double getDistanceBetween(GeoPoint point1, GeoPoint point2) {
		// Implemented from code at
		// http://www.movable-type.co.uk/scripts/latlong.html
		int R = 6371; // radius of Earth in km

		double lat2 = Math.toRadians(point2.getLatitudeE6() * 1e-6);
		double lat1 = Math.toRadians(point1.getLatitudeE6() * 1e-6);

		double dLat = lat2 - lat1;
		double dLon = Math.toRadians((point2.getLongitudeE6() - point1.getLongitudeE6()) * 1e-6);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = R * c;

		return d;
	}
	
	public static <E> HashSet<E> intersection (HashSet<E> x, HashSet<E> y) {
		HashSet<E> t = new HashSet<E>(x);
		t.retainAll(y);
		return t;
	}
}
