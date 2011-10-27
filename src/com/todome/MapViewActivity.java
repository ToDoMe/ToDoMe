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

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
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

	private MapController mapController;
	private MapView mapView;
	private LocationManager locationManager;
	private GeoUpdateHandler guh;
	private MapViewOverlay itemizedOverlay, locOverlay;
	private List<Overlay> mapOverlays;

	private int hardcodedBeginLat = Util.doubleToIntE6(50.937319d);
	private int hardcodedBeginLong = Util.doubleToIntE6(-1.397788d);

	private static MapViewActivity instance;

	private Location lastKnownUserLocation;

	final static String TAG = "MapViewActivity";

	public static MapViewActivity getInstance() {
		return instance;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		instance = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		// Enable zoom
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapController = mapView.getController();
		mapController.setZoom(17); // Zoom 1 is world view

		// Get LocationManager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		guh = new GeoUpdateHandler();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, guh);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, guh);

		//int beginLat = ToDoMeActivity.prefs.getInt("lat", hardcodedBeginLat);
		int beginLat = hardcodedBeginLat;
		//int beginLong = ToDoMeActivity.prefs.getInt("lon", hardcodedBeginLong);
		int beginLong = hardcodedBeginLong;

		Log.i(TAG, "Map Begin lat " + Util.E6IntToDouble(beginLat) + " long " + Util.E6IntToDouble(beginLong) + " coded default ("
				+ Util.E6IntToDouble(hardcodedBeginLat) + " " + Util.E6IntToDouble(hardcodedBeginLong) + ")");

		// Overlays
		mapOverlays = mapView.getOverlays();
		GeoPoint lastLocation = new GeoPoint(beginLat, beginLong);
		displayMapAt(lastLocation);
	}

	public void notifyLocationsUpdated() {
		displayMapAt(mapView.getMapCenter());
	}

	@Override
	public void onPause() {
		Log.i(TAG, "Map pausing");
		// Disable GPS to save battery
		locationManager.removeUpdates(guh);
		ToDoMeActivity.getInstance().sendMessageToService(ToDoMeService.MSG_MAP_MODE_DISABLE);
		super.onPause();
	}

	@Override
	public void onResume() {
		// Enable GPS again
		Log.i(TAG, "Map resuming");
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, guh);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, guh);
		ToDoMeActivity.getInstance().sendMessageToService(ToDoMeService.MSG_MAP_MODE_ENABLE);
		mapController.setZoom(17); // Zoom 1 is world view
		refreshMap();
		super.onResume();
	}

	public void refreshMap() {
		displayMapAt(mapView.getMapCenter());
	}

	public class GeoUpdateHandler implements LocationListener {

		public void onLocationChanged(Location location) {
			lastKnownUserLocation = location;
			GeoPoint point = Util.locationToGeoPoint(location);
			displayMapAt(point);
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	void displayMapAt(GeoPoint point) {
		mapOverlays.clear();

		itemizedOverlay = new MapViewOverlay(getResources().getDrawable(R.drawable.point_of_interest), this);
		for (Iterator<Task> iter = ToDoMeActivity.tasks.iterator(); iter.hasNext();) {
			Task task = iter.next();
			Log.i("MapViewActivity", "Looking at task, " + task.getName());
			LocationDatabase releventPOIs = ToDoMeActivity.db.searchAboutTypes(task.getTypes());

			displayPOIs(releventPOIs);
		}

		if (lastKnownUserLocation != null) {
			locOverlay = new MapViewOverlay(getResources().getDrawable(R.drawable.current_location), MapViewActivity.this);
			locOverlay.addOverlay(new OverlayItem(Util.locationToGeoPoint(lastKnownUserLocation), "You are here", ""));

			mapOverlays.add(locOverlay);
		}
		mapOverlays.add(itemizedOverlay);

		mapController.animateTo(point);
		mapView.invalidate();
	}

	private void displayPOIs(HashSet<PointOfInterest> releventPOIs) {
		for (Iterator<PointOfInterest> releventPOIsIter = releventPOIs.iterator(); releventPOIsIter.hasNext();) {
			PointOfInterest poi = releventPOIsIter.next();
			Log.i("MapViewActivity", "Found relevent POI: " + Util.E6IntToDouble(poi.getLatitudeE6()) + " " + Util.E6IntToDouble(poi.getLongitudeE6()));

			String types = "";
			for (Iterator<String> typesIter = poi.getLocationTypes().iterator(); typesIter.hasNext();) {
				String type = typesIter.next();
				Log.i("MapViewActivity", "Found type " + type);
				types = types + " " + type;
			}

			String name = ToDoMeActivity.keywords.getDescriptionForType(types.split(" ")[0]);

			if (poi.getClosingTimes() != null) {
				itemizedOverlay.addOverlay(new OverlayItem(poi.toGeoPoint(), name, poi.getOpeningTimes()[getDayOfWeek()] + " - "
						+ poi.getClosingTimes()[getDayOfWeek()]));
			} else {
				itemizedOverlay.addOverlay(new OverlayItem(poi.toGeoPoint(), name, ""));
			}
		}

	}

	static int getDayOfWeek() {
		Calendar rightNow = Calendar.getInstance();

		int DOW = rightNow.get(Calendar.DAY_OF_WEEK) - 2; // fix to get Monday = 0

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