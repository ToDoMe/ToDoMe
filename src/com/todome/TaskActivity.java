package com.todome;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class TaskActivity extends Activity {
	private ToDoMeActivity parent;

	private ArrayList<Task> tasks; // Loaded from TimePlaceActivity for convenience
	private Task touchedTask;
	private ListView lv;
	private ArrayAdapter<Task> taskAdapter;
	private Dialog dialog;
	private AlertDialog alertMarkComplete;
	private AlertDialog alertDelete;
	public String taskType;

	private ArrayList<Task> tasksWithNewTask;

	private static final String TAG = "TaskActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.todo);
		parent = (ToDoMeActivity) getParent();

		tasks = ToDoMeActivity.tasks;
		tasksWithNewTask = new ArrayList<Task>();

		// Build popups
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setMessage("Mark complete?").setCancelable(false)
		.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						touchedTask.setName("[Completed] " + touchedTask.getName());
						tasks.add(touchedTask);
						tasks.remove(touchedTask);
						touchedTask.setComplete(true);
						setUpTasksWithNewTasks();
						taskAdapter.notifyDataSetChanged();
						parent.sendTasksToService();
						ToDoMeActivity.getInstance().saveTasks();
					}
				}).setNegativeButton("No",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		alertMarkComplete = builder.create();
		
		builder.setMessage("Delete?").setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								tasks.remove(touchedTask);
								setUpTasksWithNewTasks();
								taskAdapter.notifyDataSetChanged();
								parent.sendTasksToService();
								ToDoMeActivity.getInstance().saveTasks();
							}
						}).setNegativeButton("No",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		alertDelete = builder.create();
		
		setUpListView();
	}

	private void setUpListView() {
		lv = (ListView) findViewById(R.id.taskList);
		lv.setTextFilterEnabled(true);

		// Creating list task array
		setUpTasksWithNewTasks();
		Log.i("ToDoMe", "Tasks has " + tasks.size() + " list tasks has " + tasksWithNewTask.size());

		taskAdapter = new ArrayAdapter<Task>(this, R.layout.list_item, tasksWithNewTask);
		lv.setAdapter(taskAdapter);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == 0 || position == (tasks.size() + 1)) { // If
																		// clicking
																		// on a
																		// New
																		// Task
																		// item
					showTaskDialog(tasks.size() + 1, false);
				} else {
					touchedTask = tasks.get(position - 1);
					if (touchedTask.getName().contains("[Completed] ")) {
						alertDelete.show();
					} else {
						alertMarkComplete.show();
					}
				}
			}
		});

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == 0 || position == (tasks.size() + 1)) { // If
																		// clicking
																		// on a
																		// New
																		// Task
																		// item
					return false;
				} else {
					showTaskDialog(position - 1, true);
					return true;
				}
			}
		});
	}

	private void showTaskDialog(final int position, final boolean updatingTask) {
		dialog = new Dialog(this, R.layout.new_task_dialog);

		dialog.setContentView(R.layout.new_task_dialog);
		dialog.setTitle("New Task");

		EditText taskNameEntry = (EditText) dialog.findViewById(R.id.taskNameEntry);

		if (updatingTask) {
			RatingBar ratingEntry = (RatingBar) dialog.findViewById(R.id.ratingEntry);
			EditText notesEntry = (EditText) dialog.findViewById(R.id.notesEntry);
			EditText postcodeEntry = (EditText) dialog.findViewById(R.id.postcodeEntry);

			Task thisTask = tasks.get(position);

			taskNameEntry.setText(thisTask.getName());
			ratingEntry.setRating(thisTask.getRating());
			notesEntry.setText(thisTask.getNotes());
			postcodeEntry.setText(thisTask.getPostcode());
		}

		dialog.show();

		final Button okButton = (Button) dialog.findViewById(R.id.okButton);
		okButton.setEnabled(updatingTask);

		// if (tasks.size() != 0 && tasks.size() != (position - 1)) { // Check
		// if a task is being clicked on
		// Task task = tasks.get(position);

		// taskNameEntry.setText(task.getName());
		// }

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
				hideTaskDialog(position, updatingTask);
			}
		});
	}

	private void hideTaskDialog(final int position, final boolean updatingTask) {
		EditText taskNameEntry = (EditText) dialog.findViewById(R.id.taskNameEntry);
		RatingBar ratingEntry = (RatingBar) dialog.findViewById(R.id.ratingEntry);
		EditText notesEntry = (EditText) dialog.findViewById(R.id.notesEntry);
		EditText postcodeEntry = (EditText) dialog.findViewById(R.id.postcodeEntry);

		// Create the new task
		Task task = new Task(taskNameEntry.getText().toString(), notesEntry.getText().toString(), postcodeEntry.getText().toString(), (int) ratingEntry
				.getRating());

		// Give it a type
		ArrayList<String> type = ToDoMeActivity.keywords.getTypes(task.getName());
		task.setTypes(type);

		if (updatingTask) {
			tasks.remove(position);
		}

		// Put it in the array, in the right place
		if (position == tasks.size() + 1) {
			tasks.add(tasks.size(), task);
		} else {
			tasks.add(position, task);
		}

		// Regenerate the list task array
		setUpTasksWithNewTasks();

		// Notify the taskAdaptor of the change
		taskAdapter.notifyDataSetChanged();

		Log.i(TAG, "Task just added (" + task.getName() + " " + type + ") now have " + tasks.size() + " tasks");
		Log.i(TAG, "Tasks: " + tasks.toString());

		// message("", type);
		Log.d(TAG, "Its here!");

		/*
		 * ArrayList<String> typez = new ArrayList<String>();
		 * 
		 * Time LOL = new Time(); LOL.set(1319155174000l); Time[] time = {LOL,
		 * LOL, LOL, LOL, LOL, LOL, LOL}; typez.add("postbox");
		 * 
		 * PointOfInterest poi = new PointOfInterest(4143206, -8038992, typez,
		 * null, time, 1.0);
		 * 
		 * try { message("",
		 * Long.toString(LocationDatabase.calculateTimeDeltaInMilliseconds(new
		 * GeoPoint(4043206,-8038992), poi))); } catch (Throwable e) {
		 * e.printStackTrace(); }
		 */

		taskNameEntry.setText("");
		parent.sendTasksToService();
		ToDoMeActivity.getInstance().saveTasks();

		dialog.hide();
	}

	private void setUpTasksWithNewTasks() {
		tasksWithNewTask.clear();
		tasksWithNewTask.addAll(tasks);
		tasksWithNewTask.add(0, new Task("New Task", "", "", 0));
		if (tasks.size() != 0)
			tasksWithNewTask.add(new Task("New Task", "", "", 0));

	}

	private void message(String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.show();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.about_menu_button:
			Log.i(TAG, "About menu");
			return true;
		case R.id.preferences_menu_button:
			Log.i(TAG, "Preferences menu");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}