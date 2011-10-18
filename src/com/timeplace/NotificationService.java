package com.timeplace;

import java.util.Timer;
import java.util.TimerTask;

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

	private Timer timer;
	private NotificationManager nm;
	private int icon = R.drawable.icon;
	private Context context;
	private Intent intent;
	private Notification notification;
	private PendingIntent contentIntent;
	private int num = 0;
 
	private TimerTask updateTask = new TimerTask() {
		@Override
		public void run() {
			Log.i(TAG, "Timer task doing work");
			num++;
			notification.setLatestEventInfo(context, "Hello World! " + num, "Need to post something?", contentIntent);
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

		// Notification example from http://developer.android.com/guide/topics/ui/notifiers/notifications.html
		timer = new Timer("TimePlaceNotificationTimer");
		timer.schedule(updateTask, 1000L, 60 * 1000L);
		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notification = new Notification(icon, "Hello there!", System.currentTimeMillis());
		notification.defaults |= Notification.DEFAULT_SOUND;		// Adds sound
		//notification.defaults |= Notification.DEFAULT_VIBRATE;	// TODO stop this line from crashing the program
		intent = new Intent(this, TodoActivity.class);
		contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
		context = getApplicationContext();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Service destroying");

		timer.cancel();
		timer = null;
	}
}
