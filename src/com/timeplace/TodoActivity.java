package com.timeplace;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class TodoActivity extends Activity {
	private ArrayList<Task> tasks;
	private Task touchedTask;
	private ListView lv;
	private ArrayAdapter<Task> taskAdapter;
	private Dialog dialog;
	private AlertDialog alert;
	public String taskType;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.todo);
		
		tasks = TimePlaceActivity.tasks;
		
		// Build popup
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Mark complete?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                tasks.remove(touchedTask);
		                taskAdapter.notifyDataSetChanged();
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		alert = builder.create();
		setUpListView();
	}

	private void setUpListView() {
		lv = (ListView) findViewById(R.id.taskList);
		lv.setTextFilterEnabled(true);
		taskAdapter = new ArrayAdapter<Task>(this, R.layout.list_item, tasks);
		lv.setAdapter(taskAdapter);
	
		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	if (((TextView) view).getText() == "New task")
		    	{
		    		showTaskDialog();
		    	}
				else
				{
					touchedTask = tasks.get(position);
					alert.show();
				}
		    }
		});
	}
	
	private void showTaskDialog() {
		try
		{
			dialog = new Dialog(this, R.layout.new_task_dialog);

			dialog.setContentView(R.layout.new_task_dialog);
			dialog.setTitle("New Task");
			dialog.show();
			
			Button okButton = (Button) dialog.findViewById(R.id.okButton);
			okButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v)
	        	{
	        		hideTaskDialog();
	        	}
			});
		}
		catch (Exception ex)
		{
			message(ex.getClass().toString(), ex.getMessage());
		}
	}
	
	private void hideTaskDialog()
	{
		EditText	taskNameEntry	= (EditText) dialog.findViewById(R.id.taskNameEntry);
		RatingBar	ratingEntry		= (RatingBar) dialog.findViewById(R.id.ratingEntry);
		EditText	notesEntry		= (EditText) dialog.findViewById(R.id.notesEntry);
		EditText	postcodeEntry	= (EditText) dialog.findViewById(R.id.postcodeEntry);
		
		Task task = new Task(taskNameEntry.getText().toString(),
							notesEntry.getText().toString(),
							postcodeEntry.getText().toString(),
							(int)ratingEntry.getRating());
		
		tasks.add(tasks.size() - 1, task);
		dialog.hide();
		taskAdapter.notifyDataSetChanged();
		task.setType(TimePlaceActivity.keywords.getType(task.getName()));
	}
	
	private void message(String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.show();
	}
}