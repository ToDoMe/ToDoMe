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
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
	
	public static final String FILE_PATH = "todome.dat";
	private static SharedPreferences prefs;
	private static ToDoMeActivity instance;
	public static ToDoMeActivity getInstance() { return instance; }
	
	public static final long LOC_INTERVAL = 60000;

	// Create preferences setting method, and overload with defaults.
	public static void setPreferences(Float search_radius, Long extra_time, Long gps_timeout) {
		prefs.edit().putFloat("search_radius", search_radius > 10 ? 10 : search_radius)
					.putLong("extra_time", extra_time)
					.putLong("gps_timeout", gps_timeout)
					.commit();
	}
	
	private static void setPreferences() {
		prefs.edit().putFloat("search_radius", 10)
					.putLong("extra_time", 5 * 60 * 1000)
					.putLong("gps_timeout", ToDoMeActivity.LOC_INTERVAL)
					.commit();
	}
	
	public static SharedPreferences getPreferences() {
		return prefs;
	}
	
	// Data
	public static LocationDatabase db = new LocationDatabase();
	public static KeywordDatabase keywords = KeywordDatabase.fromServer();
	public static ArrayList<Task> tasks = new ArrayList<Task>();
	
	private boolean notificationsEnabled = true; 
	
	public void saveTasks() {
		try {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("tasks", Util.getStringFromObject(tasks));
			boolean successfull = editor.commit();
			Log.i(TAG, "Successful: " + successfull);
		} catch (Exception ex) {
			Log.e(TAG, "", ex);
		}
	}
	//About dialog
	private AlertDialog aboutDialog;
	
	public boolean getLite() {
		return false;
	}
	
	public void loadTasks() {
		Log.i(TAG, "Tasks loaded");
		try {
			String str = prefs.getString("tasks", "");
			if (str != "") {
				tasks = Util.getTaskListFromString(str);
			}
		} catch (Exception ex) {
			Log.e(TAG, "", ex);
		}
	}
	
	/*public boolean getLite() {
		return false;
	}*/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Begining ToDoMe, start of onCreate");
		instance = this;
		try {
			setContentView(R.layout.main);
			
			prefs = getSharedPreferences("Tasks", MODE_PRIVATE);
			loadTasks();
			
			setPreferences(); // Default prefs
			
			Resources res = getResources();	// Resource object to get Drawables
			TabHost tabHost = getTabHost();	// The activity TabHost
			TabHost.TabSpec spec;			// Reusable TabSpec for each tab
			Intent intent;					// Reusable Intent for each tab

			// Create an Intent to launch an Activity for the tab (to be reused)
			intent = new Intent().setClass(this, TaskActivity.class);

			// Initialise a TabSpec for each tab and add it to the TabHost
			spec = tabHost.newTabSpec("todo").setIndicator("To-Do", res.getDrawable(R.drawable.ic_tab_todo)).setContent(intent);
			tabHost.addTab(spec);

			// Do the same for the other tabs
			intent = new Intent().setClass(this, MapViewActivity.class);
			spec = tabHost.newTabSpec("map").setIndicator("Map", res.getDrawable(R.drawable.ic_tab_map)).setContent(intent);
			tabHost.addTab(spec);
			
			//intent = new Intent().setClass(this, TestTabActivity.class);
			//spec = tabHost.newTabSpec("map").setIndicator("Test", res.getDrawable(R.drawable.ic_tab_map)).setContent(intent);
			//tabHost.addTab(spec);

			tabHost.setCurrentTab(0);

			// Service interaction
			queryNotificationsEnabled();
			checkIfServiceIsRunning();
			notifyTasksChanged();

		} catch (Exception ex) {
			message("ToDoMeActivity.onCreate: " + ex.getClass().toString(), ex.getMessage());
		}
	}
	
	private void message(String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.show();
	}
	
	// Service interaction
	
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	
	Messenger mService = null;
	boolean mIsBound;
	
	private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            notifyTasksChanged();
            //textStatus.setText("Attached.");
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
			//textStatus.setText("Disconnected.");
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
			case ToDoMeService.MSG_QUERY_ENABLED:
				notificationsEnabled = (msg.arg1 > 0);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	private void checkIfServiceIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        if (ToDoMeService.isRunning()) {
            doBindService();
        } else {
        	startService(new Intent(this, ToDoMeService.class));
        	doBindService();
        }
    }
	
	private void queryNotificationsEnabled() {
        if (mIsBound) {
			if (mService != null) {
                try {
                    // Send data as a String
                    Message msg = Message.obtain(null, ToDoMeService.MSG_QUERY_ENABLED);
                    mService.send(msg);
                } catch (RemoteException ex) {
                	Log.e(TAG, ex.getClass().toString() + " " + ex.getMessage());
                }
            }
        }
	}
	
	public void notifyTasksChanged() {
        if (mIsBound) {
			if (mService != null) {
                try {
                    // Send data as a String
                    Message msg = Message.obtain(null, ToDoMeService.MSG_TASKS_UPDATED);
                    mService.send(msg);
                } catch (RemoteException ex) {
                	Log.e(TAG, ex.getClass().toString() + " " + ex.getMessage());
                }
            }
        }
	}

	void doBindService() {
		this.bindService(new Intent(this, ToDoMeService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
        //textStatus.setText("Binding.");
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
            //textStatus.setText("Unbinding.");
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
	        	// TODO toggle notifications button
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    
    public void aboutDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	builder.setTitle("About").setCancelable(true).setMessage("ToDoMe v0.1\n" +
    															 "21/10/11");
		aboutDialog = builder.create();
		
		aboutDialog.show();
		
		Log.v(TAG, "yo");
    }
}