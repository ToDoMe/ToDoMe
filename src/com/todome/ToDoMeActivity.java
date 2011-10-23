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

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;

public class ToDoMeActivity extends TabActivity {
	private static final String TAG = "ToDoMeActivity";

	/**
	 * Used to store preferences for the app
	 */
	static SharedPreferences prefs;

	/**
	 * Used to store data
	 * 
	 * "tasks" for the tasks array "keywords" for the keywords database
	 */
	static SharedPreferences data;

	private static ToDoMeActivity instance;

	public static ToDoMeActivity getInstance() {
		return instance;
	}

	public static final long LOC_INTERVAL = 20000;

	// Create preferences setting method, and overload with defaults.
	public static void setPreferences(Float search_radius, Long extra_time, Long gps_timeout) {
		prefs.edit().putFloat("search_radius", search_radius > 10 ? 10 : search_radius).putLong("extra_time", extra_time).putLong("gps_timeout", gps_timeout)
				.commit();
	}

	public static void setDefaultPreferences() {
		prefs.edit().putFloat("search_radius", 10)
					.putLong("extra_time", 5 * 60 * 1000)
					.putLong("gps_timeout", ToDoMeActivity.LOC_INTERVAL)
					.putBoolean("firstStart", true)
					.commit();
	}

	// Data
	public static LocationDatabase db = new LocationDatabase();
	public static KeywordDatabase keywords = new KeywordDatabase();
	public static ArrayList<Task> tasks = new ArrayList<Task>();

	private boolean notificationsEnabled = true;

	// About dialog
	private AlertDialog aboutDialog;

	private void readTasks() {
		try {
			String str = data.getString("tasks", null);
			if (str != null) {
				tasks = Util.getTaskListFromString(str);
			} else {
				Log.i(TAG, "Loaded tasks, but got null, populating database with empty task list");
				writeTasks(new ArrayList<Task>());
			}
			Log.i(TAG, "tasks.size() = " + tasks.size());
		} catch (Exception ex) {
			Log.e(TAG, "", ex);
		}

	}

	static void writeTasks(ArrayList<Task> newTasks) {
		if (newTasks.size() != tasks.size()) {
			Log.e(TAG, "Oh dear, this shouldn't happen, tasks and newTasks are of a different size in ToDoMeActivity.writeTasks();");
		}
		Editor dataEditor = data.edit();

		dataEditor.putString("tasks", Util.getTaskArrayString(newTasks));
		if (dataEditor.commit()) {
			Log.i(TAG, "Sucessfuly commited " + newTasks.size());
		} else {
			Log.e(TAG, "ERROR commiting tasks, in ToDoMeActivity.writeTasks(); " + newTasks.size());
		}
		
		if (data.getString("tasks", null) != Util.getTaskArrayString(newTasks)) {
			Log.e(TAG, "Just put tasks in, but tasks does not come out?!?!");
		}

		Log.i(TAG, "Now have " + newTasks.size() + " tasks, sending to service " + Util.getTaskArrayString(newTasks));
		notifyTasksChanged();
	}

	private void readKeywords() {
		try {
			String str = data.getString("keywords", null);
			if (str != null) {
				keywords = Util.getKeywordDatabaseFromString(str);
			}
			Log.i(TAG, "keywords.size() = " + keywords.size());
		} catch (Exception ex) {
			Log.e(TAG, "", ex);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Begining ToDoMe, start of onCreate");
		instance = this;
		try {
			setContentView(R.layout.main);

			// Load the two shared preferences files
			prefs = getSharedPreferences("prefs", MODE_PRIVATE);
			data = getSharedPreferences("data", MODE_PRIVATE);

			if (prefs.getBoolean("firstStart", true)) {
				final AlertDialog alertDialog = new AlertDialog.Builder(ToDoMeActivity.this).create();
				alertDialog.setTitle("Welcome to ToDoMe");
				alertDialog.setMessage("To start using ToDoMe, enter some tasks in the To-Do tab. " +
									   "ToDoMe picks up certain keywords in the task name to detect " +
									   "what types of locations will enable you to carry out your task. " +
									   "Please note that this software is beta, and there are lots of " +
									   "bugs and stability issues.");
				alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int which) {
				    	  alertDialog.dismiss();
				      } }); 
				alertDialog.show();
				prefs.edit().putBoolean("firstStart", false).commit();
			}
			

			// Service interaction
			checkIfServiceIsRunning();
			sendMessageToService(ToDoMeService.MSG_QUERY_ENABLED);
			notifyTasksChanged();

			// Fill the data structures
			readKeywords();
			readTasks();

			// setPreferences(); // Default prefs

			Resources res = getResources(); // Resource object to get Drawables
			TabHost tabHost = getTabHost(); // The activity TabHost
			TabHost.TabSpec spec; // Reusable TabSpec for each tab
			Intent intent; // Reusable Intent for each tab

