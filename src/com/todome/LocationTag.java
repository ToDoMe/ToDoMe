package com.todome;

import com.google.android.maps.GeoPoint;

public class LocationTag extends Tag {
	public final GeoPoint point;

	public LocationTag(GeoPoint point) {
		super(getStringFromGeoPoint(point));
		this.point = point;
	}

	public static String getStringFromTag(LocationTag tag) {
		return getStringFromGeoPoint(tag.point);
	}

	public static String getStringFromGeoPoint(GeoPoint point) {
		return "location: " + Util.E6IntToDouble(point.getLatitudeE6()) + " " + Util.E6IntToDouble(point.getLongitudeE6());
	}
}
