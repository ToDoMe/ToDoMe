package com.timeplace;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TabHost;

public class TimePlaceActivity extends TabActivity {
	private final String TAG = "TimePlaceActivity";
	
	// Data
	public static LocationDatabase db = new LocationDatabase();
	public static KeywordDatabase keywords = new KeywordDatabase();
	public static ArrayList<Task> tasks = new ArrayList<Task>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Begining ToDoMe, start of onCreate");
		try {
			setContentView(R.layout.main);

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
			CheckIfServiceIsRunning();
			sendTasksToService();

		} catch (Exception ex) {
			message("TimePlaceActivity.onCreate: " + ex.getClass().toString(), ex.getMessage());
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
				Log.i(TAG, "Message received: " + msg.getData().getString("str1"));
				// Deserialise the string into a LocationDatabase
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	private void CheckIfServiceIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        if (ToDoMeService.isRunning()) {
            doBindService();
        } else {
        	startService(new Intent(this, ToDoMeService.class));
        	doBindService();
        }
    }
	
	public void sendTasksToService() {
		sendMessageToService(Util.getTaskArrayString(tasks));
	}

    private void sendMessageToService(String value) {
        if (mIsBound) {
			if (mService != null) {
                try {
                    // Send data as a String
                    Bundle b = new Bundle();
                    b.putString("str1", value);
                    Message msg = Message.obtain(null, ToDoMeService.MSG_TASKS_UPDATED);
                    msg.setData(b);
                    mService.send(msg);
                    
                    ArrayList<Task> reTasks = Util.getTaskListFromString(msg.getData().getString("str1"));
                    Log.i(TAG, "Sending. reTasks.size() = " + reTasks.size());
                } catch (Exception ex) {
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
            Log.e("TestTabActivity", "Failed to unbind from the service", t);
        }
    }
}