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
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
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
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

public class TaskActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private Task touchedTask;
	private ListView lv;
	private ArrayAdapter<Task> taskAdapter;
	private Dialog dialog;
	private AlertDialog alertMarkComplete;
	private AlertDialog alertDelete;
	
	private Dao<Task, Integer> taskDao;

	// Task array + the "New Task" buttons
	private ArrayList<Task> tasksWithNewTask;

	private static final String TAG = "TaskActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.todo);
		tasksWithNewTask = new ArrayList<Task>();
		
		// Load dao
		taskDao = getHelper().getTaskDao();

		// Build popups
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(R.string.mark_complete).setCancelable(false)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// touchedTask.setName("[Completed] " +
								// touchedTask.getName());
								touchedTask.setComplete(true);
								taskDao.updateId(touchedTask, taskDao.)delete(touchedTask);
								ToDoMeActivity.tasks.add(touchedTask);
								setUpTasksWithNewTasks();
								taskAdapter.notifyDataSetChanged();
								ToDoMeActivity.writeTasks(ToDoMeActivity.tasks);
							}
						}).setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		alertMarkComplete = builder.create();

		builder.setMessage(R.string.delete_message).setCancelable(false)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								ToDoMeActivity.tasks.remove(touchedTask);
								setUpTasksWithNewTasks();
								taskAdapter.notifyDataSetChanged();
								ToDoMeActivity.writeTasks(ToDoMeActivity.tasks);
							}
						}).setNegativeButton(android.R.string.no,
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
		Log.i(TAG, "Displaying " + ToDoMeActivity.tasks.size() + " tasks");

		taskAdapter = new ArrayAdapter<Task>(this, R.layout.list_item,
				tasksWithNewTask);
		lv.setAdapter(taskAdapter);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0
						|| position == (ToDoMeActivity.tasks.size() + 1)) { // If
																			// clicking
																			// on
																			// a
																			// New
																			// Task
																			// item
					showTaskDialog(position, false);
				} else {
					touchedTask = ToDoMeActivity.tasks.get(position - 1);
					// Log.i(TAG, "Task " + touchedTask.getName() + " " +
					// (position));
					if (touchedTask.isComplete()) {
						// Log.i(TAG, "Deleting");
						alertDelete.show();
					} else {
						// Log.i(TAG, "Completing");
						alertMarkComplete.show();
					}
				}
			}
		});

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0
						|| position == (ToDoMeActivity.tasks.size() + 1)) { // If
																			// clicking
																			// on
																			// a
																			// NewTask
																			// item
					return false;
				} else {
					showTaskDialog(position, true);
					return true;
				}
			}
		});
	}

	private void showTaskDialog(final int position, final boolean updatingTask) {
		dialog = new Dialog(this, R.layout.new_task_dialog);

		dialog.setContentView(R.layout.new_task_dialog);
		dialog.setTitle("New Task");

		EditText taskNameEntry = (EditText) dialog
				.findViewById(R.id.taskNameEntry);
		RatingBar ratingEntry = (RatingBar) dialog
				.findViewById(R.id.ratingEntry);
		EditText notesEntry = (EditText) dialog.findViewById(R.id.notesEntry);

		// EditText postcodeEntry = (EditText)
		// dialog.findViewById(R.id.postcodeEntry);

		TimePicker timeEntry = (TimePicker) dialog.findViewById(R.id.timeEntry);

		if (updatingTask) {

			Task thisTask = ToDoMeActivity.tasks.get(position - 1);

			taskNameEntry.setText(thisTask.getName());
			ratingEntry.setRating(thisTask.getRating());
			notesEntry.setText(thisTask.getNotes());
			// postcodeEntry.setText(thisTask.getPostcode());
			if (thisTask.getAlarmTime() != null) {
				Log.i(TAG, "AlarmTime hour " + thisTask.getAlarmTime().hour
						+ " min " + thisTask.getAlarmTime().minute);
				timeEntry.setCurrentHour(thisTask.getAlarmTime().hour);
				timeEntry.setCurrentMinute(thisTask.getAlarmTime().minute);
			}
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

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				okButton.setEnabled(s.length() > 0);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
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
		EditText taskNameEntry = (EditText) dialog
				.findViewById(R.id.taskNameEntry);
		RatingBar ratingEntry = (RatingBar) dialog
				.findViewById(R.id.ratingEntry);
		EditText notesEntry = (EditText) dialog.findViewById(R.id.notesEntry);
		// EditText postcodeEntry = (EditText)
		// dialog.findViewById(R.id.postcodeEntry);
		TimePicker timeEntry = (TimePicker) dialog.findViewById(R.id.timeEntry);

		// Create the new task
		Task task = new Task(taskNameEntry.getText().toString(), notesEntry
				.getText().toString(),
				""/* postcodeEntry.getText().toString() */, (int) ratingEntry
						.getRating());

		Calendar c = Calendar.getInstance();
		Time currentTime = new Time();
		currentTime.set(System.currentTimeMillis());
		if ((currentTime.hour != timeEntry.getCurrentHour())
				|| (currentTime.minute != timeEntry.getCurrentMinute())) {
			Time time = new Time();
			time.set(0, timeEntry.getCurrentMinute(), timeEntry
					.getCurrentHour(), c.get(Calendar.DAY_OF_MONTH), c
					.get(Calendar.MONTH), c.get(Calendar.YEAR));
			task.setAlarmTime(time);
		}

		// Give it a type
		if (ToDoMeActivity.keywords.keywords.size() == 0) {
			Log.e(TAG, "Trying to asign keywords to " + task.getName()
					+ " but keywords database is empty");
		}
		HashSet<String> type = ToDoMeActivity.keywords.getTypes(task.getName());
		task.setTypes(type);

		if (updatingTask) {
			ToDoMeActivity.tasks.remove(position - 1);
		}

		// Put it in the array, in the right place
		if (position == 0) {
			ToDoMeActivity.tasks.add(0, task);
		} else {
			ToDoMeActivity.tasks.add(task);
		}

		// Regenerate the list task array
		setUpTasksWithNewTasks();

		// Notify the taskAdaptor of the change
		taskAdapter.notifyDataSetChanged();

		Log.i(TAG, "Task just added (" + task.getName() + " " + type
				+ ") now have " + ToDoMeActivity.tasks.size() + " tasks");
		Log.i(TAG, "Tasks: " + ToDoMeActivity.tasks.toString());

		taskNameEntry.setText("");
		ToDoMeActivity.writeTasks(ToDoMeActivity.tasks);
		dialog.dismiss();
	}

	private void setUpTasksWithNewTasks() {
		HashSet<Task> tasksToAdd = new HashSet<Task>();
		tasksWithNewTask.clear();
		tasksWithNewTask.addAll(ToDoMeActivity.tasks);
		for (Iterator<Task> iter = tasksWithNewTask.iterator(); iter.hasNext();) {
			Task task = iter.next();
			if (task.isComplete()) {
				iter.remove();
				Task newTask = task.clone(); // Clone to stop modifying the
												// existing task
				if (newTask == null) {
					Log.e(TAG, "newTask == null");
				}
				newTask.setName("[Completed] " + newTask.getName());
				tasksToAdd.add(newTask); // The add and remove, re-adds the task
											// at the bottom of the list
			}
		}
		tasksWithNewTask.addAll(tasksToAdd);
		tasksWithNewTask.add(0, new Task("New Task", "", "", 0));
		if (ToDoMeActivity.tasks.size() != 0)
			tasksWithNewTask.add(new Task("New Task", "", "", 0));

	}

	private void addStarRating(int position) {
		TextView tv = (TextView) lv.getChildAt(position);
		int rating = ToDoMeActivity.tasks.get(position - 1).getRating();
		Drawable img = null;

		switch (rating) {
		case 0:
			img = null;
			break;
		case 1:
			img = getBaseContext().getResources().getDrawable(
					R.drawable.staricon1);
			break;
		case 2:
			img = getBaseContext().getResources().getDrawable(
					R.drawable.staricon2);
			break;
		case 3:
			img = getBaseContext().getResources().getDrawable(
					R.drawable.staricon3);
			break;
		case 4:
			img = getBaseContext().getResources().getDrawable(
					R.drawable.staricon4);
			break;
		case 5:
			img = getBaseContext().getResources().getDrawable(
					R.drawable.staricon5);
			break;
		default:
			break;
		}

		tv.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);

	}
}