			// Create an Intent to launch an Activity for the tab (to be reused)
			intent = new Intent().setClass(this, TaskActivity.class);

			// Initialise a TabSpec for each tab and add it to the TabHost
			spec = tabHost.newTabSpec("todo").setIndicator("To-Do", res.getDrawable(R.drawable.ic_tab_todo)).setContent(intent);
			tabHost.addTab(spec);

			// Do the same for the other tabs
			intent = new Intent().setClass(this, MapViewActivity.class);
			spec = tabHost.newTabSpec("map").setIndicator("Map", res.getDrawable(R.drawable.ic_tab_map)).setContent(intent);
			tabHost.addTab(spec);

			// intent = new Intent().setClass(this, TestTabActivity.class);
			// spec = tabHost.newTabSpec("map").setIndicator("Test", res.getDrawable(R.drawable.ic_tab_map)).setContent(intent);
			// tabHost.addTab(spec);

			if (getIntent().getBooleanExtra("displayMap", false)) {
				tabHost.setCurrentTab(1);
			} else {
				tabHost.setCurrentTab(0);
			}

		} catch (Exception ex) {
			Log.e(TAG, "In onCreate", ex);
		}
	}

	// Service interaction

	final Messenger mMessenger = new Messenger(new IncomingHandler());

	Messenger mService = null;
	boolean mIsBound;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			notifyTasksChanged();
			// textStatus.setText("Attached.");
			try {
				Message msg = Message.obtain(null, ToDoMeService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even do anything with it
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been unexpectedly disconnected - process crashed.
			mService = null;
			// textStatus.setText("Disconnected.");
		}
	};

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ToDoMeService.MSG_LOCATIONS_UPDATED:
				try {
					db = Util.getLocationDatabaseFromString(msg.getData().getString("str1"));
				} catch (Exception ex) {
					Log.e(TAG, ex.getClass().toString() + " " + ex.getMessage());
				}
				Log.i(TAG, "db.size() = " + db.size());
				if (MapViewActivity.getInstance() != null) {
					MapViewActivity.getInstance().notifyLocationsUpdated();
				}
				break;
			case ToDoMeService.MSG_KEYWORDS_UPDATED:
				Log.i(TAG, "MSG_KEYWORDS_UPDATED received.");
				readKeywords();
				break;
			case ToDoMeService.MSG_QUERY_ENABLED:
				notificationsEnabled = (msg.arg1 > 0);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private void checkIfServiceIsRunning() {
		// If the service is running when the activity starts, we want to automatically bind to it.
		if (ToDoMeService.isRunning()) {
			doBindService();
		} else {
			startService(new Intent(this, ToDoMeService.class));
			doBindService();
		}
	}

	void sendMessageToService(int msgType) {
		if (mIsBound) {
			if (mService != null) {
				try {
					// Send data as a String
					Message msg = Message.obtain(null, msgType);
					mService.send(msg);
				} catch (RemoteException ex) {
					Log.e(TAG, ex.getClass().toString() + " " + ex.getMessage());
				}
			}
		}
	}

	public static void notifyTasksChanged() {
		ToDoMeActivity.getInstance().sendMessageToService(ToDoMeService.MSG_TASKS_UPDATED);
	}

	void doBindService() {
		this.bindService(new Intent(this, ToDoMeService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
		// textStatus.setText("Binding.");
	}

	void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with it, then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null, ToDoMeService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service has crashed.
				}
			}
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
			// textStatus.setText("Unbinding.");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			doUnbindService();
		} catch (Throwable t) {
			Log.e(TAG, "Failed to unbind from the service", t);
		}
	}

	// Menu stuff below here

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.toggle_notifications_menu_button);

		if (notificationsEnabled) {
			item.setTitle(R.string.disable_notifications);
		} else {
			item.setTitle(R.string.enable_notifications);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		Intent myIntent = new Intent();

		switch (item.getItemId()) {
		case R.id.about_menu_button:
			myIntent.setClassName("com.todome", "com.todome.AboutActivity");
			startActivity(myIntent);
			return true;
		case R.id.preferences_menu_button:
			myIntent.setClassName("com.todome", "com.todome.PreferencesActivity");
			startActivity(myIntent);
			return true;
		case R.id.toggle_notifications_menu_button:
			if (notificationsEnabled) {
				sendMessageToService(ToDoMeService.MSG_DISABLE);
				notificationsEnabled = false;
				item.setTitle(R.string.disable_notifications);
			} else {
				sendMessageToService(ToDoMeService.MSG_ENABLE);
				notificationsEnabled = true;
				item.setTitle(R.string.enable_notifications);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void aboutDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("About").setCancelable(true).setMessage("ToDoMe v0.1\n" + "21/10/11");
		aboutDialog = builder.create();

		aboutDialog.show();

		Log.v(TAG, "yo");
	}
}