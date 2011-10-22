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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import android.location.Location;
import android.util.Base64;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class Util {

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

	public static KeywordDatabase getKeyboardDatabaseFromString(String str) throws StreamCorruptedException, IOException, ClassNotFoundException {
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
		// Implemented from code at
		// http://www.movable-type.co.uk/scripts/latlong.html
		int R = 6371; // radius of Earth in km

		double lat2 = Math.toRadians(point2.getLatitudeE6() * 10e-6);
		double lat1 = Math.toRadians(point1.getLatitudeE6() * 10e-6);

		double dLat = lat2 - lat1;
		double dLon = Math.toRadians((point2.getLongitudeE6() - point1.getLongitudeE6()) * 10e-6);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = R * c;

		return (d <= radius);
	}
}
