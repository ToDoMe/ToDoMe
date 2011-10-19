package com.timeplace;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ToDoMeDatabaseAdapter {

	// Database fields
	public static final String TASKS_KEY_ROWID = "_id";
	public static final String TASKS_KEY_NAME = "name";
	public static final String TASKS_KEY_NOTES = "notes";
	public static final String TASKS_KEY_POSTCODE = "postcode";
	public static final String TASKS_KEY_RATING = "rating";
	public static final String TASKS_KEY_TYPE = "type";
	private static final String TASKS_TABLE = "tasks";

	private Context context;
	private SQLiteDatabase database;
	private ToDoMeDatabaseHelper dbHelper;

	public ToDoMeDatabaseAdapter(Context context) {
		this.context = context;
	}

	public ToDoMeDatabaseAdapter open() throws SQLException {
		dbHelper = new ToDoMeDatabaseHelper(context);
		database = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	public long createTask(String name, String notes, String postcode, int rating) {
		ContentValues initialValues = createTasksContentValues(name, notes, postcode, rating);

		return database.insert(TASKS_TABLE, null, initialValues);
	}

	public boolean updateTodo(long rowId, String name, String notes, String postcode, int rating) {
		ContentValues updateValues = createTasksContentValues(name, notes, postcode, rating);

		return database.update(TASKS_TABLE, updateValues, TASKS_KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean deleteTodo(long rowId) {
		return database.delete(TASKS_TABLE, TASKS_KEY_ROWID + "=" + rowId, null) > 0;
	}

	public Cursor fetchAllTasks() {
		return database.query(TASKS_TABLE, new String[] { TASKS_KEY_ROWID, TASKS_KEY_NAME, TASKS_KEY_NOTES, TASKS_KEY_POSTCODE, TASKS_KEY_RATING,
				TASKS_KEY_TYPE }, null, null, null, null, null);
	}

	public Cursor fetchTodo(long rowId) throws SQLException {
		Cursor mCursor = database.query(true, TASKS_TABLE, new String[] { TASKS_KEY_ROWID, TASKS_KEY_NAME, TASKS_KEY_NOTES, TASKS_KEY_POSTCODE,
				TASKS_KEY_RATING, TASKS_KEY_TYPE }, TASKS_KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	private ContentValues createTasksContentValues(String name, String notes, String postcode, int rating) {
		ContentValues values = new ContentValues();
		values.put(TASKS_KEY_NAME, name);
		values.put(TASKS_KEY_NOTES, notes);
		values.put(TASKS_KEY_POSTCODE, postcode);
		values.put(TASKS_KEY_RATING, rating);
		return values;
	}
}
