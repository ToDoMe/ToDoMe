package com.timeplace;

import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TestTabActivity extends Activity {
	private TextView startTV;
	private TextView endTV;
	private Button readyButton;
	
	private LocationManager lm;
	private GeoUpdateHandler handler;
	
	private boolean started = false;
	private Time t1;
	private Location p1;
	private Time t2;
	private Location p2;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.test_tab);

			lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			handler = new GeoUpdateHandler();
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, handler);
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 100, handler);
			
			startTV = (TextView) findViewById(R.id.startTV);
			endTV = (TextView) findViewById(R.id.endTV);
			
			readyButton = (Button) findViewById(R.id.readyButton);
			readyButton.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					startTV.setText("Waiting for fix...");
					started = true;
				}
			});
			
			// Service
			Button startServB = (Button) findViewById(R.id.startServB);
			Button endServB = (Button) findViewById(R.id.endServB);
			
			startServB.setOnClickListener(new OnClickListener() {
				public void onClick(View v) { start(); }
			});
			
			endServB.setOnClickListener(new OnClickListener() {
				public void onClick(View v) { stop(); }
			});
			
		} catch (Exception ex) {
			message("TestTabActivity.onCreate: " + ex.getClass().toString(), ex.getMessage());
		}
	}
	
	private void start() {
		Log.i("ToDoMe::TestTabActivity", "About to start service");
		//Looper.prepareMainLooper();
		startService(new Intent(this, ToDoMeService.class));
	}
	
	private void stop() {
		stopService(new Intent(this, ToDoMeService.class));
	}
	
	private void message(String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.show();
	}
	
	public class GeoUpdateHandler implements LocationListener {

		public void onLocationChanged(Location location) {
			if (!started) return;
			if (p1 == null)
			{
				p1 = location;
				t1 = new Time();
				t1.setToNow();
				startTV.setText(p1.toString());
			}
			else if (p2 == null)
			{
				p2 = location;
				t2 = new Time();
				t2.setToNow();
				endTV.setText(p2.toString());
				started = false;
			}
		}

		public void onProviderDisabled(String provider) { }

		public void onProviderEnabled(String provider) { }

		public void onStatusChanged(String provider, int status, Bundle extras) { }
	}
}
