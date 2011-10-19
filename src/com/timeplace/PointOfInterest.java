package com.timeplace;

import android.text.format.Time;

import com.google.android.maps.GeoPoint;

public class PointOfInterest extends GeoPoint {
	
	final String[] locationType;
	final double radiusOfEffect;
	
	final Time[] openingTimes;
	final Time[] closingTimes;
	
	public String[] getLocationType() {
		return locationType;
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

	public PointOfInterest(int latitude, int longitude, String[] locationType, Time[] openingTimes, Time[] closingTimes, double radiusOfEffect) {
		super(latitude, longitude);
		this.locationType = locationType;
		this.openingTimes = openingTimes;
		this.closingTimes = closingTimes;
		this.radiusOfEffect = radiusOfEffect;
	}
}


