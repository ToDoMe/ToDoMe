package com.timeplace;

import java.io.Serializable;
import java.util.ArrayList;

import android.location.Location;
import android.text.format.Time;

import com.google.android.maps.GeoPoint;

public class PointOfInterest /*extends GeoPoint*/ implements Serializable {
	
	final ArrayList<String> locationTypes;
	final double radiusOfEffect;
	
	final Time[] openingTimes;
	final Time[] closingTimes;
	
	int latitudeE6, longitudeE6;
	public int getLatitudeE6() { return latitudeE6; }
	public int getLongitudeE6() { return longitudeE6; }
	
	public PointOfInterest(int latitude, int longitude, ArrayList<String> locationTypes, Time[] openingTimes, Time[] closingTimes, double radiusOfEffect) {
		//super(latitude,longitude);
		this.latitudeE6 = latitude;
		this.longitudeE6 = longitude;
		this.locationTypes = locationTypes;
		this.openingTimes = openingTimes;
		this.closingTimes = closingTimes;
		this.radiusOfEffect = radiusOfEffect;
	}
	
	public GeoPoint toGeoPoint() {
		return new GeoPoint(latitudeE6, longitudeE6);
	}
	
	public String toString() {
		return latitudeE6 + ", " + longitudeE6;
	}
	
	public ArrayList<String> getLocationTypes() {
		return locationTypes;
	}

	public double getRadiusOfEffect() {
		return radiusOfEffect;
	}

	public Time[] getOpeningTimes() {
		return openingTimes;
	}

	public Time[] getClosingTimes() {
		return closingTimes;
	}
}


