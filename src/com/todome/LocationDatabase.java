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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.Time;

import com.google.android.maps.GeoPoint;

public class LocationDatabase extends HashSet<PointOfInterest> implements Serializable {

	public LocationDatabase() {
		super();
	}
	
	//public int userLatitudeE6, userLongitudeE6;

	public LocationDatabase searchAboutTypes(ArrayList<String> type) {
		LocationDatabase subset = new LocationDatabase();

		for (Iterator<PointOfInterest> iter = this.iterator(); iter.hasNext();) {
			PointOfInterest poi = iter.next();

			for (int i = 0; i < type.size(); i++) {
				ArrayList<String> types = poi.getLocationTypes();
				if (types != null) {
					for (int j = 0; j < types.size(); j++) {
	
						if (types.get(j).equals(type.get(i))) {
							subset.add(poi);
						}
					}
				}
			}
		}

		return subset;
	}

	public LocationDatabase findPointsWithinRadius(GeoPoint location, double km_radius) {
		return findPointsWithinRadiusOfLocDB(location, km_radius, this);
	}

	private static LocationDatabase findPointsWithinRadiusOfLocDB(GeoPoint location, double km_radius, LocationDatabase db) {
		LocationDatabase results = new LocationDatabase();

		Iterator<PointOfInterest> iter = db.iterator();

		while (iter.hasNext()) {
			PointOfInterest database_entry = iter.next();
			GeoPoint database_point = new GeoPoint(database_entry.getLatitudeE6(), database_entry.getLongitudeE6());

			if (isPointsWithinRange(location, database_point, km_radius))
				results.add(database_entry);
		}

		return results;
	}

	public LocationDatabase findTriggeredPoints(GeoPoint location, double range) {
		LocationDatabase results = new LocationDatabase();

		LocationDatabase searchArea = findPointsWithinRadius(location, range);
		Iterator<PointOfInterest> iter = searchArea.iterator();

		while (iter.hasNext()) {
			PointOfInterest database_entry = iter.next();
			GeoPoint database_point = new GeoPoint(database_entry.getLatitudeE6(), database_entry.getLongitudeE6());

			if (isPointsWithinRange(location, database_point, database_entry.getRadiusOfEffect()))
				results.add(database_entry);
		}

		return results;
	}

	private static boolean isPointsWithinRange(GeoPoint point1, GeoPoint point2, double radius) {
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
	
	public static long calculateTimeDeltaInMilliseconds(GeoPoint location, PointOfInterest POI) throws MalformedURLException, IOException, JSONException {
		String origin = Double.toString((location.getLatitudeE6() * 10e-6)) + "," + Double.toString((location.getLongitudeE6() * 10e-6));
		String destination = Double.toString((POI.getLatitudeE6() * 10e-6)) + "," + Double.toString((POI.getLongitudeE6() * 10e-6));
		
		URL query = new URL("http://maps.googleapis.com/maps/api/distancematrix/json?origins=" + origin + "&destinations=" + destination + "&mode=walking&units=metric&sensor=true");
		InputStreamReader reader = new InputStreamReader(query.openStream());
		
		final char[] buffer = new char[0x10000];
		StringBuilder out = new StringBuilder();
		int read;
		do {
		  read = reader.read(buffer, 0, buffer.length);
		  if (read>0) {
		    out.append(buffer, 0, read);
		  }
		} while (read>=0);
		
		JSONObject jsonObject = new JSONObject(out.toString());
		
		int time = jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("duration").getInt("value");
			
		Time closingTime = POI.closingTimes[getDayOfWeek()];
		
		return ((closingTime.toMillis(false) - Calendar.getInstance().getTimeInMillis()));
	}
	
	public static int getDayOfWeek() {
		Calendar rightNow = Calendar.getInstance();
		int DOW = rightNow.get(Calendar.DAY_OF_WEEK);
		
		if (DOW == 1) return 6; else return DOW - 2; // fixes DOW
	}

	String print() {
		StringBuilder builder = new StringBuilder();
		for (Iterator<PointOfInterest> iter = this.iterator(); iter.hasNext();) {
			PointOfInterest point = iter.next();
			builder.append(point.getLatitudeE6() + "," + point.getLongitudeE6() + "\n");
		}
		return builder.toString();
	}
}
