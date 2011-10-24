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
package uk.org.todome;

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
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;

import com.google.android.maps.GeoPoint;

// Service will fetch location data from server and send it to the Activity

public class ToDoMeService extends Service implements LocationListener {
	private final String TAG = "ToDoMeService";

	/**
	 * Used to store preferences for the app
	 */
	private SharedPreferences prefs;

	/**
	 * Used to store data
	 * 
	 * "tasks" for the tasks array "keywords" for the keywords database
	 */
	private SharedPreferences data;

	// Data
	private ArrayList<Task> tasks;
	private LocationDatabase pointsOfInterest;
	private KeywordDatabase keywords;

	private Location userCurrentLocation;

	private NotificationManager nm;
	private LocationManager locationManager;
	private static boolean isRunning = false;

	private boolean enabled = true;

	// MapMode relates to the frequency of location updates, in map mode these
	// happen as fast as possible
	private boolean mapMode = false;

	/**
	 * This array list contains the id's of the point of interests, that have been notified for the current location. To keep this up to date, id's are added as
	 * notifications are shown, and removed when the user location is updated.
	 */
	HashSet<PointOfInterest> notifiedPOIs = new HashSet<PointOfInterest>();

	/**
	 * Keeps track of all current registered clients.
	 */
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	int mValue = 0; // Holds last value set by a client.
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_LOCATIONS_UPDATED = 3;
	public static final int MSG_KEYWORDS_UPDATED = 4;
	public static final int MSG_TASKS_UPDATED = 5;
	public static final int MSG_QUERY_ENABLED = 6;
	public static final int MSG_ENABLE = 7;
	public static final int MSG_DISABLE = 8;
	public static final int MSG_MAP_MODE_ENABLE = 9;
	public static final int MSG_MAP_MODE_DISABLE = 10;
	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler()); //

	private Handler timedTasksHandler = new Handler();

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service Started.");

		isRunning = true;

		setLocationUpdateSettings();

		prefs = getSharedPreferences("prefs", MODE_PRIVATE);
		data = getSharedPreferences("data", MODE_PRIVATE);
		readKeywords();
		readTasks();
	}

	private void setLocationUpdateSettings() {
		if (mapMode) {
			// Register LocationListener
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		} else {
			// Register LocationListener
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, ToDoMeActivity.LOC_INTERVAL, 0, this);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, ToDoMeActivity.LOC_INTERVAL, 0, this);
		}
	}

	private void readTasks() {
		try {
			data = getSharedPreferences("data", MODE_PRIVATE); // For some reason this is required for the tasks list to be updated properly
			String str = data.getString("tasks", null);
			if (str != null) {
				tasks = Util.getTaskListFromString(str);
				Log.i(TAG, "Got " + tasks.size() + " from the shared preferences");

				if (this.tasks.size() == 0) {
					// Disable GPS to save battery
					disableNotifications();
				} else {
					// Enable GPS again
					enableNotifications();
				}

				checkForReleventNotifications();
			} else {
				Log.i(TAG, "Loaded tasks, but got null, populating database with empty task list");
				writeTasks(new ArrayList<Task>());
			}
		} catch (Exception ex) {
			Log.e(TAG, "", ex);
		}

	}

	private void writeTasks(ArrayList<Task> newTasks) {
		Editor dataEditor = data.edit();

		dataEditor.putString("tasks", Util.getTaskArrayString(newTasks));
		dataEditor.commit();
	}

	private void readKeywords() {
		Log.i(TAG, "Reading keywords.");
		try {
			data = getSharedPreferences("data", MODE_PRIVATE); // Added as per tasks above
			String str = data.getString("keywords", null);
			if (str != null) {
				keywords = Util.getKeywordDatabaseFromString(str);
				long keywordsDate = data.getLong("keywordsDate", -1);
				if (keywordsDate == -1 || keywords.size() == 0) {
					getKeywordDatabaseFromServer();
				} else {
					// Calculate the time since last update
					Time lastUpdate = new Time();
					lastUpdate.set(keywordsDate);
					Time now = new Time();
					now.setToNow();
					if ((now.toMillis(false) - lastUpdate.toMillis(false)) < 1.21E9 /*
																					 * 2 weeks in ms
																					 */) {
						writeKeywords(keywords);
					} else {
						Log.i(TAG, "Keywords out of date. Populating database with keywords from server.");
						getKeywordDatabaseFromServer();
					}
				}

			} else {
				Log.i(TAG, "Loaded keywords, but got null. Populating database with keywords from server.");
				getKeywordDatabaseFromServer();
			}
			Log.i(TAG, "keywords.size() = " + keywords.size() + ". Sending MSG_KEYWORDS_UPDATED");
			sendMessageToUI(MSG_KEYWORDS_UPDATED);
		} catch (Exception ex) {
			Log.e(TAG, "", ex);
		}
	}

	private void writeKeywords(KeywordDatabase newKeywords) {
		Editor dataEditor = data.edit();

		dataEditor.putString("keywords", Util.getKeywordDatabaseString(newKeywords));
		dataEditor.commit();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("MyService", "Received start id " + startId + ": " + intent);
		return START_STICKY; // run until explicitly stopped.
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		nm.cancel(R.string.service_started); // Cancel the persistent notification.
		Log.i(TAG, "Service Stopped.");
		isRunning = false;
	}

	private void showNotification(ArrayList<Task> notifyTasks, PointOfInterest poi) {
		Log.i(TAG, "Showing notification");
		Collections.sort(notifyTasks, new TaskPriorityComparator());

		Log.i(TAG, "Got " + notifyTasks.size() + " tasks");

		String tickerText = "";
		for (int i = 0; i < notifyTasks.size(); i++) {
			if (i != (notifyTasks.size() - 2)) {
				tickerText += notifyTasks.get(i).getName() + ", ";
			} else {
				tickerText += notifyTasks.get(i).getName() + " and ";
			}
		}

		Log.i(TAG, "TickerText " + tickerText);

		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.notification_icon, tickerText, System.currentTimeMillis());
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		// The PendingIntent to launch our activity if the user selects this notification
		Time taskTime = notifyTasks.get(0).getAlarmTime();
		PendingIntent contentIntent;
		if (taskTime != null) { // If a timed task, dont show the map
			contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ToDoMeActivity.class).putExtra("displayMap", false), 0);
		} else {
			contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ToDoMeActivity.class).putExtra("displayMap", true), 0);
		}
		// Set the info for the views that show in the notification panel.
		String message = "";

		message = "You are near a ";

		HashSet<String> taskTypes = new HashSet<String>();
		for (Iterator<Task> taskIter = notifyTasks.iterator(); taskIter.hasNext();) {
			taskTypes.addAll(taskIter.next().getTypes());
		}
		HashSet<String> locationTypes = poi.getLocationTypes();
		HashSet<String> typesIntersection = new HashSet<String>();

		typesIntersection.addAll(taskTypes);
		typesIntersection.retainAll(locationTypes);

		for (Iterator<String> iter = typesIntersection.iterator(); iter.hasNext();) {
			message = message + keywords.getDescriptionForType(iter.next()) + " ";
		}

		Log.i(TAG, "Message " + message);

		notification.setLatestEventInfo(this, tickerText, message, contentIntent);

		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to cancel.
		nm.notify(R.string.service_started, notification);
	}

	private void showNotification(Task notifyTask, PointOfInterest poi) {
		Log.i(TAG, "Showing notification");

		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.notification_icon, notifyTask.getName(), System.currentTimeMillis());
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;

		Time taskTime = notifyTask.getAlarmTime();
		PendingIntent contentIntent;
		if (taskTime != null) { // If a timed task, dont show the map
			contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ToDoMeActivity.class).putExtra("displayMap", false), 0);
		} else {
			contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ToDoMeActivity.class).putExtra("displayMap", true), 0);
		}
		// Set the info for the views that show in the notification panel.
		String message = "";

		if (poi != null) {
			message = "You are near a ";

			HashSet<String> taskTypes = notifyTask.getTypes();
			HashSet<String> locationTypes = poi.getLocationTypes();
			HashSet<String> typesIntersection = new HashSet<String>();

			typesIntersection.addAll(taskTypes);
			typesIntersection.retainAll(locationTypes);

			for (Iterator<String> iter = typesIntersection.iterator(); iter.hasNext();) {
				message = message + keywords.getDescriptionForType(iter.next()) + " ";
			}
		} else {
			message = notifyTask.getNotes();
		}
		notification.setLatestEventInfo(this, notifyTask.getName(), message, contentIntent);

		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to cancel.
		nm.notify(R.string.service_started, notification);
	}

	public static boolean isRunning() {
		return isRunning;
	}

	private void getKeywordDatabaseFromServer() {
		Log.i(TAG, "Getting KeywordDatabase from http://ec2-176-34-195-131.eu-west-1.compute.amazonaws.com/location_types.json");
		String file = Util.getFileFromServer("http://ec2-176-34-195-131.eu-west-1.compute.amazonaws.com/location_types.json");

		KeywordDatabase db = new KeywordDatabase();

		try {
			JSONArray jsonArray = new JSONArray(file);

			Log.i(TAG, "Number of entries " + jsonArray.length());

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				try {
					String type = jsonObject.getString("name");
					if (!KeywordDatabase.blacklistedTypes.contains(type)) {
						String descr = jsonObject.getString("description");
						// Log.v(TAG, type);
						JSONArray tags = jsonObject.getJSONArray("tags");
						for (int j = 0; j < tags.length(); j++) {
							String name = tags.getJSONObject(j).getString("name");
							db.add(name, type, descr);
						}
					}

				} catch (JSONException e) {
					Log.e(TAG, e.getMessage() + " for " + i + "/" + jsonArray.length(), e);
				}
			}
		} catch (JSONException ex) {
			Log.e(TAG, "", ex);
		}

		if (db != null) {
			// Save the date
			Time now = new Time();
			now.setToNow();
			data.edit().putLong("keywordsDate", now.toMillis(false)).commit();

			keywords = db;
			writeKeywords(db);
		} else {
			Log.e(TAG, "Got null keywords database from server");
			writeKeywords(new KeywordDatabase());
		}
	}

	/**
	 * Fetches locations about the given arguments. Returns null if a error occurs.
	 */
	private LocationDatabase getLocationDatabase(GeoPoint point, int radius, String type) {
		Log.i(TAG, "Beginning to get data from server, for " + Util.E6IntToDouble(point.getLatitudeE6()) + " " + Util.E6IntToDouble(point.getLongitudeE6()));

		double lat = Util.E6IntToDouble(point.getLatitudeE6());
		double lng = Util.E6IntToDouble(point.getLongitudeE6());

		String request = "http://ec2-176-34-195-131.eu-west-1.compute.amazonaws.com/locations.json?lat=" + lat + "&long=" + lng + "&radius=" + radius
				+ "&type=" + type;
		String file = Util.getFileFromServer(request);
		if (file.length() == 2) {
			Log.e(TAG, "File size for request " + request + " returned " + file);
			return null;
		}

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
					if (Util.intersection(KeywordDatabase.blacklistedTypes, types).size() == 0) {
						Log.v(TAG, type);
						newLocDatabase.add(new PointOfInterest((int) (jsonObject.getDouble("lat") * 1e6), (int) (jsonObject.getDouble("long") * 1e6), types,
								null, null, 10));
					}
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage() + " for " + i + "/" + jsonArray.length(), e);
				}
			}
		} catch (JSONException e1) {
			Log.e(TAG, "Error", e1);
			return null;
		}

		Log.i(TAG, "Updated locations database \n" + newLocDatabase.print());

		return newLocDatabase;

	}

	/**
	 * This updates the central database with the relevant data from the server
	 */
	private boolean updateDatabase(HashSet<String> taskTypes) {
		LocationDatabase newDatabase = new LocationDatabase();

		for (Iterator<String> iter = taskTypes.iterator(); iter.hasNext();) {
			String type = iter.next();
			Log.i(TAG, "Getting POIs for " + type);

			LocationDatabase tempDatabase = null;
			if (userCurrentLocation != null) {
				tempDatabase = getLocationDatabase(Util.locationToGeoPoint(userCurrentLocation), 500, type);
			} else {
				Log.i(TAG, "No user location fetch userCurrentLocation==null");
			}
			if (tempDatabase != null) {
				newDatabase.addAll(tempDatabase);
			}
		}

		if (newDatabase.size() > 0) {

			pointsOfInterest = newDatabase; // TODO At the moment new location data replaces old data, this needs to be fixed
			Log.i(TAG, "Location database replaced with new data");

			sendDatabaseToUI(pointsOfInterest);
			return true;
		} else {
			Log.w(TAG, "updateDatabase got a empty database");
			return false;
		}
	}

	void checkForReleventNotifications() {
		Log.i(TAG, "Checking for relevent notifications");
		if (!updateDatabase(getAllTaskTypes())) {
			Log.w(TAG, "checkForReleventNotifications errored, falling back to old database");
			if (pointsOfInterest == null) {
				Log.w(TAG, "pointsOfInterest ==null aswell, giving up");
			}
		}

		if (userCurrentLocation != null && pointsOfInterest != null) {
			LocationDatabase locDb = pointsOfInterest.findPointsWithinRadius(Util.locationToGeoPoint(userCurrentLocation), 0.1d);
			// locDb.removeDuplicatesOfTypeByDistance(Util.locationToGeoPoint(userCurrentLocation), getAllTaskTypes());

			for (Iterator<PointOfInterest> iter = locDb.iterator(); iter.hasNext();) {
				PointOfInterest poi = iter.next();

				if (!notifiedPOIs.contains(poi)) {

					ArrayList<Task> releventTasks = Util.getReleventTasks(tasks, poi);
					// Log.i(TAG, "Distance from " + poi.toString() + " is " + releventTasks.size() + " relevent tasks.");

					boolean mutipleNotifications = false;

					if (mutipleNotifications) {
						for (Iterator<Task> taskIter = releventTasks.iterator(); taskIter.hasNext();) {
							showNotification(taskIter.next(), poi);
							notifiedPOIs.add(poi);
						}
					} else {

						if (releventTasks.size() > 0) {
							showNotification(releventTasks, poi);
							notifiedPOIs.add(poi);
						} else {
							Log.e(TAG, "getReleventTasks has returned 0, this should not happen!");
						}
					}

				} else {
					Log.i(TAG, "Not notifying for " + poi.getLatitudeE6() + " " + poi.getLongitudeE6()
							+ " as it has been notified for in this location already");
				}
			}
		} else {
			Log.w(TAG, "Cant check for location triggered notifications as userCurrentLocation==null");
		}

		Log.i(TAG, "Begining to check timed tasks");
		for (Iterator<Task> tasksIter = tasks.iterator(); tasksIter.hasNext();) {
			Task task = tasksIter.next();
			Time taskTime = task.getAlarmTime();
			if (taskTime != null) {
				Time currentTime = new Time();
				currentTime.set(System.currentTimeMillis());
				Log.i(TAG, "Task " + task.getName() + " has a alarm time of " + taskTime.hour + ":" + taskTime.minute + ":" + taskTime.second);
				Log.i(TAG, "Current time is " + currentTime.hour + ":" + currentTime.minute + ":" + currentTime.second);
				if ((System.currentTimeMillis() - taskTime.toMillis(false)) > -1000 && (System.currentTimeMillis() - taskTime.toMillis(false)) < 1000) {
					Log.i(TAG, "Putting up notification for timed task");
					showNotification(task, null);
				} else {
					if (taskTime.toMillis(false) - System.currentTimeMillis() > 0) {
						Log.i(TAG, "Regiestering call back for " + (taskTime.toMillis(false) - System.currentTimeMillis()) + " milliseconds");
						timedTasksHandler.removeCallbacks(mUpdateTimeTask);
						timedTasksHandler.postDelayed(mUpdateTimeTask, taskTime.toMillis(false) - System.currentTimeMillis());
					}
				}
			}
		}
	}

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			Log.i(TAG, "Got a call back, checkingForReleventNotifications");
			checkForReleventNotifications();
		}
	};

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

	/*
	 * Handler of incoming messages from clients.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				Log.i(TAG, "Client registered.");
				readTasks();
				readKeywords();
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			case MSG_TASKS_UPDATED:
				Log.i(TAG, "MSG_TASKS_UPDATED received.");
				readTasks();
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
			case MSG_MAP_MODE_ENABLE:
				mapMode = true;
				setLocationUpdateSettings();
				break;
			case MSG_MAP_MODE_DISABLE:
				mapMode = false;
				setLocationUpdateSettings();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	// Messaging

	private void sendQueryResponse() {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				Message msg = Message.obtain(null, MSG_QUERY_ENABLED);
				msg.arg1 = enabled ? 1 : 0;
				mClients.get(i).send(msg);

			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
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
				// The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
				mClients.remove(i);
			}
		}
	}

	private void sendMessageToUI(int msgType) {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				Message msg = Message.obtain(null, msgType);
				mClients.get(i).send(msg);

			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
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
