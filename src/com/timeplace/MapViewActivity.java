package com.timeplace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.android.maps.*;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

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
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// 50.9376967, -1.3980702
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        tasks = TimePlaceActivity.tasks;
        
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
    }
    
    public class GeoUpdateHandler implements LocationListener {

		public void onLocationChanged(Location location) {
			int lat = (int) (location.getLatitude() * 1E6);
			int lng = (int) (location.getLongitude() * 1E6);
			GeoPoint point = new GeoPoint(lat, lng);
			mapController.animateTo(point); //	mapController.setCenter(point);			
			try {
	    		locDb = TimePlaceActivity.db;
	    		for (Iterator<Task> iter = tasks.iterator(); iter.hasNext(); ){
	    			Task task = iter.next();
	    			if (task.getName() != "New task"){
		    			LocationDatabase taskDb = locDb.searchAboutType(task.getType());

		    			Iterator<PointOfInterest> DBiter = taskDb.iterator();
		    			
						while (DBiter.hasNext())
						{
							PointOfInterest poi = DBiter.next();
							itemizedOverlay.addOverlay(new OverlayItem(poi, poi.getLocationType(), poi.getOpeningTimes() + " - " + poi.getClosingTimes()));
						}
	    			}
	    		}
				
				mapOverlays.add(itemizedOverlay);
	    	}
	    	catch (Exception ex){
	    		message("onLocationChanged: " + ex.getClass().toString(), ex.getMessage());
	    	}
		}

		public void onProviderDisabled(String provider) {	}
		public void onProviderEnabled(String provider) {	}
		public void onStatusChanged(String provider, int status, Bundle extras) {	}
	}
    
    @Override
    protected boolean isRouteDisplayed() { return false; }
    
	private void message(String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.show();
	}
}