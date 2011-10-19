package com.timeplace.gui;

import java.io.File;
import java.util.ArrayList;

import com.timeplace.R;
import com.timeplace.ToDoMeDatabaseAdapter;
import com.timeplace.ToDoMeService;
import com.timeplace.R.drawable;
import com.timeplace.R.layout;
import com.timeplace.google.MapViewActivity;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TabHost;

public class ToDoMeActivity extends TabActivity {

	private ToDoMeDatabaseAdapter dbHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("ToDoMe::TimePlaceActivity", "Begining ToDoMe, start of onCreate.");
		try {
			setContentView(R.layout.main);

			Resources res = getResources(); // Resource object to get Drawables
			TabHost tabHost = getTabHost(); // The activity TabHost
			TabHost.TabSpec spec; // Reusable TabSpec for each tab
			Intent intent; // Reusable Intent for each tab

			// Create an Intent to launch an Activity for the tab (to be reused)
			intent = new Intent().setClass(this, TaskViewActivity.class);

			// Initialise a TabSpec for each tab and add it to the TabHost
			spec = tabHost.newTabSpec("todo").setIndicator("To-Do", res.getDrawable(R.drawable.ic_tab_todo)).setContent(intent);
			tabHost.addTab(spec);

			// Do the same for the other tabs
			// intent = new Intent().setClass(this, MapViewActivity.class);
			// spec = tabHost.newTabSpec("map").setIndicator("Map",
			// res.getDrawable(R.drawable.ic_tab_map)).setContent(intent);
			// tabHost.addTab(spec);

			tabHost.setCurrentTab(0);

			dbHelper = new ToDoMeDatabaseAdapter(this);
			dbHelper.open();

			dbHelper.createTask("Task 1", "Notes 1", "Postcode 1", 2);
			dbHelper.createTask("Task 2", "Notes 2", "Postcode 2", 4);

			// start the service
			Log.d("ToDoMe-TimePlaceActivity", "Starting service");
			bindService(new Intent(this, ToDoMeService.class), null, 0);

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
}