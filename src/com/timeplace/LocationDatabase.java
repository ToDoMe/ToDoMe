package com.timeplace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import com.google.android.maps.GeoPoint;

public class LocationDatabase extends HashSet<PointOfInterest> {

	public LocationDatabase() {
		super();
	}

	public LocationDatabase searchAboutTypes(ArrayList<String> type) {
		LocationDatabase subset = new LocationDatabase();

		for (Iterator<PointOfInterest> iter = this.iterator(); iter.hasNext();) {
			PointOfInterest location = iter.next();

			for (int i = 0; i < type.size(); i++) {

				for (int j = 0; j < location.locationTypes.size(); j++) {

					if (location.locationTypes.get(j).equals(type.get(i))) {
						subset.add(location);
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

		double lat2 = Math.toRadians(point2.getLatitudeE6() * 10e6);
		double lat1 = Math.toRadians(point1.getLatitudeE6() * 10e6);

		double dLat = lat2 - lat1;
		double dLon = Math.toRadians((point2.getLongitudeE6() - point1.getLongitudeE6()) * 10e6);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = R * c;

		return (d <= radius);
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
