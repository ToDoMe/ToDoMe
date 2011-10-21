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

import java.io.Serializable;
import java.util.ArrayList;

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


