package com.timeplace;

public class Task {	
	private String name;
	private String type;
	
	private boolean complete; 
	private int rating;
	
	public Task(String name, int rating, boolean complete)
	{
		this.name = name;
		this.rating = rating;
		this.complete = complete;
	}
	
	public String toString()
	{
		return name;
	}
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	public boolean isComplete() { return complete; }
	public void setComplete(boolean complete) { this.complete = complete; }
	public int getRating() { return rating; }
	public void setRating(int rating) { this.rating = rating; }
}
