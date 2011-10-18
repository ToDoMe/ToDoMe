package com.timeplace;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TabHost;

public class TimePlaceActivity extends TabActivity {

	// Data
	public static LocationDatabase db = new LocationDatabase();
	public static KeywordDatabase keywords = new KeywordDatabase();
	public static ArrayList<Task> tasks = new ArrayList<Task>();;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.main);

			Resources res = getResources();	// Resource object to get Drawables
			TabHost tabHost = getTabHost();	// The activity TabHost
			TabHost.TabSpec spec;			// Reusable TabSpec for each tab
			Intent intent;					// Reusable Intent for each tab

			// Create an Intent to launch an Activity for the tab (to be reused)
			intent = new Intent().setClass(this, TodoActivity.class);

			// Initialise a TabSpec for each tab and add it to the TabHost
			spec = tabHost.newTabSpec("todo").setIndicator("To-Do", res.getDrawable(R.drawable.ic_tab_todo)).setContent(intent);
			tabHost.addTab(spec);

			// Do the same for the other tabs
			intent = new Intent().setClass(this, MapViewActivity.class);
			spec = tabHost.newTabSpec("map").setIndicator("Map", res.getDrawable(R.drawable.ic_tab_map)).setContent(intent);
			tabHost.addTab(spec);
			
			intent = new Intent().setClass(this, TestTabActivity.class);
			spec = tabHost.newTabSpec("map").setIndicator("Test", res.getDrawable(R.drawable.ic_tab_map)).setContent(intent);
			tabHost.addTab(spec);

			tabHost.setCurrentTab(0);

			// Get database
			File file = new File("/data/spb.tsv");
			if (file.exists()) {
				PostcodeParser parser = new PostcodeParser(file, db);
				parser.parse();
			} else {
				message("Error", "TSV file not found. Please place the file at /data/spb.tsv");
			}

			// Add a "New task" task if one does not already exist
			boolean found = false;
			for (int i = 0; i < tasks.size(); i++) {
				if (tasks.get(i).getName() == "New task") {
					found = true;
				}
			}
			if (!found) {
				tasks.add(new Task("New task", "", "", 0));
			}
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