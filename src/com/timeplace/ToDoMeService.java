package com.timeplace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.timeplace.gui.TaskActivity;

public class ToDoMeService extends Service implements LocationListener {

	private ToDoMeDatabaseAdapter dbHelper;

	public boolean running = false;

	private static final String TAG = "ToDoMe-" + Service.class.getSimpleName();

	private Timer timer;
	private int icon = R.drawable.icon;
	private Notification notification;

	Location userCurrentLocation;

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		ToDoMeService getService() {
			return ToDoMeService.this;
		}
	}

	private TimerTask updateTask = new TimerTask() {
		@Override
		public void run() {
			Log.i(TAG, "Timer task doing work");

			userCurrentLocation = new Location("");
			userCurrentLocation.setLatitude(50.896995f);
			userCurrentLocation.setLongitude(-1.40416);
			checkForReleventNotifications();

			// notification.setLatestEventInfo(context, "ToDoMe Reminder", name,
			// contentIntent);
			// notification.setLatestEventInfo(context, "ToDoMe Reminder",
			// "Task name", contentIntent);
			// nm.notify(1, notification);

			int num = dbHelper.fetchAllTasks().getColumnCount();
			Log.i(TAG, "Fetching tasks from service " + num);
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "Service just bound to");
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service creating");

		running = true;

		// Database Test
		dbHelper = new ToDoMeDatabaseAdapter(this);
		dbHelper.open();

		dbHelper.createTask("Task 1", "Notes 1", "Postcode 1", 2);
		dbHelper.createTask("Task 2", "Notes 2", "Postcode 2", 4);

		// Notification example from
		// http://developer.android.com/guide/topics/ui/notifiers/notifications.html
		// timer = new Timer("TimePlaceNotificationTimer");
		// timer.schedule(updateTask, 1000L, 60 * 1000L);
		// notification = new Notification(icon, "Hello there!",
		// System.currentTimeMillis());
		// notification.defaults |= Notification.DEFAULT_SOUND; // Adds sound
		// notification.icon = R.drawable.notification_icon;
		// notification.defaults |= Notification.DEFAULT_VIBRATE;

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v("StartServiceAtBoot", "StartAtBootService -- onStartCommand()");
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Service destroying");
		timer.cancel();
		timer = null;
	}

	/*
	 * public void notificationPopup(Task task) {
	 * notification.setLatestEventInfo(context, "ToDoMe Reminder", task
	 * .getName(), contentIntent); }
	 */

	private void getLocationDatabase(GeoPoint point, int radius, String type) {
		Log.i(TAG, "Begining to get data from server, for " + Util.E6IntToDouble(point.getLatitudeE6()) + " " + Util.E6IntToDouble(point.getLongitudeE6()));

		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		double lat = point.getLatitudeE6() / 1e6;
		double lng = point.getLongitudeE6() / 1e6;
		String request = "http://ec2-176-34-195-131.eu-west-1.compute.amazonaws.com/locations.json?lat=" + lat + "&long=" + lng + "&radius=" + radius
				+ "&type=" + type;
		HttpGet httpGet = new HttpGet(request);
		Log.i(TAG, "Request used: " + request);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e("", "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			JSONArray jsonArray = new JSONArray(builder.toString());

			Log.i(TAG, "Number of entries " + jsonArray.length());

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				// try {
				// newLocDatabase.add(new PointOfInterest((int)
				// (jsonObject.getDouble("lat") * 1e6), (int)
				// (jsonObject.getDouble("long") * 1e6), null, null,
				// null, 10));
				// } catch (JSONException e) {
				// Log.e(TAG, e.getMessage() + " for " + i + "/" +
				// jsonArray.length(), e);
				// }
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * This updates the central database with the relevant data from the server
	 */
	private void updateDatabase(HashSet<String> taskTypes) {

		for (Iterator<String> iter = taskTypes.iterator(); iter.hasNext();) {
			String type = iter.next();
			Log.i(TAG, "Getting tasks for " + type);
			// pointsOfInterest.addAll(getLocationDatabase(Util.locationToGeoPoint(userCurrentLocation),
			// 100, type));
		}

	}

	void checkForReleventNotifications() {
		Log.i(TAG, "Checking for relevent notifications");
		updateDatabase(getAllTaskTypes());

		/*
		 * for (Iterator<PointOfInterest> iter = pointsOfInterest.iterator();
		 * iter.hasNext();) { PointOfInterest poi = iter.next();
		 * 
		 * float dist =
		 * userCurrentLocation.distanceTo(Util.geoPointToLocation(poi));
		 * Log.i(TAG, "Distance from " + poi.toString() + " is " + dist); }
		 */
	}

	HashSet<String> getAllTaskTypes() {

		// Log.i(TAG, "Finding all task types " + tasks.size());
		HashSet<String> taskTypes = new HashSet<String>();

		/*
		 * for (Iterator<Task> iter = tasks.iterator(); iter.hasNext();) { Task
		 * task = iter.next(); Log.i(TAG, "Looking at task " + task.getName());
		 * Log.i(TAG, "It has types " + task.getTypes());
		 * taskTypes.addAll(task.getTypes()); }
		 */
		Log.i(TAG, "Total of " + taskTypes.size() + " returned");
		return taskTypes;
	}

	public void onLocationChanged(Location location) {
		userCurrentLocation = location;
		checkForReleventNotifications();
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}
