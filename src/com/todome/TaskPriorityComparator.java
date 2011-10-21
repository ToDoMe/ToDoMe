package com.todome;

import java.util.Comparator;

public class TaskPriorityComparator implements Comparator<Task> {
	public int compare(Task task1, Task task2) {
		return task1.getRating() - task2.getRating();
	}
}
