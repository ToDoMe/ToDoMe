package com.timeplace;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class TaskActivity extends Activity {
	private ArrayList<Task> tasks; // Loaded from TimePlaceActivity for
	// convenience
	private Task touchedTask;
	private ListView lv;
	private ArrayAdapter<Task> taskAdapter;
	private Dialog dialog;
	private AlertDialog alert;
	public String taskType;

	private ArrayList<Task> tasksWithNewTask;

	private static final String TAG = "TodoActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.todo);

		tasks = TimePlaceActivity.tasks;
		tasksWithNewTask = new ArrayList<Task>();

		// Build popup
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Mark complete?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				tasks.remove(touchedTask);
				tasksWithNewTask.clear();
				tasksWithNewTask.addAll(tasks);
				tasksWithNewTask.add(0, new Task("New Task", "", "", 0));
				tasksWithNewTask.add(new Task("New Task", "", "", 0));
				taskAdapter.notifyDataSetChanged();
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener() {
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

		// Creating list task array
		tasksWithNewTask.clear();
		tasksWithNewTask.addAll(tasks);
		tasksWithNewTask.add(0, new Task("New Task", "", "", 0));
		tasksWithNewTask.add(new Task("New Task", "", "", 0));
		Log.i("ToDoMe", "Tasks has " + tasks.size() + " list tasks has " + tasksWithNewTask.size());

		taskAdapter = new ArrayAdapter<Task>(this, R.layout.list_item, tasksWithNewTask);
		lv.setAdapter(taskAdapter);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == 0 || position == (tasks.size() + 1)) {
					showTaskDialog(position);
				} else {
					touchedTask = tasks.get(position - 1);
					alert.show();
				}
			}
		});

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				showTaskDialog(position);
				return true;
			}
		});
	}

	private void showTaskDialog(final int position) {
		try {
			dialog = new Dialog(this, R.layout.new_task_dialog);

			dialog.setContentView(R.layout.new_task_dialog);
			dialog.setTitle("New Task");
			dialog.show();

			EditText taskNameEntry = (EditText) dialog.findViewById(R.id.taskNameEntry);
			final Button okButton = (Button) dialog.findViewById(R.id.okButton);
			okButton.setEnabled(false);
			
			/*if (tasks.size() >= (position - 1)) {
				Task task = tasks.get(position - 1);
				
				taskNameEntry.setText(task.getName());
			}*/

			taskNameEntry.addTextChangedListener(new TextWatcher() {

				public void onTextChanged(CharSequence s, int start, int before, int count) {
					okButton.setEnabled(s.length() > 0);
				}

				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				public void afterTextChanged(Editable s) {
				}
			});

			okButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					hideTaskDialog(position);
				}
			});
		} catch (Exception ex) {
			message(ex.getClass().toString(), ex.getMessage());
		}
	}

	private void hideTaskDialog(final int position) {
		EditText taskNameEntry = (EditText) dialog.findViewById(R.id.taskNameEntry);
		RatingBar ratingEntry = (RatingBar) dialog.findViewById(R.id.ratingEntry);
		EditText notesEntry = (EditText) dialog.findViewById(R.id.notesEntry);
		EditText postcodeEntry = (EditText) dialog.findViewById(R.id.postcodeEntry);

		// Create the new task
		Task task = new Task(taskNameEntry.getText().toString(), notesEntry.getText().toString(), postcodeEntry.getText().toString(), (int) ratingEntry
				.getRating());

		// Give it a type
		ArrayList<String> type = TimePlaceActivity.keywords.getTypes(task.getName());
		task.setTypes(type);

		// Put it in the array, in the right place
		if (position == 0) {
			tasks.add(0, task);
		} else {
			tasks.add(task);
		}

		// Regenerate the list task array
		tasksWithNewTask.clear();
		tasksWithNewTask.addAll(tasks);
		tasksWithNewTask.add(0, new Task("New Task", "", "", 0));
		tasksWithNewTask.add(new Task("New Task", "", "", 0));

		// Notify the taskAdaptor of the change
		taskAdapter.notifyDataSetChanged();

		Log.i(TAG, "Task just added (" + task.getName() + " " + type + ") now have " + (tasks.size() - 1) + " tasks");
		Log.i(TAG, "Tasks: " + tasks.toString());

		dialog.hide();

	}

	private void message(String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.show();
	}
}