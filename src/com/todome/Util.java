package com.todome;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import android.location.Location;
import android.util.Base64;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class Util {

	static int doubleToIntE6(double dub) {
		return (int) (dub * 1e6);
	}

	static double E6IntToDouble(int integer) {
		return (double) (integer / 1e6);
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

	static String getTaskArrayString(ArrayList<Task> tasks) {
		return getStringFromObject(tasks);
	}

	static String getLocationDatabaseString(LocationDatabase ld) {
		return getStringFromObject(ld);
	}

	static String getKeywordDatabaseString(KeywordDatabase kd) {
		return getStringFromObject(kd);
	}

	static String getStringFromObject(Serializable obj) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
		} catch (IOException e) {
			Log.e("Util.getStringFromObject", "IOException: " + e.getMessage());
		}
		return null;
	}

	static LocationDatabase getLocationDatabaseFromString(String str) throws StreamCorruptedException, IOException, ClassNotFoundException {
		return (LocationDatabase) getObjectFromString(str);
	}

	static ArrayList<Task> getTaskListFromString(String str) throws StreamCorruptedException, IOException, ClassNotFoundException {
		return (ArrayList<Task>) getObjectFromString(str);
	}

	static KeywordDatabase getKeyboardDatabaseFromString(String str) throws StreamCorruptedException, IOException, ClassNotFoundException {
		return (KeywordDatabase) getObjectFromString(str);
	}

	static Object getObjectFromString(String str) throws StreamCorruptedException, IOException, ClassNotFoundException {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(Base64.decode(str, Base64.DEFAULT)));
		} catch (EOFException ex) {
			Log.i("Util.getObjectFromString", "EOFException");
		}
		return ois.readObject();
	}
}
