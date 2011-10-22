package com.todome;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PreferencesActivity extends Activity {

	public PreferencesActivity() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);
        
        // Set values
        EditText search_radius = (EditText) findViewById(R.id.search_radius);
    	EditText extra_time = (EditText) findViewById(R.id.extra_time);
    	EditText gps_timeout = (EditText) findViewById(R.id.gps_timeout);
        
    	SharedPreferences preferences = ToDoMeActivity.getPreferences();
    	
    	search_radius.setText(Float.toString(preferences.getFloat("search_radius", 10)));
    	extra_time.setText(Long.toString(preferences.getLong("extra_time", 10) / (60 * 1000)));
    	gps_timeout.setText(Long.toString(preferences.getLong("gps_timeout", 10) / (60 * 1000)));
    	
        final Button buttonSave = (Button) findViewById(R.id.buttonSave); // Save preferences
        buttonSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	EditText search_radius = (EditText) findViewById(R.id.search_radius);
            	EditText extra_time = (EditText) findViewById(R.id.extra_time);
            	EditText gps_timeout = (EditText) findViewById(R.id.gps_timeout);
            	
                ToDoMeActivity.setPreferences(Float.valueOf(search_radius.getText().toString()),
                							  Long.valueOf(extra_time.getText().toString()) * 60 * 1000,
                							  Long.valueOf(gps_timeout.getText().toString()) * 60 * 1000);
                finish();
            }
        });
        
        final Button buttonCancel = (Button) findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		finish();
        		EditText search_radius = (EditText) findViewById(R.id.search_radius);
        		Log.v("121212", search_radius.getText().toString());
        	}
        });

    }

}
