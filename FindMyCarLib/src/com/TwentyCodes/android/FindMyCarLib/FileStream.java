/**
 * @author Twenty Codes
 * @author WWPowers
 * @author ricky barrette
 */

package com.TwentyCodes.android.FindMyCarLib;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.util.Log;

public class FileStream {

	private Context mContext;

	/*
	 * Constructor
	 */
	public FileStream(Context context) {
		mContext = context;
	}
	
	/**
	 * returns a boolean that is saved in a file as 1 = true, 0 = false
	 * @param path to file
	 * @return boolean
	 * @author ricky barrette
	 */
	public boolean readBoolean(String path){
		if(readInteger(path) == 1){
			return true;
		}
		return false;
	}
	
	/**
	 * returns an int, or 0 if the file is not parse-able
	 * @param String path
	 * @return int
	 * @author WWPowers
	 */
	public int readInteger(String path) {
		int data = 0;
		try {
			data = Integer.parseInt(readString(path));
		} catch (NumberFormatException e) {
//			e.printStackTrace();
			Log.w(mContext.getClass().getName(),"File "+ path +" did not contain an int");
		}
		return data;
	}
	
	/**
	 * reads a string from a file
	 * @param String path
	 * @return String
	 * @author ricky barrette
	 */
	public String readString(String path) {
		String theLine = "";
		StringBuffer data = new StringBuffer();
		int index = 0;
		try {
			BufferedReader theReader = new BufferedReader(new InputStreamReader(mContext.openFileInput(path)));
			while ((theLine = theReader.readLine())!=null) {
				//if this is not the first line, then append a new line
				if (index > 0) {
					data.append("\n");
				}
				//append the line from the file to the string
				data.append(theLine);
				index++;
			}
		} catch (FileNotFoundException e1) {
//			e1.printStackTrace();
			Log.w(mContext.getClass().getName(),"File "+ path +" Not Found");
		} catch(IOException e2) {
			e2.printStackTrace();
		}
		System.gc();
		String output = data.toString();
		return output;
	}
	
	/**
	 * writes a boolean to a file in the form of an int. 1 = true, 0 = false
	 * @param path to file
	 * @param b boolean to write
	 * @return true if write was successful, false otherwise
	 * @author ricky barrette
	 */
	public boolean writeBoolean(String path, boolean b){
		if (b){
			return writeString(path, Integer.toString(1));
		} 
		if (!b){
			return writeString(path, Integer.toString(0));
		}
		return false;
	}
	
	/**
	 * writes an int to a file
	 * @param String path
	 * @param int num
	 * @author WWPowers
	 */
	public boolean writeInteger(String path, int num) {
		return writeString(path, Integer.toString(num));
	}

	/**
	 * writes a String to a file
	 * @param String path
	 * @param  String data
	 * @return boolean true if write was a success
	 * @return boolean false if write was not a success
	 * @author ricky barrette
	 */
	protected boolean writeString(String path, String data) {
		try {
			FileOutputStream theFile = mContext.openFileOutput(path, Context.MODE_PRIVATE);
			theFile.write(data.getBytes());
			theFile.flush();
			theFile.close();
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
