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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MapViewActivity extends MapActivity {
	/** Called when the activity is first created. */
	private ArrayList<Task> tasks;

	private MapController mapController;
	private MapView mapView;
	private LocationManager locationManager;
	private LocationDatabase locDb;
	private GeoUpdateHandler guh;
	private MapViewOverlay itemizedOverlay, locOverlay;
	private List<Overlay> mapOverlays;
	private Drawable drawable;

	private double hardcodedBeginLat = 50.896996;
	private double hardcodedBeginLong = -1.40416;

	private static MapViewActivity instance;

	public static MapViewActivity getInstance() {
		return instance;
	}

	public void notifyLocationsUpdated() {
		LocationDatabase db = ToDoMeActivity.db;
		itemizedOverlay = new MapViewOverlay(drawable, this);
		for (Iterator<PointOfInterest> iter = db.iterator(); iter.hasNext();) {
			PointOfInterest poi = iter.next();
			ArrayList<String> types = poi.getLocationTypes();

			OverlayItem item = new OverlayItem(poi.toGeoPoint(), (types == null) ? "Point of interest" : types.toString(), "");
			// TODO Display opening and closing times

			itemizedOverlay.addOverlay(item);
		}
		mapOverlays.clear();
		if (locOverlay != null) mapOverlays.add(locOverlay);
		mapOverlays.add(itemizedOverlay);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		instance = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		tasks = ToDoMeActivity.tasks;

		// Enable zoom
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapController = mapView.getController();
		mapController.setZoom(14); // Zoom 1 is world view

		// Get LocationManager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		guh = new GeoUpdateHandler();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,	 0, 0, guh);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, guh);

		// Overlays
		mapOverlays = mapView.getOverlays();
		drawable = this.getResources().getDrawable(R.drawable.androidmarker);
		itemizedOverlay = new MapViewOverlay(drawable, this);
		displayMapAt(new GeoPoint((int) (hardcodedBeginLat * 1e6), (int) (hardcodedBeginLong * 1e6)));
	}
	
	@Override
	public void onPause() {
		// Disable GPS to save battery
		locationManager.removeUpdates(guh);
		super.onPause();
	}
	
	@Override
	public void onResume() {
		// Enable GPS again
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,	 0, 0, guh);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, guh);
		super.onResume();
	}

	public class GeoUpdateHandler implements LocationListener {

		public void onLocationChanged(Location location) {
			int lat = (int) (location.getLatitude() * 1E6);
			int lng = (int) (location.getLongitude() * 1E6);
			GeoPoint point = new GeoPoint(lat, lng);
			mapController.animateTo(point); // mapController.setCenter(point);
			locOverlay = new MapViewOverlay(getResources().getDrawable(R.drawable.current_location), MapViewActivity.this);
			locOverlay.addOverlay(new OverlayItem(point, "You are here", ""));
			mapOverlays.add(locOverlay);
			displayMapAt(point);
		}

		public void onProviderDisabled(String provider) { }

		public void onProviderEnabled(String provider) { }

		public void onStatusChanged(String provider, int status, Bundle extras) { }
	}

	void displayMapAt(GeoPoint point) {
		mapController.animateTo(point); // mapController.setCenter(point);
		locDb = ToDoMeActivity.db;
		for (Iterator<Task> iter = tasks.iterator(); iter.hasNext();) {
			Task task = iter.next();
			if (task.getName() != "New task") {
				LocationDatabase taskDb = locDb.searchAboutTypes(task.getTypes());

				Iterator<PointOfInterest> DBiter = taskDb.iterator();

				while (DBiter.hasNext()) {
					PointOfInterest poi = DBiter.next();
					itemizedOverlay.addOverlay(new OverlayItem(poi.toGeoPoint(), poi.getLocationTypes().get(1), poi.getOpeningTimes()[getDayOfWeek()] + " - "
							+ poi.getClosingTimes()[getDayOfWeek()]));
				}
			}
		}

		mapOverlays.add(itemizedOverlay);
	}

	static int getDayOfWeek() {
		Calendar rightNow = Calendar.getInstance();

		int DOW = rightNow.get(Calendar.DAY_OF_WEEK) - 2; // fix to get Monday =
															// 0

		if (DOW > -1)
			return DOW;
		else
			return 6;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}