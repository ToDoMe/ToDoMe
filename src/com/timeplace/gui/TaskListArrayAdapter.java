package com.timeplace.gui;

import java.util.ArrayList;
import java.util.List;

import com.timeplace.Task;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TaskListArrayAdapter extends ArrayAdapter<Task> {

	private final Activity context;
	private final int resource;
	private int labelForNewButtons;

	public TaskListArrayAdapter(Activity context, int resource, int labelForNewButtons) {
		super(context, resource, null);
		this.context = context;
		this.resource = resource;
		this.labelForNewButtons = labelForNewButtons;
	}

	// static to save the reference to the outer class and to avoid access to
	// any members of the containing class
	static class ViewHolder {
		public ImageView imageView;
		public TextView textView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// ViewHolder will buffer the assess to the individual fields of the row
		// layout

		ViewHolder holder;
		// Recycle existing view if passed as parameter
		// This will save memory and time on Android
		// This only works if the base layout for all classes are the same
		View rowView = convertView;
		return rowView;
		/*if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(resource, null, true);
			holder = new ViewHolder();
			holder.textView = (TextView) rowView.findViewById(R.layout.list_item);
			holder.imageView = (ImageView) rowView.findViewById(R.layout.list_item);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}

		if (position == 0 || position == tasks.size()) {
			holder.imageView.setImageResource(labelForNewButtons);
		} else {
			// holder.imageView.setImageResource(R.drawable.ok);
		}

		return rowView;*/
	}
}
