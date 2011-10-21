package com.todome;

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

public class MapViewActivity extends MapActivity {
	/** Called when the activity is first created. */
	private ArrayList<Task> tasks;

	private MapController mapController;
	private MapView mapView;
	private MyLocationOverlay locOverlay;
	private LocationManager locationManager;
	private LocationDatabase locDb;
	private boolean haveLocation = false;
	private MapViewOverlay itemizedOverlay;
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
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new GeoUpdateHandler());

		// Overlays
		mapOverlays = mapView.getOverlays();
		drawable = this.getResources().getDrawable(R.drawable.androidmarker);
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

	private void message(String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.show();
	}
}