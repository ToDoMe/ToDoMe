package com.todome;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.maps.GeoPoint;

// Service will fetch location data from server and send it to the Activity

public class ToDoMeService extends Service implements LocationListener {
	private final String TAG = "ToDoMeService";

	// Data
	private ArrayList<Task> tasks;
	private LocationDatabase pointsOfInterest;

	private Location userCurrentLocation;

	private NotificationManager nm;
	private static boolean isRunning = false;

	ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track
	// of all
	// current
	// registered
	// clients.
	int mValue = 0; // Holds last value set by a client.
	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_UNREGISTER_CLIENT = 2;
	static final int MSG_TASKS_UPDATED = 3;
	static final int MSG_LOCATIONS_UPDATED = 4;
	static final int MSG_KEYWORDS_UPDATED = 5;
	final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target

	// we publish for clients to send messages to IncomingHandler.

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	class IncomingHandler extends Handler { // Handler of incoming messages from
		// clients.
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				Log.i(TAG, "Client registered.");
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			case MSG_TASKS_UPDATED:
				String tasksData = msg.getData().getString("str1");
				try {
					tasks = Util.getTaskListFromString(tasksData);
				} catch (Exception ex) {
					Log.e(TAG, ex.getClass().toString() + " " + ex.getMessage());
				}

				if (tasks != null) {
					Log.i(TAG, "MSG_TASKS_UPDATED received. tasks.size() = " + tasks.size());
				} else {
					Log.i(TAG, "MSG_TASKS_UPDATED received. tasks = null.");
				}
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private void sendDatabaseToUI(LocationDatabase db) {
		sendMessageToUI(Util.getLocationDatabaseString(db));
	}

	private void sendMessageToUI(String value) {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				// Send data as a String
				Bundle b = new Bundle();
				b.putString("str1", value);
				Message msg = Message.obtain(null, MSG_LOCATIONS_UPDATED);
				msg.setData(b);
				mClients.get(i).send(msg);
				// Log.i(TAG, "Sent message \"" + value + "\" to " + i);

			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going
				// through the list from back to front so this is safe to do
				// inside the loop.
				mClients.remove(i);
			}
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service Started.");
		isRunning = true;

		// Register LocationListener
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("MyService", "Received start id " + startId + ": " + intent);
		return START_STICKY; // run until explicitly stopped.
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		nm.cancel(R.string.service_started); // Cancel the persistent
		// notification.
		Log.i(TAG, "Service Stopped.");
		isRunning = false;
	}

