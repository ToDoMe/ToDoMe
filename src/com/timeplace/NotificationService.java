package com.timeplace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

// Service example from http://mindtherobot.com/blog/37/android-architecture-tutorial-developing-an-app-with-a-background-service-using-ipc/
// TODO make subsequent notifications show up
public class NotificationService extends Service {

	private static final String TAG = NotificationService.class.getSimpleName();

	private boolean debug = true;

	private Timer timer;
	private NotificationManager nm;
	private int icon = R.drawable.icon;
	private Context context;
	private Intent intent;
	private Notification notification;
	private PendingIntent contentIntent;

	private TimerTask updateTask = new TimerTask() {
		@Override
		public void run() {
			Log.i(TAG, "Timer task doing work");
			try {
				String name = TimePlaceActivity.tasks.get(1).getName();
			} catch (Exception e) {
				System.out.println("It's all broked still :(");
			}
			notification.setLatestEventInfo(context, "ToDoMe Reminder",
					"Task name", contentIntent);
			nm.notify(1, notification);
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service creating");

		// Notification example from
		// http://developer.android.com/guide/topics/ui/notifiers/notifications.html
		timer = new Timer("TimePlaceNotificationTimer");
		timer.schedule(updateTask, 1000L, 60 * 1000L);
		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notification = new Notification(icon, "Hello there!", System
				.currentTimeMillis());
		notification.defaults |= Notification.DEFAULT_SOUND; // Adds sound
		notification.icon = R.drawable.notification_icon;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		intent = new Intent(this, TaskActivity.class);
		contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
		context = getApplicationContext();

		getDataAndUpdateDatabase(new GeoPoint((int) (50.896996f * 1e6),
				(int) (-1.40416f * 1e6)), 100, "bct");
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

	public void notificationPopup(Task task) {
		notification.setLatestEventInfo(context, "ToDoMe Reminder", task
				.getName(), contentIntent);
	}

	void getDataAndUpdateDatabase(GeoPoint point, int radius, String type) {

		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		double lat = point.getLatitudeE6() / 1e6;
		double lng = point.getLongitudeE6() / 1e6;
		HttpGet httpGet = new HttpGet(
				"http://ec2-176-34-195-131.eu-west-1.compute.amazonaws.com/locations.json?lat="
						+ lat + "&long=" + lng + "&radius=" + radius + "&type="
						+ type);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
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
			Log.i("", "Number of entries " + jsonArray.length());
			notification.setLatestEventInfo(context, "ToDoMe ", ""
					+ jsonArray.length(), contentIntent);
			nm.notify(1, notification);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Log.i("", jsonObject.getString("text"));
			}
			TimePlaceActivity.tasks.clear();

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);

				TimePlaceActivity.db.add(new PointOfInterest((int) (jsonObject
						.getDouble("lat") * 1e6), (int) (jsonObject
						.getDouble("long") * 1e6), null, null, null, 10));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
