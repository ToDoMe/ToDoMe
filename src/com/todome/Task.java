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

import java.io.Serializable;
import java.util.ArrayList;

import android.text.format.Time;

public class Task implements Serializable, Comparable<Task> {
	private String name;
	private String notes;
	private String postcode;
	private ArrayList<String> types;

	private int second;
	private int minute;
	private int hour;
	private int monthDay;
	private int month;
	private int year;

	private boolean complete;
	private int rating;

	public Task(String name, String notes, String postcode, int rating) {
		this.name = name;
		this.notes = notes;
		this.postcode = postcode;
		this.rating = rating;
		this.complete = false;
	}

	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public ArrayList<String> getTypes() { // Returns an empty array for completed tasks
		if (!complete) {
			return types;
		} else {
			return new ArrayList<String>();
		}
	}

	public void setTypes(ArrayList<String> types) {
		this.types = types;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public Time getAlarmTime() {
		Time time = new Time();
		time.set(second, minute, hour, monthDay, month, year);
		return time;
	}

	public void setAlarmTime(Time alarmTime) {
		second = alarmTime.second;
		minute = alarmTime.minute;
		hour = alarmTime.hour;
		monthDay = alarmTime.monthDay;
		month = alarmTime.month;
		year = alarmTime.year;
	}

	public int compareTo(Task another) {
		// TODO Auto-generated method stub
		return 0;
	}

}
