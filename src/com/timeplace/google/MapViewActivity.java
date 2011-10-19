package com.timeplace.google;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.timeplace.*;

public class MapViewActivity extends MapActivity {
	/** Called when the activity is first created. */

	private MapController mapController;
	private MapView mapView;
	private MyLocationOverlay locOverlay;
	private LocationManager locationManager;
	private boolean haveLocation = false;
	private MapViewOverlay itemizedOverlay;
	private List<Overlay> mapOverlays;

	private double hardcodedBeginLat = 50.896996;
	private double hardcodedBeginLong = -1.40416;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// 50.9376967, -1.3980702
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		// Enable zoom
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapController = mapView.getController();
		mapController.setZoom(14); // Zoom 1 is world view

		// Get LocationManager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new GeoUpdateHandler());

		// Overlays
		mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.androidmarker);
		itemizedOverlay = new MapViewOverlay(drawable, this);
		displayMapAt(new GeoPoint((int) (hardcodedBeginLat * 1e6), (int) (hardcodedBeginLong * 1e6)));
	}

	public class GeoUpdateHandler implements LocationListener {

		public void onLocationChanged(Location location) {
			int lat = (int) (location.getLatitude() * 1E6);
			int lng = (int) (location.getLongitude() * 1E6);
			GeoPoint point = new GeoPoint(lat, lng);
			mapController.animateTo(point); // mapController.setCenter(point);
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
		mapController.animateTo(point); // mapController.setCenter(point);
		try {
			/*
			 * locDb = TimePlaceActivity.pointsOfInterest; for (Iterator<Task>
			 * iter = tasks.iterator(); iter.hasNext();) { Task task =
			 * iter.next(); if (task.getName() != "New task") { LocationDatabase
			 * taskDb = locDb.searchAboutTypes(task.getTypes());
			 * 
			 * Iterator<PointOfInterest> DBiter = taskDb.iterator(); while
			 * (DBiter.hasNext()) { PointOfInterest poi = DBiter.next();
			 * itemizedOverlay.addOverlay(new OverlayItem(poi,
			 * poi.getLocationTypes().get(1),
			 * poi.getOpeningTimes()[getDayOfWeek()] + " - " +
			 * poi.getClosingTimes()[getDayOfWeek()])); } } }
			 */

			mapOverlays.add(itemizedOverlay);
		} catch (Exception ex) {
			message("onLocationChanged: " + ex.getClass().toString(), ex.getMessage());
		}
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

	private void message(String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.show();
	}
}