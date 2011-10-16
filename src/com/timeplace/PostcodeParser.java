package com.timeplace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PostcodeParser {
	private File postcodeFile;
	private LocationDatabase database; 
	
	public PostcodeParser(File file, LocationDatabase database) {
		this.postcodeFile = file;
		this.database = database;
	}
	
	public void parse() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(postcodeFile)));
			
			String line;
			while ((line = reader.readLine()) != null) {
				String[] lineItems = line.split("\t");
				
				database.add(new PointOfInterest(lineItems[0], (int)(Double.valueOf(lineItems[1]) * (10e5)), (int)(Double.valueOf(lineItems[2]) * (10e5)), null, null, "postbox", "postbox"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
