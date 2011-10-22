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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

	private SharedPreferences prefs;

	// Data
	private ArrayList<Task> tasks;
	private LocationDatabase pointsOfInterest;
	private KeywordDatabase keywords;

	public void loadTasks() {
		try {
			String str = prefs.getString("tasks", "");
			if (str != null && str != "") {
				tasks = Util.getTaskListFromString(str);
			}
		} catch (Exception ex) {
			Log.e(TAG, "", ex);
		}

		if (this.tasks.size() == 0) {
			// Disable GPS to save battery
			disableNotifications();
		} else {
			// Enable GPS again
			enableNotifications();
		}
	}

	private Location userCurrentLocation;

	private NotificationManager nm;
	private LocationManager locationManager;
	private static boolean isRunning = false;

	private boolean enabled = true;

	/*
	 * This array list contains the id's of the point of interests, that have been notified for the current location. To keep this up to date, id's are added as
	 * notifications are shown, and removed when the user location is updated.
	 */
	HashSet<PointOfInterest> notifiedPOIs = new HashSet<PointOfInterest>();

	ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
	int mValue = 0; // Holds last value set by a client.
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_LOCATIONS_UPDATED = 3;
	public static final int MSG_KEYWORDS_UPDATED = 4;
	public static final int MSG_TASKS_UPDATED = 5;
	public static final int MSG_QUERY_ENABLED = 6;
	public static final int MSG_ENABLE = 7;
	public static final int MSG_DISABLE = 8;
	final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target

	// we publish for clients to send messages to IncomingHandler.

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service Started.");

		isRunning = true;

		// Register LocationListener
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, ToDoMeActivity.LOC_INTERVAL, 0, this);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, ToDoMeActivity.LOC_INTERVAL, 0, this);

		// Load tasks
		prefs = getSharedPreferences("Tasks", MODE_PRIVATE);
		loadTasks();
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

	private void showNotification(ArrayList<Task> notifyTasks, PointOfInterest poi) {
		Log.i(TAG, "Showing notification");
		Collections.sort(notifyTasks, new TaskPriorityComparator());

		Log.i(TAG, "Got " + notifyTasks.size() + " tasks");

		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.notification_icon, notifyTasks.get(0).getName(), System.currentTimeMillis());
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ToDoMeActivity.class).putExtra("displayMap", true), 0);
		// Set the info for the views that show in the notification panel.
		String message = "";

		message = "You are near a ";

		HashSet<String> taskTypes = notifyTasks.get(0).getTypes();
		HashSet<String> locationTypes = poi.getLocationTypes();
		HashSet<String> typesIntersection = new HashSet<String>();

		typesIntersection.addAll(taskTypes);
		typesIntersection.retainAll(locationTypes);

		for (Iterator<String> iter = typesIntersection.iterator(); iter.hasNext();) {
			message = message + iter.next() + " ";
		}
		notification.setLatestEventInfo(this, notifyTasks.get(0).getName(), message, contentIntent);

		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to cancel.
		nm.notify(R.string.service_started, notification);
	}

	public static boolean isRunning() {
		return isRunning;
	}

	private LocationDatabase getLocationDatabase(GeoPoint point, int radius, String type) {
		Log.i(TAG, "Beginning to get data from server, for " + Util.E6IntToDouble(point.getLatitudeE6()) + " " + Util.E6IntToDouble(point.getLongitudeE6()));

		double lat = point.getLatitudeE6() / 1e6;
		double lng = point.getLongitudeE6() / 1e6;

		String request = "http://ec2-176-34-195-131.eu-west-1.compute.amazonaws.com/locations.json?lat=" + lat + "&long=" + lng + "&radius=" + radius
				+ "&type=" + type;
		String file = Util.getFileFromServer(request);
		Log.i(TAG, "File for " + type + " is " + file.length());

		LocationDatabase newLocDatabase = new LocationDatabase();

		try {
			JSONArray jsonArray = new JSONArray(file);

			Log.i(TAG, "Number of entries " + jsonArray.length());

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				try {
					JSONObject locationTypes = jsonObject.getJSONObject("location_type");
					HashSet<String> types = new HashSet<String>();
					// Log.i(TAG, "About to parse location types");
					for (Iterator<String> iter = locationTypes.keys(); iter.hasNext();) {
						String key = iter.next();
						// Log.i(TAG, "Got key " + key);
						String value = locationTypes.get(key).toString();
						// Log.i(TAG, "Got value " + value);
						types.add(value);
					}
					newLocDatabase.add(new PointOfInterest((int) (jsonObject.getDouble("lat") * 1e6), (int) (jsonObject.getDouble("long") * 1e6), types, null,
							null, 10));
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

		if (taskTypes.size() == 0) {
			/*keywords = KeywordDatabase.fromServer();
			for (Iterator<String> iter = keywords.getAllTypes().iterator(); iter.hasNext();) {
				String type = iter.next();
				Log.i(TAG, "Getting POIs for " + type);
				pointsOfInterest.addAll(getLocationDatabase(Util.locationToGeoPoint(userCurrentLocation), 100, type));
			}*/
		} else {
			for (Iterator<String> iter = taskTypes.iterator(); iter.hasNext();) {
				String type = iter.next();
				Log.i(TAG, "Getting POIs for " + type);
				pointsOfInterest.addAll(getLocationDatabase(Util.locationToGeoPoint(userCurrentLocation), 100, type));
			}
		}

		sendDatabaseToUI(pointsOfInterest);
	}

	void checkForReleventNotifications() {
		Log.i(TAG, "Checking for relevent notifications");
		updateDatabase(getAllTaskTypes());

		for (Iterator<PointOfInterest> iter = pointsOfInterest.findPointsWithinRadius(Util.locationToGeoPoint(userCurrentLocation), 0.5d).iterator(); iter
				.hasNext();) {
			PointOfInterest poi = iter.next();

			if (!notifiedPOIs.contains(poi)) {

				float dist = userCurrentLocation.distanceTo(Util.geoPointToLocation(poi.toGeoPoint()));
				if (dist < 100f) {
					// ArrayList<Task> releventTasks = getReleventTasks(poi);
					Log.i(TAG, "Distance from " + poi.toString() + " is " + dist + ". "/* + releventTasks.size() + " relevent tasks." */);
					// if (releventTasks.size() > 0) {
					Log.w(TAG, "Added " + poi.getLatitudeE6() + " " + poi.getLongitudeE6() + " " + notifiedPOIs.size());
					notifiedPOIs.add(poi);
					showNotification(/* releventTasks */tasks, poi); // TODO Make releventTasks work
					// }
				} else {
					Log.i(TAG, "Distance from " + poi.toString() + " is " + dist);
				}
			} else {
				Log.i(TAG, "Not notifying for " + poi.getLatitudeE6() + " " + poi.getLongitudeE6() + " as it has been notified for in this location already");
			}
		}
	}

	ArrayList<Task> getReleventTasks(PointOfInterest poi) {
		// Log.i(TAG, "getReleventTasktypes != nulls(" + poi.toString() + ")");

		ArrayList<Task> releventTasks = new ArrayList<Task>();
		for (Iterator<Task> iter = tasks.iterator(); iter.hasNext();) {
			Task task = iter.next();

			HashSet<String> poiTypes = poi.getLocationTypes();
			HashSet<String> taskTypes = task.getTypes();

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

	/*
	 * This method removes all poi's that are not close to the users current location
	 */
	private void updateNotifiedPOIs() {
		double distance = 0.1d; // meters

		if (pointsOfInterest == null)
			return;

		// Look at each of the points of interest that have been notified for
		for (Iterator<PointOfInterest> iter = notifiedPOIs.iterator(); iter.hasNext();) {
			PointOfInterest poi = iter.next();

			// If the point of interest is now "distance" or more away from the users current location, then remove it so more notifications can be given
			if (!Util.isPointsWithinRange(Util.locationToGeoPoint(userCurrentLocation), poi.toGeoPoint(), distance)) {
				Log.e(TAG, "Removed " + poi.toGeoPoint() + " dist " + Util.getDistanceBetween(Util.locationToGeoPoint(userCurrentLocation), poi.toGeoPoint()));
				iter.remove();
			}
		}
	}

	HashSet<String> getAllTaskTypes() {

		// Log.i(TAG, "Finding all task types " + tasks.size());
		HashSet<String> taskTypes = new HashSet<String>();

		for (Iterator<Task> iter = tasks.iterator(); iter.hasNext();) {
			Task task = iter.next();
			Log.i(TAG, "The task named \"" + task.getName() + "\" has types " + task.getTypes());
			HashSet<String> thisTaskTypes = task.getTypes();
			if (thisTaskTypes != null) {
				taskTypes.addAll(task.getTypes());
			}
		}
		// Log.i(TAG, "Total of " + taskTypes.size() + " types returned");
		return taskTypes;
	}

	// Messaging

	class IncomingHandler extends Handler { // Handler of incoming messages from clients.
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
				loadTasks();
				Log.i(TAG, "MSG_TASKS_UPDATED received.");
				break;
			case MSG_QUERY_ENABLED:
				sendQueryResponse();
				break;
			case MSG_ENABLE:
				enableNotifications();
				break;
			case MSG_DISABLE:
				disableNotifications();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private void sendQueryResponse() {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				Message msg = Message.obtain(null, MSG_QUERY_ENABLED);
				msg.arg1 = enabled ? 1 : 0;
				mClients.get(i).send(msg);

			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going through the list from back to front
				// so this is safe to do inside the loop.
				mClients.remove(i);
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

			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going through the list from back to front
				// so this is safe to do inside the loop.
				mClients.remove(i);
			}
		}
	}

	// LocationListener

	public void onLocationChanged(Location location) {
		Log.i(TAG, "Location changed.");
		userCurrentLocation = location;
		updateNotifiedPOIs();

		// Save new location
		prefs.edit().putLong("lat", (long) (location.getLatitude() * 1e6)).putLong("lon", (long) (location.getLongitude() * 1e6)).commit();

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

	// Enable/disable notifications

	private void disableNotifications() {
		locationManager.removeUpdates(this);
		enabled = true;
	}

	private void enableNotifications() {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, ToDoMeActivity.LOC_INTERVAL, 0, this);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, ToDoMeActivity.LOC_INTERVAL, 0, this);
		enabled = false;
	}
}
