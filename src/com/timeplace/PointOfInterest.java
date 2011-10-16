package com.timeplace;

import android.text.format.Time;

import com.google.android.maps.GeoPoint;

public class PointOfInterest extends GeoPoint {
	
	final String locationType;
	final String name;
	
	final Time openingTime;
	final Time closingTime;
	
	final String postcode;
	
	public String getLocationType() {
		return locationType;
	}

	public String getName() {
		return name;
	}

	public Time getOpeningTime() {
		return openingTime;
	}

	public Time getClosingTime() {
		return closingTime;
	}

	public String getPostcode() {
		return postcode;
	}

	public PointOfInterest(String postcode, int latitude, int longitude, Time openingTime, Time closingTime, String name, String type) {
		super(latitude, longitude);
		this.locationType = type;
		this.name = name;
		this.closingTime = closingTime;
		this.openingTime = openingTime;
		this.postcode = postcode; 
	}
}


