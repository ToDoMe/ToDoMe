package com.timeplace;

import java.io.Serializable;
import java.util.ArrayList;

import android.location.Location;
import android.text.format.Time;

import com.google.android.maps.GeoPoint;

public class PointOfInterest extends GeoPoint implements Serializable {
	
	final ArrayList<String> locationTypes;
	final double radiusOfEffect;
	
	final Time[] openingTimes;
	final Time[] closingTimes;
	
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

	public PointOfInterest(int latitude, int longitude, ArrayList<String> locationTypes, Time[] openingTimes, Time[] closingTimes, double radiusOfEffect) {
		super(latitude,longitude);
		this.locationTypes = locationTypes;
		this.openingTimes = openingTimes;
		this.closingTimes = closingTimes;
		this.radiusOfEffect = radiusOfEffect;
	}
}


