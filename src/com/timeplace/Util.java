package com.timeplace;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import android.location.Location;

import com.google.android.maps.GeoPoint;

public class Util {

	static int doubleToIntE6(double dub) {
		return (int) (dub * 1e6);
	}

	static double E6IntToDouble(int integer) {
		return (int) (integer / 1e6);
	}

	static GeoPoint locationToGeoPoint(Location loc) {
		return new GeoPoint(doubleToIntE6(loc.getLatitude()), doubleToIntE6(loc.getLongitude()));
	}

	static Location geoPointToLocation(GeoPoint point) {
		Location loc = new Location("");
		loc.setLatitude(E6IntToDouble(point.getLatitudeE6()));
		loc.setLongitude(E6IntToDouble(point.getLongitudeE6()));
		return loc;
	}

	String getTaskArrayString(ArrayList<Task> tasks) {
		return getStringFromObject(tasks);
	}

	String getLocationDatabaseString(LocationDatabase ld) {
		return getStringFromObject(ld);
	}

	String getKeywordDatabaseString(KeywordDatabase kd) {
		return getStringFromObject(kd);
	}

	String getStringFromObject(Object obj) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			return baos.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	LocationDatabase getLocationDatabaseFromString(String str) {
		return (LocationDatabase) getObjectFromString(str);
	}

	ArrayList<Task> getTaskListFromString(String str) {
		return (ArrayList<Task>) getObjectFromString(str);
	}

	KeywordDatabase getKeyboardDatabaseFromString(String str) {
		return (KeywordDatabase) getObjectFromString(str);
	}

	Object getObjectFromString(String str) {
		ByteArrayInputStream baip = new ByteArrayInputStream(str.getBytes());

		try {
			ObjectInputStream ois = new ObjectInputStream(baip);
			return ois.readObject();
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
