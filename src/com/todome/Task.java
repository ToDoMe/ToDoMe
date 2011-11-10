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

import java.util.HashSet;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import android.text.format.Time;

@DatabaseTable(tableName = "tasks")
public class Task implements Comparable<Task> {

	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(canBeNull = false)
	private String name;
	@DatabaseField(canBeNull = true)
	private String notes;
	@DatabaseField(canBeNull = false)

	private TagList tags;

	@DatabaseField(canBeNull = false)
	private boolean complete;

	public Task() {

	}

	public Task(String name, String notes, String postcode, int rating) {
		this.name = name;
		this.notes = notes;
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

	public int compareTo(Task another) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Task clone() {
		// TODO
		return null;
	}

}
