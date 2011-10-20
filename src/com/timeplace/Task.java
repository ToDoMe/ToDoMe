package com.timeplace;

import java.io.Serializable;
import java.util.ArrayList;

public class Task implements Serializable {	
	private String name;
	private String notes;
	private String postcode;
	private ArrayList<String> types;
	
	private boolean complete; 
	private int rating;
	
	public Task(String name, String notes, String postcode, int rating)
	{
		this.name = name;
		this.notes = notes;
		this.postcode = postcode;
		this.rating = rating;
		this.complete = false;
	}
	
	public String toString()
	{
		return name;
	}
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getNotes() { return notes; }
	public void setNotes(String notes) { this.notes = notes; }
	public String getPostcode() { return postcode; }
	public void setPostcode(String postcode) { this.postcode = postcode; }
	public ArrayList<String> getTypes() { return types; }
	public void setTypes(ArrayList<String> types) { this.types = types; }
	public boolean isComplete() { return complete; }
	public void setComplete(boolean complete) { this.complete = complete; }
	public int getRating() { return rating; }
	public void setRating(int rating) { this.rating = rating; }

}
