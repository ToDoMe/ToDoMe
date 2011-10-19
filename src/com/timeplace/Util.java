package com.timeplace;

import com.google.android.maps.GeoPoint;

import android.location.Location;

public class Util {
	
	static int doubleToIntE6(double dub) {
		return (int) (dub * 1e6);
	}

	static double E6IntToDouble(int integer) {
		return (int) (integer / 1e6);
	}
	
	static GeoPoint locationToGeoPoint(Location loc) {
		return new GeoPoint(doubleToIntE6(loc.getLatitude()),doubleToIntE6(loc.getLongitude()));
	}
	
	static Location geoPointToLocation(GeoPoint point) {
		Location loc = new Location("");
		loc.setLatitude(E6IntToDouble(point.getLatitudeE6()));
		loc.setLongitude(E6IntToDouble(point.getLongitudeE6()));
		return loc;
	}
	
}
