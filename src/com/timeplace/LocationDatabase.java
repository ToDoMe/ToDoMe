package com.timeplace;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import android.app.AlertDialog;

import com.google.android.maps.GeoPoint;

public class LocationDatabase extends ArrayList<PointOfInterest> {

	public LocationDatabase searchAboutType(String type) {
		LocationDatabase subset = new LocationDatabase();
		
		for (Iterator<PointOfInterest> iter = this.iterator(); iter.hasNext(); ) {
			PointOfInterest location  = iter.next();
			
			if (location.locationType.equals(type)) {
				subset.add(location);
			}
		}
		
		return subset;
	}
	
	public LocationDatabase searchAboutGeoPoint(GeoPoint location, double km_radius) {
		LocationDatabase results = new LocationDatabase();
		
		int R = 6371; // radius of Earth in km
		
		for (int i = 0; i < this.size(); i++) {
			PointOfInterest database_entry = this.get(i);
		
			// Implemented from code at http://www.movable-type.co.uk/scripts/latlong.html
			double lat2 = Math.toRadians(database_entry.getLatitudeE6() * 10e6);
			double lat1 = Math.toRadians(location.getLatitudeE6() * 10e6);
			
			double dLat = lat2 - lat1;
			double dLon = Math.toRadians((database_entry.getLongitudeE6() - location.getLongitudeE6()) * 10e6);

			double a = Math.sin(dLat /2 ) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2); 
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)); 
			double d = R * c;
		
			if (d <= km_radius) {
				results.add(database_entry);
			}
		}
		
		return results;
	}
}
