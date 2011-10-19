package com.timeplace.gui;

import java.util.ArrayList;

import com.timeplace.R;
import com.timeplace.ToDoMeDatabaseAdapter;
import com.timeplace.R.drawable;
import com.timeplace.R.id;
import com.timeplace.R.layout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class TaskViewActivity extends ListActivity {

	private ToDoMeDatabaseAdapter dbHelper;
	private Dialog dialog;
	private TaskListArrayAdapter taskAdapter;

	private static final String TAG = "TodoActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.todo);

		dbHelper = new ToDoMeDatabaseAdapter(this);
		dbHelper.open();

		Cursor cursor = dbHelper.fetchAllTasks();
		startManagingCursor(cursor);

		String[] from = new String[] { ToDoMeDatabaseAdapter.TASKS_KEY_NAME };
		int[] to = new int[] { R.drawable.androidmarker };

		SimpleCursorAdapter task = new SimpleCursorAdapter(this, R.layout.todo, cursor, from, to);

		setListAdapter(task);
	}

	private void showTaskDialog() {
		try {
			dialog = new Dialog(this, R.layout.new_task_dialog);

			dialog.setContentView(R.layout.new_task_dialog);
			dialog.setTitle("New Task");
			dialog.show();

			EditText taskNameEntry = (EditText) dialog.findViewById(R.id.taskNameEntry);
			final Button okButton = (Button) dialog.findViewById(R.id.okButton);
			okButton.setEnabled(false);

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
					hideTaskDialog();
				}
			});
		} catch (Exception ex) {
			Log.i(ex.getClass().toString(), ex.getMessage());
		}
	}

	private void hideTaskDialog() {
		EditText taskNameEntry = (EditText) dialog.findViewById(R.id.taskNameEntry);
		RatingBar ratingEntry = (RatingBar) dialog.findViewById(R.id.ratingEntry);
		EditText notesEntry = (EditText) dialog.findViewById(R.id.notesEntry);
		EditText postcodeEntry = (EditText) dialog.findViewById(R.id.postcodeEntry);

		dialog.hide();
		taskAdapter.notifyDataSetChanged();

	}

}