	private void showNotification(ArrayList<Task> tasks, PointOfInterest poi) {
		Log.i(TAG, "Showing notification");
		Collections.sort(tasks, new TaskPriorityComparator());

		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.notification_icon, tasks.get(0).getName(), System.currentTimeMillis());
		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ToDoMeActivity.class), 0);
		// Set the info for the views that show in the notification panel.
		ArrayList<String> types = poi.getLocationTypes();
		notification.setLatestEventInfo(this, tasks.get(0).getName(), (types == null) ? "" : types.get(0), contentIntent);
		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to cancel.
		nm.notify(R.string.service_started, notification);
	}

	public static boolean isRunning() {
		return isRunning;
	}

	private LocationDatabase getLocationDatabase(GeoPoint point, int radius, String type) {
		Log.i(TAG, "Begining to get data from server, for " + Util.E6IntToDouble(point.getLatitudeE6()) + " " + Util.E6IntToDouble(point.getLongitudeE6()));

		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		double lat = point.getLatitudeE6() / 1e6;
		double lng = point.getLongitudeE6() / 1e6;
		String request = "http://ec2-176-34-195-131.eu-west-1.compute.amazonaws.com/locations.json?lat=" + lat + "&long=" + lng + "&radius=" + radius + "&type=" + type;
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

		LocationDatabase newLocDatabase = new LocationDatabase();

		try {
			JSONArray jsonArray = new JSONArray(builder.toString());

			Log.i(TAG, "Number of entries " + jsonArray.length());

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				try {
					newLocDatabase.add(new PointOfInterest((int) (jsonObject.getDouble("lat") * 1e6), (int) (jsonObject.getDouble("long") * 1e6), null, null, null, 10));
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage() + " for " + i + "/" + jsonArray.length(), e);
				}
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		Log.i(TAG, pointsOfInterest.print());

		return newLocDatabase;

	}

	/**
	 * This updates the central database with the relevant data from the server
	 */
	private void updateDatabase(HashSet<String> taskTypes) {
		if (pointsOfInterest == null) {
			pointsOfInterest = new LocationDatabase();
		} else {
			pointsOfInterest.clear();
		}
		Log.i(TAG, "Location database cleared");

		for (Iterator<String> iter = taskTypes.iterator(); iter.hasNext();) {
			String type = iter.next();
			Log.i(TAG, "Getting POIs for " + type);
			pointsOfInterest.addAll(getLocationDatabase(Util.locationToGeoPoint(userCurrentLocation), 100, type));
		}

		sendDatabaseToUI(pointsOfInterest);
	}

	void checkForReleventNotifications() {
		Log.i(TAG, "Checking for relevent notifications");
		updateDatabase(getAllTaskTypes());

		for (Iterator<PointOfInterest> iter = pointsOfInterest.findPointsWithinRadius(Util.locationToGeoPoint(userCurrentLocation), 0.5d).iterator(); iter.hasNext();) {
			PointOfInterest poi = iter.next();

			float dist = userCurrentLocation.distanceTo(Util.geoPointToLocation(poi.toGeoPoint()));
			if (dist < 100f) {
				//ArrayList<Task> releventTasks = getReleventTasks(poi);
				Log.i(TAG, "Distance from " + poi.toString() + " is " + dist + ". "/* + releventTasks.size() + " relevent tasks."*/);
				//if (releventTasks.size() > 0) {
					showNotification(/*releventTasks*/ tasks, poi);	// TODO Make releventTasks work
				//}
			} else {
				Log.i(TAG, "Distance from " + poi.toString() + " is " + dist);
			}
		}
	}

	ArrayList<Task> getReleventTasks(PointOfInterest poi) {
		Log.i(TAG, "getReleventTasktypes != nulls(" + poi.toString() + ")");
		
		ArrayList<Task> releventTasks = new ArrayList<Task>();
		for (Iterator<Task> iter = tasks.iterator(); iter.hasNext();) {
			Task task = iter.next();
			
			ArrayList<String> poiTypes = poi.getLocationTypes();
			ArrayList<String> taskTypes = task.getTypes();
			
			if (poiTypes != null && taskTypes != null && taskTypes.size() != 0 && poiTypes.size() != 0) {
				for (Iterator<String> taskTypesIter = taskTypes.iterator(); taskTypesIter.hasNext();) {
					
					String taskType = taskTypesIter.next();
					if (poiTypes.contains(taskType)) {
						releventTasks.add(task);
						break;
					}
				}
			}
		}
		return releventTasks;
	}

	HashSet<String> getAllTaskTypes() {

		Log.i(TAG, "Finding all task types " + tasks.size());
		HashSet<String> taskTypes = new HashSet<String>();

		for (Iterator<Task> iter = tasks.iterator(); iter.hasNext();) {
			Task task = iter.next();
			Log.i(TAG, "The task named \"" + task.getName() + "\" has types " + task.getTypes());
			taskTypes.addAll(task.getTypes());
		}
		Log.i(TAG, "Total of " + taskTypes.size() + " types returned");
		return taskTypes;
	}

	public void onLocationChanged(Location location) {
		// Log.i(TAG, "Location changed.");
		userCurrentLocation = location;
		checkForReleventNotifications();
		Log.i(TAG, "tasks.size() = " + tasks.size());
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
