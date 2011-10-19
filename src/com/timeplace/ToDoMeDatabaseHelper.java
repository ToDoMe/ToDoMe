package com.timeplace;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ToDoMeDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "ToDoMeData";

	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String TASKS_TABLE = "create table tasks (_id integer primary key autoincrement, name text not null, notes text not null, postcode text not null, type text not null, complete integer not null, rating integer not null);";
	private static final String KEYWORDS_TABLE= "create table keywords (_id integer primary key autoincrement, name text not null, notes text not null, postcode text not null, type text not null, complete integer not null, rating integer not null);";
	private static final String LOCATIONS_TABLE= "create table locations (_id integer primary key autoincrement, name text not null, notes text not null, postcode text not null, type text not null, complete integer not null, rating integer not null);";
	private static final String DATABASE_CREATE = TASKS_TABLE + " " + KEYWORDS_TABLE + " " + LOCATIONS_TABLE;

	public ToDoMeDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	// Method is called during an upgrade of the database, e.g. if you increase
	// the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log
				.w(ToDoMeDatabaseHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion
						+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS tasks keywords locations");
		onCreate(database);
	}
}
